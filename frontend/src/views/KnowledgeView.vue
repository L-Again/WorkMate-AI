<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete as DeleteIcon, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { listCategories, type CategoryVO } from '../api/category'
import {
  createKnowledge,
  deleteKnowledge,
  getKnowledgeDetail,
  pageKnowledge,
  updateKnowledge,
  updateKnowledgeStatus,
  type KnowledgeListItemVO,
  type KnowledgePayload,
} from '../api/knowledge'
import { getCurrentUser, type CurrentUserVO } from '../api/user'

const currentUser = ref<CurrentUserVO | null>(null)
const categories = ref<CategoryVO[]>([])
const records = ref<KnowledgeListItemVO[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingKnowledgeId = ref<number | null>(null)
const errorMessage = ref('')
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: null as number | null,
  status: null as number | null,
})
const form = reactive<KnowledgePayload>({
  categoryId: 0,
  title: '',
  keywords: '',
  content: '',
  status: 1,
})

const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const dialogTitle = computed(() => (editingKnowledgeId.value ? '编辑知识' : '新增知识'))

async function loadBaseData() {
  currentUser.value = await getCurrentUser()
  categories.value = await listCategories(currentUser.value.role === 'ADMIN')
}

async function loadKnowledge() {
  loading.value = true
  errorMessage.value = ''
  try {
    if (!currentUser.value) {
      await loadBaseData()
    }
    const page = await pageKnowledge(query)
    records.value = page.records
    total.value = page.total
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.categoryId = null
  query.status = null
  query.pageNum = 1
  void loadKnowledge()
}

function openCreateDialog() {
  editingKnowledgeId.value = null
  form.categoryId = categories.value[0]?.id || 0
  form.title = ''
  form.keywords = ''
  form.content = ''
  form.status = 1
  dialogVisible.value = true
}

async function openEditDialog(row: KnowledgeListItemVO) {
  try {
    const detail = await getKnowledgeDetail(row.id)
    editingKnowledgeId.value = detail.id
    form.categoryId = detail.categoryId
    form.title = detail.title
    form.keywords = detail.keywords || ''
    form.content = detail.content
    form.status = detail.status
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

async function saveKnowledge() {
  if (!form.categoryId || !form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写分类、标题和正文')
    return
  }

  saving.value = true
  try {
    if (editingKnowledgeId.value) {
      await updateKnowledge(editingKnowledgeId.value, form)
      ElMessage.success('知识已更新')
    } else {
      await createKnowledge(form)
      ElMessage.success('知识已创建')
    }
    dialogVisible.value = false
    await loadKnowledge()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: KnowledgeListItemVO) {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await updateKnowledgeStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '知识已启用' : '知识已停用')
    await loadKnowledge()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

async function removeKnowledge(row: KnowledgeListItemVO) {
  try {
    await ElMessageBox.confirm(`确认删除知识「${row.title}」？`, '删除知识', {
      type: 'warning',
    })
    await deleteKnowledge(row.id)
    ElMessage.success('知识已删除')
    await loadKnowledge()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(resolveErrorMessage(error))
    }
  }
}

function handlePageChange(pageNum: number) {
  query.pageNum = pageNum
  void loadKnowledge()
}

function formatDate(value: string) {
  return value ? value.replace('T', ' ') : '-'
}

function resolveErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '请求失败'
}

onMounted(async () => {
  await loadBaseData()
  await loadKnowledge()
})
</script>

<template>
  <section class="admin-page">
    <div class="admin-header">
      <div>
        <h2>知识管理</h2>
        <p>管理员维护知识内容，知识变更后后端会清理 Redis 问答缓存。</p>
      </div>
      <el-button type="primary" :icon="Plus" :disabled="!isAdmin" @click="openCreateDialog">新增知识</el-button>
    </div>

    <el-alert
      v-if="!isAdmin"
      title="当前用户不是管理员，只能查看知识。请切换到 admin_demo 后进行管理操作。"
      type="warning"
      show-icon
      class="admin-alert"
    />
    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon class="admin-alert" />

    <div class="admin-toolbar">
      <el-input v-model="query.keyword" clearable placeholder="搜索标题、关键词、正文" />
      <el-select v-model="query.categoryId" clearable placeholder="分类">
        <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadKnowledge()">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="table-surface">
      <el-table v-loading="loading" :data="records">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="keywords" label="关键词" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" :disabled="!isAdmin" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" :disabled="!isAdmin" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" :icon="DeleteIcon" :disabled="!isAdmin" @click="removeKnowledge(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      class="admin-pagination"
      layout="prev, pager, next, total"
      :current-page="query.pageNum"
      :page-size="query.pageSize"
      :total="total"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form label-position="top">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="form.keywords" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveKnowledge">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
