import request, { type CommonResult } from '../utils/request'

export interface CurrentUserVO {
  id: number
  username: string
  displayName: string
  role: string
  status: number
}

export async function getCurrentUser() {
  const response = await request.get<CommonResult<CurrentUserVO>>('/users/current')
  return response.data.data
}