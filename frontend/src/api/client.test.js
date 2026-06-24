import { describe, expect, it } from 'vitest'
import apiClient, { apiError, unwrap } from './client'


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

  it('uses credentialed cookies and the standard CSRF cookie/header pair', () => {
    expect(apiClient.defaults.withCredentials).toBe(true)
    expect(apiClient.defaults.withXSRFToken).toBe(true)
    expect(apiClient.defaults.xsrfCookieName).toBe('XSRF-TOKEN')
    expect(apiClient.defaults.xsrfHeaderName).toBe('X-XSRF-TOKEN')
    expect(apiClient.interceptors.request.handlers).toHaveLength(0)
  })
})
