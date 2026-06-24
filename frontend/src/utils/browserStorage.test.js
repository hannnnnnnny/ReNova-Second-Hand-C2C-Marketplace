import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  readStorageItem,
  readStorageJson,
  removeStorageItem,
  writeStorageItem,
  writeStorageJson
} from './browserStorage'

afterEach(() => {
  vi.unstubAllGlobals()
})

function installStorage(initial = {}) {
  const store = { ...initial }
  const storage = {
    getItem: vi.fn((key) => store[key] ?? null),
    setItem: vi.fn((key, value) => {
      store[key] = String(value)
    }),
    removeItem: vi.fn((key) => {
      delete store[key]
    })
  }
  vi.stubGlobal('localStorage', storage)
  return { storage, store }
}

describe('browserStorage', () => {
  it('reads, writes, and removes plain values', () => {
    const { storage } = installStorage()

    writeStorageItem('renova.locale', 'zh')
    expect(storage.setItem).toHaveBeenCalledWith('renova.locale', 'zh')
    expect(readStorageItem('renova.locale')).toBe('zh')

    removeStorageItem('renova.locale')
    expect(storage.removeItem).toHaveBeenCalledWith('renova.locale')
    expect(readStorageItem('renova.locale')).toBe('')
  })

  it('serializes JSON values and removes empty values', () => {
    const { store, storage } = installStorage()

    writeStorageJson('renova.user', { id: 1, displayName: 'Ava' })
    expect(JSON.parse(store['renova.user'])).toEqual({ id: 1, displayName: 'Ava' })
    expect(readStorageJson('renova.user')).toEqual({ id: 1, displayName: 'Ava' })

    writeStorageJson('renova.user', null)
    expect(storage.removeItem).toHaveBeenCalledWith('renova.user')
  })

  it('clears malformed JSON instead of leaking a broken session', () => {
    const { storage } = installStorage({ 'renova.user': '{bad-json' })

    expect(readStorageJson('renova.user')).toBeNull()
    expect(storage.removeItem).toHaveBeenCalledWith('renova.user')
  })

  it('treats blocked browser storage as best effort', () => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => { throw new Error('blocked') }),
      setItem: vi.fn(() => { throw new Error('blocked') }),
      removeItem: vi.fn(() => { throw new Error('blocked') })
    })

    expect(readStorageItem('anything')).toBe('')
    expect(readStorageJson('anything')).toBeNull()
    expect(() => writeStorageItem('anything', 'value')).not.toThrow()
    expect(() => removeStorageItem('anything')).not.toThrow()
  })
})
