export function readStorageItem(key) {
  try {
    return globalThis.localStorage?.getItem(key) || ''
  } catch {
    return ''
  }
}

export function writeStorageItem(key, value) {
  try {
    globalThis.localStorage?.setItem(key, value)
  } catch {
    // Storage can be unavailable in private browsing, SSR, or tests.
  }
}

export function removeStorageItem(key) {
  try {
    globalThis.localStorage?.removeItem(key)
  } catch {
    // Treat storage removal as best-effort.
  }
}

export function readStorageJson(key) {
  const raw = readStorageItem(key)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    removeStorageItem(key)
    return null
  }
}

export function writeStorageJson(key, value) {
  if (value === null || value === undefined) {
    removeStorageItem(key)
    return
  }
  writeStorageItem(key, JSON.stringify(value))
}
