import request, { type CommonResult } from '../utils/request'
import type { PageResult } from './chat'

export interface KnowledgeListItemVO {
  id: number
  categoryId: number
  categoryName: string
  title: string
  keywords: string | null
  status: number
  updatedAt: string
}

export interface KnowledgeDetailVO extends KnowledgeListItemVO {
  content: string
}

export interface KnowledgePayload {
  categoryId: number
  title: string
  keywords?: string
  content: string
  status: number
}

export interface KnowledgeQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  categoryId?: number | null
  status?: number | null
}

export async function pageKnowledge(query: KnowledgeQuery) {
  const response = await request.get<CommonResult<PageResult<KnowledgeListItemVO>>>('/knowledge', {
    params: {
      pageNum: query.pageNum ?? 1,
      pageSize: query.pageSize ?? 10,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      status: query.status ?? undefined,
    },
  })
  return response.data.data
}

export async function getKnowledgeDetail(knowledgeId: number) {
  const response = await request.get<CommonResult<KnowledgeDetailVO>>(`/knowledge/${knowledgeId}`)
  return response.data.data
}

export async function createKnowledge(payload: KnowledgePayload) {
  const response = await request.post<CommonResult<KnowledgeDetailVO>>('/knowledge', payload)
  return response.data.data
}

export async function updateKnowledge(knowledgeId: number, payload: KnowledgePayload) {
  const response = await request.put<CommonResult<KnowledgeDetailVO>>(
    `/knowledge/${knowledgeId}`,
    payload,
  )
  return response.data.data
}

export async function updateKnowledgeStatus(knowledgeId: number, status: number) {
  const response = await request.patch<CommonResult<KnowledgeDetailVO>>(
    `/knowledge/${knowledgeId}/status`,
    { status },
  )
  return response.data.data
}

export async function deleteKnowledge(knowledgeId: number) {
  const response = await request.delete<CommonResult<boolean>>(`/knowledge/${knowledgeId}`)
  return response.data.data
}
