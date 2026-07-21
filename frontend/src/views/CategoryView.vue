<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete as DeleteIcon, Edit, Plus } from '@element-plus/icons-vue'
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  updateCategoryStatus,
  type CategoryPayload,
  type CategoryVO,
} from '../api/category'
import { getCurrentUser, type CurrentUserVO } from '../api/user'

const currentUser = ref<CurrentUserVO | null>(null)
const categories = ref<CategoryVO[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingCategoryId = ref<number | null>(null)
const errorMessage = ref('')
const form = reactive<CategoryPayload>({
  name: '',
  description: '',
  sortOrder: 0,
})

const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const dialogTitle = computed(() => (editingCategoryId.value ? '编辑分类' : '新增分类'))

async function loadPage() {
  loading.value = true
  errorMessage.value = ''
  try {
    currentUser.value = await getCurrentUser()
    categories.value = await listCategories(currentUser.value.role === 'ADMIN')
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingCategoryId.value = null
  form.name = ''
  form.description = ''
  form.sortOrder = categories.value.length + 1
  dialogVisible.value = true
}

function openEditDialog(category: CategoryVO) {
  editingCategoryId.value = category.id
  form.name = category.name
  form.description = category.description || ''
  form.sortOrder = category.sortOrder
  dialogVisible.value = true
}

async function saveCategory() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  saving.value = true
  try {
    if (editingCategoryId.value) {
      await updateCategory(editingCategoryId.value, form)
      ElMessage.success('分类已更新')
    } else {
      await createCategory(form)
      ElMessage.success('分类已创建')
    }
    dialogVisible.value = false
    await loadPage()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  } finally {
    saving.value = false
  }
}

async function toggleStatus(category: CategoryVO) {
  const nextStatus = category.status === 1 ? 0 : 1
  try {
    await updateCategoryStatus(category.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '分类已启用' : '分类已停用')
    await loadPage()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

async function removeCategory(category: CategoryVO) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${category.name}」？`, '删除分类', {
      type: 'warning',
    })
    await deleteCategory(category.id)
    ElMessage.success('分类已删除')
    await loadPage()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(resolveErrorMessage(error))
    }
  }
}

function resolveErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '请求失败'
}

onMounted(loadPage)
</script>

<template>
  <section class="admin-page">
    <div class="admin-header">
      <div>
        <h2>分类管理</h2>
        <p>管理员维护知识分类，分类停用后其知识不参与 Agent 检索。</p>
      </div>
      <el-button type="primary" :icon="Plus" :disabled="!isAdmin" @click="openCreateDialog">新增分类</el-button>
    </div>

    <el-alert
      v-if="!isAdmin"
      title="当前用户不是管理员，只能查看启用分类。请切换到 admin_demo 后进行管理操作。"
      type="warning"
      show-icon
      class="admin-alert"
    />
    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon class="admin-alert" />

    <div class="table-surface">
      <el-table v-loading="loading" :data="categories">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" :disabled="!isAdmin" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" :disabled="!isAdmin" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" :icon="DeleteIcon" :disabled="!isAdmin" @click="removeCategory(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form label-position="top">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类说明">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
