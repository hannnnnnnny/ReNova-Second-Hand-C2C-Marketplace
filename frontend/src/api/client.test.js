import { describe, expect, it } from 'vitest'
import { getApiError } from './client'

describe('API error formatting', () => {
  it('formats field validation arrays into readable messages', () => {
    const error = {
      response: {
        data: {
          errors: [
            { field: 'customerEmail', message: 'Enter a valid email address.' },
            { field: 'items[0].quantity', message: 'Quantity must be at least 1.' }
          ]
        }
      }
    }

    expect(getApiError(error)).toBe(
      'Customer Email: Enter a valid email address. Items 1 Quantity: Quantity must be at least 1.'
    )
  })

  it('formats object validation errors into readable messages', () => {
    const error = {
      response: {
        data: {
          errors: {
            shippingAddress: 'Shipping address is required.'
          }
        }
      }
    }

    expect(getApiError(error)).toBe('Shipping Address: Shipping address is required.')
  })

  it('uses API messages before local fallbacks', () => {
    const error = {
      response: {
        data: {
          message: 'Insufficient stock for Silk Wrap Blouse.'
        }
      }
    }

    expect(getApiError(error, 'Order could not be placed.')).toBe('Insufficient stock for Silk Wrap Blouse.')
  })

  it('uses a friendly fallback when the backend response has no message', () => {
    const error = {
      message: 'Request failed with status code 500',
      response: {
        data: {}
      }
    }

    expect(getApiError(error, 'Order could not be placed.')).toBe('Order could not be placed.')
  })

  it('uses an unavailable-server message for network failures', () => {
    const error = {
      message: 'Network Error'
    }

    expect(getApiError(error)).toBe('The server is unavailable. Check that the backend is running and try again.')
  })
})
