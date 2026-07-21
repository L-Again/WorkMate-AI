<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import {
  ChatRound,
  Collection,
  Connection,
  DataLine,
  Files,
  FolderOpened,
  User,
} from '@element-plus/icons-vue'
import { getCurrentUser, type CurrentUserVO } from './api/user'
import { getHealth, type HealthVO } from './api/health'
import { DEMO_USERS, getDemoUserId, setDemoUserId } from './utils/userContext'

const route = useRoute()
const router = useRouter()
const currentUserId = ref(getDemoUserId())
const currentUser = ref<CurrentUserVO | null>(null)
const health = ref<HealthVO | null>(null)
const errorMessage = ref('')
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const adminOnlyPaths = ['/knowledge', '/categories', '/model-logs']

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
  if (!isAdmin.value && adminOnlyPaths.includes(route.path)) {
    await router.push('/agent')
  }
}

onMounted(loadBaseInfo)
</script>

<template>
  <el-container class="app-shell">
    <el-aside width="248px" class="sidebar">
      <div class="brand-block">
        <div class="brand-mark">
          <el-icon><Connection /></el-icon>
        </div>
        <div>
          <h1>WorkMate AI</h1>
          <p>Knowledge Agent</p>
        </div>
      </div>

      <el-menu router :default-active="route.path">
        <el-menu-item index="/agent">
          <el-icon><ChatRound /></el-icon>
          <span>Agent 聊天</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/knowledge">
          <el-icon><Collection /></el-icon>
          <span>知识管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/categories">
          <el-icon><FolderOpened /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/tickets">
          <el-icon><Files /></el-icon>
          <span>{{ isAdmin ? '工单管理' : '我的工单' }}</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/model-logs">
          <el-icon><DataLine /></el-icon>
          <span>模型日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar-user">
          <span class="topbar-label">
            <el-icon><User /></el-icon>
            当前用户
          </span>
          <el-select :model-value="currentUserId" size="small" @change="changeUser">
            <el-option
              v-for="user in DEMO_USERS"
              :key="user.id"
              :label="`${user.label} (${user.role})`"
              :value="user.id"
            />
          </el-select>
        </div>
        <div class="system-status" :class="{ online: health?.application }">
          <span class="status-dot"></span>
          {{ health?.application ? `后端已连接：${health.application}` : '后端未连接' }}
        </div>
      </el-header>

      <el-main class="app-main">
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon />
        <div v-if="currentUser" class="user-line">
          <span>{{ currentUser.displayName }}</span>
          <el-tag size="small" :type="currentUser.role === 'ADMIN' ? 'success' : 'info'">
            {{ currentUser.role }}
          </el-tag>
        </div>
        <RouterView :key="currentUserId" />
      </el-main>
    </el-container>
  </el-container>
</template>
