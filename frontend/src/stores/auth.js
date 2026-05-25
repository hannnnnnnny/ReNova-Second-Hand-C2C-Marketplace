import { defineStore } from 'pinia'
import { authApi } from '../api/endpoints'
import { TOKEN_KEY } from '../api/client'

const USER_KEY = 'renova.user'

function readUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser()
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user)
  },
  actions: {
    persist() {
      if (this.token) {
        localStorage.setItem(TOKEN_KEY, this.token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
      if (this.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        localStorage.removeItem(USER_KEY)
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
