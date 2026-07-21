import request, { type CommonResult } from '../utils/request'

export interface PageResult<T> {
  records: T[]
  pageNum: number
  pageSize: number
  total: number
  pages: number
}

export interface SessionVO {
  sessionId: number
  title: string
  lastMessageAt: string | null
  createdAt: string
}

export interface SessionListVO {
  sessionId: number
  title: string
  lastMessage: string | null
  lastMessageAt: string | null
  createdAt: string
}

export interface MessageVO {
  messageId: number
  sessionId: number
  role: 'USER' | 'ASSISTANT'
  content: string
  fromCache: number
  canCreateTicket: number
  createdAt: string
}

export interface SessionCreateDTO {
  title?: string
}

export async function createSession(payload: SessionCreateDTO) {
  const response = await request.post<CommonResult<SessionVO>>('/chat/sessions', payload)
  return response.data.data
}

export async function listSessions(pageNum = 1, pageSize = 20) {
  const response = await request.get<CommonResult<PageResult<SessionListVO>>>('/chat/sessions', {
    params: { pageNum, pageSize },
  })
  return response.data.data
}

export async function listMessages(sessionId: number, pageNum = 1, pageSize = 50) {
  const response = await request.get<CommonResult<PageResult<MessageVO>>>(
    `/chat/sessions/${sessionId}/messages`,
    {
      params: { pageNum, pageSize },
    },
  )
  return response.data.data
}
