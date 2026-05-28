import { afterEach, describe, expect, it, vi } from 'vitest'
import apiClient, {
  TOKEN_KEY,
  apiError,
  clearStoredAuthToken,
  getStoredAuthToken,
  unwrap
} from './client'

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

describe('api client', () => {
  it('unwraps standard API envelopes without hiding raw responses', () => {
    expect(unwrap({ data: { data: { id: 1 } } })).toEqual({ id: 1 })
    expect(unwrap({ data: { success: true } })).toEqual({ success: true })
  })

  it('normalizes validation errors into readable field messages', () => {
    expect(apiError({
      response: {
        data: {
          errors: [
            { field: 'shippingAddress', message: 'must not be blank' },
            { field: 'imageUrls[0]', message: 'must be a valid URL' }
          ]
        }
      }
    })).toBe('Shipping Address: must not be blank; Image Urls 1: must be a valid URL')
  })

  it('supports object-shaped validation errors and API messages', () => {
    expect(apiError({
      response: { data: { errors: { email: 'must be valid' } } }
    })).toBe('Email: must be valid')

    expect(apiError({
      response: { data: { message: 'Listing not found.' } }
    })).toBe('Listing not found.')
  })

  it('uses an actionable network error when the backend cannot be reached', () => {
    expect(apiError(new Error('Network Error'))).toBe('Cannot reach the server. Check that the backend is running.')
  })

  it('keeps token storage behind one API boundary', async () => {
    const { storage } = installStorage({ [TOKEN_KEY]: 'token-123' })

    expect(getStoredAuthToken()).toBe('token-123')
    const requestHandler = apiClient.interceptors.request.handlers[0].fulfilled
    const config = await requestHandler({ headers: {} })
    expect(config.headers.Authorization).toBe('Bearer token-123')

    clearStoredAuthToken()
    expect(storage.removeItem).toHaveBeenCalledWith(TOKEN_KEY)
  })

  it('clears stale auth tokens after 401 responses', async () => {
    const { storage } = installStorage({ [TOKEN_KEY]: 'expired-token' })
    const responseHandler = apiClient.interceptors.response.handlers[0].rejected

    await expect(responseHandler({ response: { status: 401, data: {} } })).rejects.toEqual({
      response: { status: 401, data: {} }
    })

    expect(storage.removeItem).toHaveBeenCalledWith(TOKEN_KEY)
  })
})
