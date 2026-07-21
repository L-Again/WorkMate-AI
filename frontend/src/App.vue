<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterView } from 'vue-router'
import { getCurrentUser, type CurrentUserVO } from './api/user'
import { getHealth, type HealthVO } from './api/health'
import { DEMO_USERS, getDemoUserId, setDemoUserId } from './utils/userContext'

const currentUserId = ref(getDemoUserId())
const currentUser = ref<CurrentUserVO | null>(null)
const health = ref<HealthVO | null>(null)
const errorMessage = ref('')

async function loadBaseInfo() {
  errorMessage.value = ''
  try {
    currentUser.value = await getCurrentUser()
    health.value = await getHealth()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '请求失败'
  }
}

async function changeUser(userId: string) {
  setDemoUserId(userId)
  currentUserId.value = userId
  await loadBaseInfo()
}

onMounted(loadBaseInfo)
</script>

<template>
  <el-container class="app-shell">
    <el-aside width="220px" class="sidebar">
      <h1>WorkMate AI</h1>

      <el-menu router default-active="/agent">
        <el-menu-item index="/agent">Agent 聊天</el-menu-item>
        <el-menu-item index="/knowledge">知识管理</el-menu-item>
        <el-menu-item index="/categories">分类管理</el-menu-item>
        <el-menu-item index="/tickets">工单管理</el-menu-item>
        <el-menu-item index="/model-logs">模型日志</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          当前用户：
          <el-select :model-value="currentUserId" size="small" @change="changeUser">
            <el-option
              v-for="user in DEMO_USERS"
              :key="user.id"
              :label="`${user.label} (${user.role})`"
              :value="user.id"
            />
          </el-select>
        </div>
        <div>后端状态：{{ health?.application || '未连接' }}</div>
      </el-header>

      <el-main>
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon />
        <p v-if="currentUser" class="user-line">
          {{ currentUser.displayName }} / {{ currentUser.role }}
        </p>
        <RouterView :key="currentUserId" />
      </el-main>
    </el-container>
  </el-container>
</template>
