export const DEMO_USERS = [
  { id: '1', label: 'employee_demo', role: 'EMPLOYEE' },
  { id: '2', label: 'admin_demo', role: 'ADMIN' },
]

export function getDemoUserId() {
  return localStorage.getItem('workmate-user-id') || '1'
}

export function setDemoUserId(userId: string) {
  localStorage.setItem('workmate-user-id', userId)
}