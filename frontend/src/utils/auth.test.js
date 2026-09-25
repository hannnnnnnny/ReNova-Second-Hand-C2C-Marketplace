import { describe, expect, it } from 'vitest'
import { authErrorKey, safeAuthRedirect } from './auth'

describe('safeAuthRedirect', () => {
  it('keeps local application paths including their query and hash', () => {
    expect(safeAuthRedirect('/checkout/42?offer=7#payment')).toBe('/checkout/42?offer=7#payment')
  })

  it.each([
    ['https://evil.example/steal', '/'],
    ['//evil.example/steal', '/'],
    ['/\\evil.example/steal', '/'],
    ['javascript:alert(1)', '/'],
    ['login', '/'],
    ['', '/']
  ])('rejects unsafe redirect %s', (redirect, expected) => {
    expect(safeAuthRedirect(redirect)).toBe(expected)
  })

  it('uses the first value when Vue Router supplies an array', () => {
    expect(safeAuthRedirect(['/orders', 'https://evil.example'])).toBe('/orders')
  })
})

describe('authErrorKey', () => {
  it.each([
    [undefined, 'networkError'],
    [401, 'invalidCredentials'],
    [409, 'emailInUse'],
    [422, 'invalidDetails'],
    [500, 'genericError']
  ])('maps status %s to localized copy', (status, expected) => {
    const error = status ? { response: { status } } : new Error('network')
    expect(authErrorKey(error)).toBe(expected)
  })

  it.each([
    ['email', 'invalidEmail'],
    ['displayName', 'invalidDisplayName'],
    ['password', 'invalidPassword'],
    ['location', 'invalidLocation']
  ])('maps backend validation for %s to actionable localized copy', (field, expected) => {
    const error = { response: { status: 400, data: { errors: [{ field, message: 'backend text' }] } } }
    expect(authErrorKey(error)).toBe(expected)
  })

  it('falls back safely for an unknown backend validation field', () => {
    const error = { response: { status: 400, data: { errors: [{ field: 'unknown', message: 'backend text' }] } } }
    expect(authErrorKey(error)).toBe('invalidDetails')
  })
})
