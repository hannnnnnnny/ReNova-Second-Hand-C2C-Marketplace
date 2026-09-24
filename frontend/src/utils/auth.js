const AUTH_ERROR_KEYS = {
  400: 'invalidDetails',
  401: 'invalidCredentials',
  409: 'emailInUse',
  422: 'invalidDetails'
}

const FIELD_ERROR_KEYS = {
  email: 'invalidEmail',
  displayName: 'invalidDisplayName',
  password: 'invalidPassword',
  location: 'invalidLocation'
}

export function safeAuthRedirect(value, fallback = '/') {
  const redirect = Array.isArray(value) ? value[0] : value
  if (typeof redirect !== 'string' || !redirect.startsWith('/') || /^\/[\\/]/.test(redirect)) {
    return fallback
  }
  return redirect
}

export function authErrorKey(error) {
  if (!error?.response) return 'networkError'
  const field = error.response.data?.errors?.[0]?.field
  if (FIELD_ERROR_KEYS[field]) return FIELD_ERROR_KEYS[field]
  return AUTH_ERROR_KEYS[error.response.status] || 'genericError'
}
