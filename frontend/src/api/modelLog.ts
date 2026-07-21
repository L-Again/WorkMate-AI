import request, { type CommonResult } from '../utils/request'
import type { PageResult } from './chat'

export interface ModelCallLogVO {
  id: number
  userId: number
  sessionId: number
  questionMessageId: number | null
  answerMessageId: number | null
  modelName: string | null
  fromCache: boolean
  callStatus: string
  durationMs: number
  promptTokens: number | null
  completionTokens: number | null
  errorMessage: string | null
  createdAt: string
}

export async function pageModelLogs(pageNum = 1, pageSize = 20, callStatus?: string) {
  const response = await request.get<CommonResult<PageResult<ModelCallLogVO>>>(
    '/admin/model-logs',
    {
      params: { pageNum, pageSize, callStatus: callStatus || undefined },
    },
  )
  return response.data.data
}
