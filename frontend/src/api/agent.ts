import request, { type CommonResult } from '../utils/request'

export interface KnowledgeReferenceVO {
  knowledgeId: number
  title: string
  categoryName: string
}

export interface AgentTraceStepVO {
  step: string
  description: string
  success: boolean
  detail: string
}

export interface AgentAnswerVO {
  sessionId: number
  questionMessageId: number
  answerMessageId: number | null
  answer: string
  fromCache: boolean
  canCreateTicket: boolean
  references: KnowledgeReferenceVO[]
  traceSteps: AgentTraceStepVO[]
}

export interface AgentChatDTO {
  sessionId: number
  question: string
}

export async function chatWithAgent(payload: AgentChatDTO) {
  const response = await request.post<CommonResult<AgentAnswerVO>>('/agent/chat', payload)
  return response.data.data
}
