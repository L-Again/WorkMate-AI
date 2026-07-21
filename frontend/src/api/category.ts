import request, { type CommonResult } from '../utils/request'

export interface CategoryVO {
  id: number
  name: string
  description: string | null
  sortOrder: number
  status: number
}

export interface CategoryPayload {
  name: string
  description?: string
  sortOrder: number
}

export async function listCategories(includeDisabled = false) {
  const response = await request.get<CommonResult<CategoryVO[]>>('/knowledge/categories', {
    params: { includeDisabled },
  })
  return response.data.data
}

export async function createCategory(payload: CategoryPayload) {
  const response = await request.post<CommonResult<CategoryVO>>('/knowledge/categories', payload)
  return response.data.data
}

export async function updateCategory(categoryId: number, payload: CategoryPayload) {
  const response = await request.put<CommonResult<CategoryVO>>(
    `/knowledge/categories/${categoryId}`,
    payload,
  )
  return response.data.data
}

export async function updateCategoryStatus(categoryId: number, status: number) {
  const response = await request.patch<CommonResult<CategoryVO>>(
    `/knowledge/categories/${categoryId}/status`,
    { status },
  )
  return response.data.data
}

export async function deleteCategory(categoryId: number) {
  const response = await request.delete<CommonResult<boolean>>(`/knowledge/categories/${categoryId}`)
  return response.data.data
}
