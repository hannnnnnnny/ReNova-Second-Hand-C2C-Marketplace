import axios from 'axios'

const TOKEN_KEY = 'renova.token'
const DEFAULT_ERROR_MESSAGE = 'The request could not be completed.'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
    }
    return Promise.reject(error)
  }
)

export function unwrap(response) {
  return response.data?.data ?? response.data
}

export function apiError(error, fallback = DEFAULT_ERROR_MESSAGE) {
  if (!error.response) {
    return 'Cannot reach the server. Check that the backend is running.'
  }
  const data = error.response.data
  const validation = data?.errors
  if (Array.isArray(validation) && validation.length) {
    return validation.map((v) => v.message || v.field).filter(Boolean).join(' • ')
  }
  return data?.message || fallback
}

export { TOKEN_KEY }
export default apiClient
