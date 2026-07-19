import request, { type CommonResult } from '../utils/request'

export interface HealthVO {
  application: string
}

export async function getHealth() {
  const response = await request.get<CommonResult<HealthVO>>('/health')
  return response.data.data
}