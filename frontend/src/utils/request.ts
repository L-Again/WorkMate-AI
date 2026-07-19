import axios from 'axios'

export interface CommonResult<T> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const userId = localStorage.getItem('workmate-user-id') || '1'
  config.headers['X-User-Id'] = userId
  return config
})

request.interceptors.response.use((response) => {
  const result = response.data as CommonResult<unknown>
  if (result.code !== 200) {
    return Promise.reject(new Error(result.message || '请求失败'))
  }
  return response
})

export default request