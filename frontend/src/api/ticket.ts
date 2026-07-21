import request, { type CommonResult } from '../utils/request'
import type { PageResult } from './chat'

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

export interface TicketListVO {
  ticketId: number
  ticketNo: string
  userId: number
  title: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface TicketStatusUpdateDTO {
  status: string
  resolution?: string
}

export async function createTicket(payload: TicketCreateDTO) {
  const response = await request.post<CommonResult<TicketVO>>('/tickets', payload)
  return response.data.data
}

export async function listMyTickets(pageNum = 1, pageSize = 10, status?: string) {
  const response = await request.get<CommonResult<PageResult<TicketListVO>>>('/tickets/my', {
    params: { pageNum, pageSize, status: status || undefined },
  })
  return response.data.data
}

export async function listAdminTickets(
  pageNum = 1,
  pageSize = 10,
  status?: string,
  keyword?: string,
) {
  const response = await request.get<CommonResult<PageResult<TicketListVO>>>('/admin/tickets', {
    params: {
      pageNum,
      pageSize,
      status: status || undefined,
      keyword: keyword || undefined,
    },
  })
  return response.data.data
}

export async function getTicketDetail(ticketId: number) {
  const response = await request.get<CommonResult<TicketVO>>(`/tickets/${ticketId}`)
  return response.data.data
}

export async function updateTicketStatus(ticketId: number, payload: TicketStatusUpdateDTO) {
  const response = await request.patch<CommonResult<TicketVO>>(
    `/admin/tickets/${ticketId}/status`,
    payload,
  )
  return response.data.data
}
