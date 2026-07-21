<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { pageModelLogs, type ModelCallLogVO } from '../api/modelLog'
import { getCurrentUser, type CurrentUserVO } from '../api/user'

const currentUser = ref<CurrentUserVO | null>(null)
const records = ref<ModelCallLogVO[]>([])
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  callStatus: '',
})

const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

async function loadLogs() {
  loading.value = true
  errorMessage.value = ''
  try {
    currentUser.value = await getCurrentUser()
    if (!isAdmin.value) {
      records.value = []
      total.value = 0
      return
    }
    const page = await pageModelLogs(query.pageNum, query.pageSize, query.callStatus)
    records.value = page.records
    total.value = page.total
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.callStatus = ''
  query.pageNum = 1
  void loadLogs()
}

function handlePageChange(pageNum: number) {
  query.pageNum = pageNum
  void loadLogs()
}

function statusType(status: string) {
  if (status === 'SUCCESS') {
    return 'success'
  }
  if (status === 'CACHE_HIT') {
    return 'primary'
  }
  if (status === 'NO_KNOWLEDGE') {
    return 'warning'
  }
  return 'danger'
}

function formatDate(value: string) {
  return value ? value.replace('T', ' ') : '-'
}

function resolveErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '请求失败'
}

onMounted(loadLogs)
</script>

<template>
  <section class="admin-page">
    <div class="admin-header">
      <div>
        <h2>模型日志</h2>
        <p>管理员查看 Agent 问答、缓存命中、无知识和模型失败日志。</p>
      </div>
    </div>

    <el-alert
      v-if="!isAdmin"
      title="当前用户不是管理员，不能查看模型调用日志。请切换到 admin_demo。"
      type="warning"
      show-icon
      class="admin-alert"
    />
    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon class="admin-alert" />

    <div class="admin-toolbar">
      <el-select v-model="query.callStatus" clearable placeholder="调用状态" :disabled="!isAdmin">
        <el-option label="SUCCESS" value="SUCCESS" />
        <el-option label="CACHE_HIT" value="CACHE_HIT" />
        <el-option label="NO_KNOWLEDGE" value="NO_KNOWLEDGE" />
        <el-option label="FAILED" value="FAILED" />
      </el-select>
      <el-button type="primary" :disabled="!isAdmin" @click="query.pageNum = 1; loadLogs()">查询</el-button>
      <el-button :disabled="!isAdmin" @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column prop="sessionId" label="会话 ID" width="110" />
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <el-tag :type="statusType(row.callStatus)">{{ row.callStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="缓存" width="90">
        <template #default="{ row }">
          <el-tag :type="row.fromCache ? 'success' : 'info'">
            {{ row.fromCache ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="modelName" label="模型" width="140" />
      <el-table-column prop="durationMs" label="耗时 ms" width="110" />
      <el-table-column prop="errorMessage" label="错误信息" min-width="220" show-overflow-tooltip />
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
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
  </section>
</template>
