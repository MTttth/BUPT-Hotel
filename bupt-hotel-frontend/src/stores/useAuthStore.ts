// src/store/useAuthStore.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export type Role = 'ADMIN' | 'RECEPTION' | 'MANAGER' | 'CLIENT'

export const useAuthStore = defineStore('auth', () => {
  const role = ref<Role>()
  // 登录时的错误信息
  const error = ref<string | null>(null)
  // 如果后端返回 Token，可以存储
  const token = ref<string | null>(null)
  // 如果用户（User）登录到某房间，需要记录当前房间号
  const userRoomId = ref<number | null>(null)

  function loginAsReception(receivedToken: string) {
    role.value = 'RECEPTION'
    token.value = receivedToken
    error.value = null
    userRoomId.value = null
  }

  function loginAsAdmin(receivedToken: string) {
    role.value = 'ADMIN'
    token.value = receivedToken
    error.value = null
    userRoomId.value = null
  }

  function loginAsClient(roomId: number, password: string) {
    // 这里只做前端记录，具体密码校验留给后端接口
    role.value = 'CLIENT'
    error.value = null
    userRoomId.value = roomId
  }

  function setError(msg: string) {
    error.value = msg
  }

  function logout() {
    role.value = 'CLIENT'
    token.value = null
    error.value = null
    userRoomId.value = null
  }

  return {
    role,
    error,
    token,
    userRoomId,
    loginAsReception,
    loginAsAdmin,
    loginAsClient,
    setError,
    logout,
  }
})
