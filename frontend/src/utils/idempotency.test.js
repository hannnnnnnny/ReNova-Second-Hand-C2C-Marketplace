import { describe, expect, it } from 'vitest'
import { createIdempotencyKey } from './idempotency'

describe('createIdempotencyKey', () => {
  it('uses the platform UUID generator when available', () => {
    expect(createIdempotencyKey({ randomUUID: () => '6ba7b810-9dad-41d1-80b4-00c04fd430c8' }))
      .toBe('6ba7b810-9dad-41d1-80b4-00c04fd430c8')
  })

  it('creates an RFC 4122 version 4 UUID from secure random bytes', () => {
    const cryptoApi = {
      getRandomValues: (bytes) => {
        bytes.set(Array.from({ length: 16 }, (_, index) => index))
        return bytes
      }
    }
    expect(createIdempotencyKey(cryptoApi)).toBe('00010203-0405-4607-8809-0a0b0c0d0e0f')
  })

  it('fails instead of using an insecure random fallback', () => {
    expect(() => createIdempotencyKey({})).toThrow('Secure random number generation is unavailable.')
  })
})
