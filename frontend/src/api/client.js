import axios from 'axios'

const DEFAULT_ERROR_MESSAGE = 'The request could not be completed.'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json'
  }
})

export function unwrap(response) {
  return response.data?.data ?? response.data
}

export function apiError(error, fallback = DEFAULT_ERROR_MESSAGE) {
  if (!error.response) {
    return 'Cannot reach the server. Check that the backend is running.'
  }

  const data = error.response.data
  const validationMessages = validationErrorMessages(data?.errors)
  if (validationMessages.length) {
    return validationMessages.join('; ')
  }

  return data?.message || fallback
}

function validationErrorMessages(errors) {
  if (Array.isArray(errors)) {
    return errors.map((error) => fieldErrorMessage(error.field, error.message)).filter(Boolean)
  }

  if (errors && typeof errors === 'object') {
    return Object.entries(errors)
      .map(([field, message]) => fieldErrorMessage(field, message))
      .filter(Boolean)
  }

  return []
}

function fieldErrorMessage(field, message) {
  if (!message) return field || ''
  if (!field) return message
  return `${humanizeField(field)}: ${message}`
}

function humanizeField(field) {
  return String(field)
    .replace(/\[(\d+)\]/g, (_, index) => ` ${Number(index) + 1}`)
    .replace(/\./g, ' ')
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/[_-]+/g, ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

export default apiClient
