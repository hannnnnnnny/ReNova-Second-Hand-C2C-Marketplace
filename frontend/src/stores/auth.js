import { defineStore } from 'pinia'
import { authApi } from '../api/endpoints'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    initialized: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.user)
  },
  actions: {
    async initialize() {
      if (this.initialized) return this.user
      try {
        await authApi.csrf()
        await this.refresh()
      } catch {
        this.user = null
      } finally {
        this.initialized = true
      }
      return this.user
    },
    async signup(payload) {
      await authApi.csrf()
      const result = await authApi.signup(payload)
      this.user = result.user
      return result
    },
    async login(payload) {
      await authApi.csrf()
      const result = await authApi.login(payload)
      this.user = result.user
      return result
    },
    async refresh() {
      try {
        const user = await authApi.me()
        this.user = user
        return user
      } catch {
        this.user = null
        return null
      }
    },
    async logout() {
      try {
        await authApi.csrf()
        await authApi.logout()
      } finally {
        this.user = null
      }
    },
    setUser(user) {
      this.user = user
    }
  }
})
