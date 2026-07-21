<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Promotion, Tickets } from '@element-plus/icons-vue'
import { chatWithAgent, type AgentAnswerVO } from '../api/agent'
import { createSession, listMessages, listSessions, type MessageVO, type SessionListVO } from '../api/chat'
import { createTicket } from '../api/ticket'

const sessions = ref<SessionListVO[]>([])
const messages = ref<MessageVO[]>([])
const selectedSessionId = ref<number | null>(null)
const question = ref('')
const lastQuestion = ref('')
const lastAnswer = ref<AgentAnswerVO | null>(null)
const loadingSessions = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const creatingTicket = ref(false)
const errorMessage = ref('')
const ticketDialogVisible = ref(false)
const ticketTitle = ref('')
const ticketDescription = ref('')

const selectedSession = computed(() =>
  sessions.value.find((session) => session.sessionId === selectedSessionId.value),
)

const canCreateTicket = computed(
  () => Boolean(lastAnswer.value?.canCreateTicket && lastAnswer.value.questionMessageId),
)

async function loadSessions() {
  loadingSessions.value = true
  errorMessage.value = ''
  try {
    const page = await listSessions()
    sessions.value = page.records
    if (!selectedSessionId.value && sessions.value.length > 0) {
      selectedSessionId.value = sessions.value[0].sessionId
      await loadMessages(selectedSessionId.value)
    }
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    loadingSessions.value = false
  }
}

async function loadMessages(sessionId: number) {
  loadingMessages.value = true
  errorMessage.value = ''
  try {
    const page = await listMessages(sessionId)
    messages.value = page.records
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    loadingMessages.value = false
  }
}

async function handleCreateSession() {
  errorMessage.value = ''
  try {
    const session = await createSession({ title: '新会话' })
    selectedSessionId.value = session.sessionId
    lastAnswer.value = null
    messages.value = []
    await loadSessions()
    await loadMessages(session.sessionId)
    ElMessage.success('会话已创建')
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  }
}

async function handleSelectSession(sessionId: number) {
  selectedSessionId.value = sessionId
  lastAnswer.value = null
  await loadMessages(sessionId)
}

async function handleSendQuestion() {
  const text = question.value.trim()
  if (!text) {
    ElMessage.warning('请输入问题')
    return
  }

  sending.value = true
  errorMessage.value = ''
  try {
    let sessionId = selectedSessionId.value
    if (!sessionId) {
      const session = await createSession({ title: '新会话' })
      sessionId = session.sessionId
      selectedSessionId.value = sessionId
    }

    lastQuestion.value = text
    const answer = await chatWithAgent({ sessionId, question: text })
    lastAnswer.value = answer
    question.value = ''
    await loadSessions()
    selectedSessionId.value = sessionId
    await loadMessages(sessionId)
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    sending.value = false
  }
}

function openTicketDialog() {
  if (!lastAnswer.value) {
    return
  }
  ticketTitle.value = lastQuestion.value ? lastQuestion.value.slice(0, 80) : '人工咨询'
  ticketDescription.value = lastQuestion.value
    ? `Agent 未找到可靠知识，请人工确认：${lastQuestion.value}`
    : 'Agent 未找到可靠知识，请人工确认。'
  ticketDialogVisible.value = true
}

async function handleCreateTicket() {
  if (!lastAnswer.value || !selectedSessionId.value) {
    return
  }

  creatingTicket.value = true
  errorMessage.value = ''
  try {
    const ticket = await createTicket({
      sessionId: selectedSessionId.value,
      questionMessageId: lastAnswer.value.questionMessageId,
      title: ticketTitle.value,
      description: ticketDescription.value,
    })
    ticketDialogVisible.value = false
    ElMessage.success(`工单已创建：${ticket.ticketNo}`)
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    creatingTicket.value = false
  }
}

function formatDate(value: string | null) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

function resolveErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '请求失败'
}

onMounted(loadSessions)
</script>

<template>
  <section class="chat-workspace">
    <aside class="session-panel">
      <div class="panel-title-row">
        <h2>会话</h2>
        <el-button type="primary" size="small" :icon="Plus" @click="handleCreateSession">新建</el-button>
      </div>

      <el-skeleton v-if="loadingSessions" :rows="5" animated />
      <el-empty v-else-if="sessions.length === 0" description="暂无会话" />

      <div v-else class="session-list">
        <button
          v-for="session in sessions"
          :key="session.sessionId"
          class="session-item"
          :class="{ active: session.sessionId === selectedSessionId }"
          type="button"
          @click="handleSelectSession(session.sessionId)"
        >
          <span class="session-title">
            <span>{{ session.title }}</span>
          </span>
          <span class="session-message">{{ session.lastMessage || '暂无消息' }}</span>
          <span class="session-time">{{ formatDate(session.lastMessageAt || session.createdAt) }}</span>
        </button>
      </div>
    </aside>

    <section class="conversation-panel">
      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon class="chat-alert" />

      <div class="conversation-header">
        <div>
          <h2>{{ selectedSession?.title || 'Agent 聊天' }}</h2>
          <p>企业知识库问答工作台</p>
        </div>
        <el-tag v-if="lastAnswer?.fromCache" type="success">缓存命中</el-tag>
      </div>

      <div v-loading="loadingMessages" class="message-list">
        <el-empty v-if="!selectedSessionId && messages.length === 0" description="新建会话后开始提问" />
        <el-empty v-else-if="messages.length === 0" description="当前会话暂无消息" />

        <article
          v-for="message in messages"
          :key="message.messageId"
          class="message-row"
          :class="message.role === 'USER' ? 'message-user' : 'message-assistant'"
        >
          <div class="message-meta">
            <span class="message-avatar">{{ message.role === 'USER' ? '我' : 'AI' }}</span>
            <strong>{{ message.role === 'USER' ? '我' : 'WorkMate AI' }}</strong>
            <span>{{ formatDate(message.createdAt) }}</span>
            <el-tag v-if="message.fromCache === 1" size="small" type="success">缓存</el-tag>
            <el-tag v-if="message.canCreateTicket === 1" size="small" type="warning">可建工单</el-tag>
          </div>
          <p>{{ message.content }}</p>
        </article>
      </div>

      <div class="question-box">
        <el-input
          v-model="question"
          type="textarea"
          :rows="3"
          maxlength="2000"
          show-word-limit
          placeholder="例如：Git 分支应该怎么命名？"
          @keydown.meta.enter.prevent="handleSendQuestion"
          @keydown.ctrl.enter.prevent="handleSendQuestion"
        />
        <div class="question-actions">
          <span>支持 Ctrl/Command + Enter 发送</span>
          <el-button type="primary" :icon="Promotion" :loading="sending" @click="handleSendQuestion">发送</el-button>
        </div>
      </div>
    </section>

    <aside class="answer-panel">
      <div class="info-block">
        <div class="panel-title-row">
          <h2>引用知识</h2>
          <el-button v-if="canCreateTicket" type="warning" size="small" :icon="Tickets" @click="openTicketDialog">
            创建工单
          </el-button>
        </div>
        <el-empty v-if="!lastAnswer || lastAnswer.references.length === 0" description="暂无引用" />
        <div v-else class="reference-list">
          <div v-for="reference in lastAnswer.references" :key="reference.knowledgeId" class="reference-item">
            <strong>{{ reference.title }}</strong>
            <span>{{ reference.categoryName }}</span>
          </div>
        </div>
      </div>

      <div class="info-block">
        <h2>执行过程</h2>
        <el-empty v-if="!lastAnswer" description="暂无执行记录" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="step in lastAnswer.traceSteps"
            :key="`${step.step}-${step.detail}`"
            :type="step.success ? 'success' : 'danger'"
          >
            <strong>{{ step.description }}</strong>
            <p>{{ step.detail }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </aside>
  </section>

  <el-dialog v-model="ticketDialogVisible" title="创建人工咨询工单" width="520px">
    <el-form label-position="top">
      <el-form-item label="标题">
        <el-input v-model="ticketTitle" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="问题描述">
        <el-input v-model="ticketDescription" type="textarea" :rows="5" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="ticketDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="creatingTicket" @click="handleCreateTicket">确认创建</el-button>
    </template>
  </el-dialog>
</template>
