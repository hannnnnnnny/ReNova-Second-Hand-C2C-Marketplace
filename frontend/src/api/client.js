import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const ADMIN_LOGIN_PATH = '/admin/login'
const DEFAULT_ERROR_MESSAGE = 'The request could not be completed.'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.clearSession()
      redirectExpiredAdminSession()
    }
    return Promise.reject(error)
  }
)

export function getApiData(response) {
  return response.data?.data ?? response.data
}

export function getApiError(error, fallbackMessage = DEFAULT_ERROR_MESSAGE) {
  if (!error.response) {
    return 'The server is unavailable. Check that the backend is running and try again.'
  }

  const validationMessages = getValidationMessages(error.response.data?.errors)
  if (validationMessages.length) {
    return validationMessages.join(' ')
  }

  return error.response?.data?.message || error.message || fallbackMessage
}

function getValidationMessages(errors) {
  if (Array.isArray(errors)) {
    return errors
      .map((entry) => formatFieldError(entry.field, entry.message))
      .filter(Boolean)
  }

  if (errors && typeof errors === 'object') {
    return Object.entries(errors)
      .map(([field, message]) => formatFieldError(field, message))
      .filter(Boolean)
  }

  return []
}

function formatFieldError(field, message) {
  if (!message) return ''
  if (!field) return message
  return `${humanizeField(field)}: ${message}`
}

function humanizeField(field) {
  return String(field)
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/[_-]+/g, ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function redirectExpiredAdminSession() {
  if (typeof window === 'undefined') return
  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  const isAdminRoute = window.location.pathname.startsWith('/admin')
  const isLoginRoute = window.location.pathname === ADMIN_LOGIN_PATH

  if (isAdminRoute && !isLoginRoute) {
    const loginUrl = `${ADMIN_LOGIN_PATH}?redirect=${encodeURIComponent(currentPath)}&session=expired`
    window.location.assign(loginUrl)
  }
}

export default apiClient
