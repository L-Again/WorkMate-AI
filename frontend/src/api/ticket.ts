import request, { type CommonResult } from '../utils/request'

export interface TicketVO {
  ticketId: number
  ticketNo: string
  userId: number
  sessionId: number | null
  questionMessageId: number | null
  title: string
  description: string
  status: string
  resolution: string | null
  handledBy: number | null
  resolvedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface TicketCreateDTO {
  sessionId: number
  questionMessageId: number
  title: string
  description: string
}

export async function createTicket(payload: TicketCreateDTO) {
  const response = await request.post<CommonResult<TicketVO>>('/tickets', payload)
  return response.data.data
}
