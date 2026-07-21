<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTicketDetail,
  listAdminTickets,
  listMyTickets,
  updateTicketStatus,
  type TicketListVO,
  type TicketVO,
} from '../api/ticket'
import { getCurrentUser, type CurrentUserVO } from '../api/user'

const currentUser = ref<CurrentUserVO | null>(null)
const records = ref<TicketListVO[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const detailVisible = ref(false)
const statusDialogVisible = ref(false)
const selectedTicket = ref<TicketVO | null>(null)
const errorMessage = ref('')
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  keyword: '',
})
const statusForm = reactive({
  status: 'PROCESSING',
  resolution: '',
})

const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

async function loadTickets() {
  loading.value = true
  errorMessage.value = ''
  try {
    currentUser.value = await getCurrentUser()
    const page = isAdmin.value
      ? await listAdminTickets(query.pageNum, query.pageSize, query.status, query.keyword)
      : await listMyTickets(query.pageNum, query.pageSize, query.status)
    records.value = page.records
    total.value = page.total
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    loading.value = false
  }
}

async function openDetail(row: TicketListVO) {
  try {
    selectedTicket.value = await getTicketDetail(row.ticketId)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

async function openStatusDialog(row: TicketListVO) {
  try {
    selectedTicket.value = await getTicketDetail(row.ticketId)
    statusForm.status = nextSuggestedStatus(selectedTicket.value.status)
    statusForm.resolution = selectedTicket.value.resolution || ''
    statusDialogVisible.value = true
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

async function saveStatus() {
  if (!selectedTicket.value) {
    return
  }
  if (statusForm.status === 'RESOLVED' && !statusForm.resolution.trim()) {
    ElMessage.warning('RESOLVED 状态必须填写处理结果')
    return
  }

  saving.value = true
  try {
    await updateTicketStatus(selectedTicket.value.ticketId, {
      status: statusForm.status,
      resolution: statusForm.resolution || undefined,
    })
    ElMessage.success('工单状态已更新')
    statusDialogVisible.value = false
    await loadTickets()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  } finally {
    saving.value = false
  }
}

function resetQuery() {
  query.status = ''
  query.keyword = ''
  query.pageNum = 1
  void loadTickets()
}

function handlePageChange(pageNum: number) {
  query.pageNum = pageNum
  void loadTickets()
}

function nextSuggestedStatus(status: string) {
  if (status === 'PENDING') {
    return 'PROCESSING'
  }
  if (status === 'PROCESSING') {
    return 'RESOLVED'
  }
  if (status === 'RESOLVED') {
    return 'CLOSED'
  }
  return status
}

function statusType(status: string) {
  if (status === 'PENDING') {
    return 'warning'
  }
  if (status === 'PROCESSING') {
    return 'primary'
  }
  if (status === 'RESOLVED') {
    return 'success'
  }
  return 'info'
}

function formatDate(value: string | null) {
  return value ? value.replace('T', ' ') : '-'
}

function resolveErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '请求失败'
}

onMounted(loadTickets)
</script>

<template>
  <section class="admin-page">
    <div class="admin-header">
      <div>
        <h2>工单管理</h2>
        <p>员工查看自己的工单，管理员查看并处理全部工单。</p>
      </div>
      <el-tag :type="isAdmin ? 'success' : 'info'">{{ isAdmin ? '管理员视图' : '员工视图' }}</el-tag>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon class="admin-alert" />

    <div class="admin-toolbar">
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="PENDING" value="PENDING" />
        <el-option label="PROCESSING" value="PROCESSING" />
        <el-option label="RESOLVED" value="RESOLVED" />
        <el-option label="CLOSED" value="CLOSED" />
      </el-select>
      <el-input v-if="isAdmin" v-model="query.keyword" clearable placeholder="管理员按标题搜索" />
      <el-button type="primary" @click="query.pageNum = 1; loadTickets()">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="ticketNo" label="工单号" min-width="180" />
      <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button size="small" type="primary" :disabled="!isAdmin" @click="openStatusDialog(row)">
            处理
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="admin-pagination"
      layout="prev, pager, next, total"
      :current-page="query.pageNum"
      :page-size="query.pageSize"
      :total="total"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="detailVisible" title="工单详情" width="720px">
      <el-descriptions v-if="selectedTicket" :column="2" border>
        <el-descriptions-item label="工单号">{{ selectedTicket.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selectedTicket.status }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ selectedTicket.userId }}</el-descriptions-item>
        <el-descriptions-item label="会话 ID">{{ selectedTicket.sessionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ selectedTicket.title }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ selectedTicket.description }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">
          {{ selectedTicket.resolution || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="解决时间">
          {{ formatDate(selectedTicket.resolvedAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人">
          {{ selectedTicket.handledBy || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="处理工单" width="520px">
      <el-form label-position="top">
        <el-form-item label="目标状态">
          <el-select v-model="statusForm.status">
            <el-option label="PROCESSING" value="PROCESSING" />
            <el-option label="RESOLVED" value="RESOLVED" />
            <el-option label="CLOSED" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="statusForm.resolution" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStatus">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
