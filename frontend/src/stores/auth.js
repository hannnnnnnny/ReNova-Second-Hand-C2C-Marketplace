import { defineStore } from 'pinia'
import { authApi } from '../api/endpoints'
import { TOKEN_KEY } from '../api/client'
import { readStorageItem, readStorageJson, removeStorageItem, writeStorageItem, writeStorageJson } from '../utils/browserStorage'

const USER_KEY = 'renova.user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: readStorageItem(TOKEN_KEY),
    user: readStorageJson(USER_KEY)
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user)
  },
  actions: {
    persist() {
      if (this.token) {
        writeStorageItem(TOKEN_KEY, this.token)
      } else {
        removeStorageItem(TOKEN_KEY)
      }
      if (this.user) {
        writeStorageJson(USER_KEY, this.user)
      } else {
        removeStorageItem(USER_KEY)
      }
    },
    async signup(payload) {
      const result = await authApi.signup(payload)
      this.token = result.token
      this.user = result.user
      this.persist()
      return result
    },
    async login(payload) {
      const result = await authApi.login(payload)
      this.token = result.token
      this.user = result.user
      this.persist()
      return result
    },
    async refresh() {
      if (!this.token) return null
      try {
        const user = await authApi.me()
        this.user = user
        this.persist()
        return user
      } catch {
        this.logout()
        return null
      }
    },
    logout() {
      this.token = ''
      this.user = null
      this.persist()
    },
    setUser(user) {
      this.user = user
      this.persist()
    }
  }
})
