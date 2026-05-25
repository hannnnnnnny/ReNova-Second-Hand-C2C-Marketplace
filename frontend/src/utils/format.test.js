import { describe, expect, it } from 'vitest'
import { formatPrice, initials, avatarBackground } from './format'

describe('formatPrice', () => {
  it('renders USD currency', () => {
    expect(formatPrice(12.5)).toMatch(/\$?12\.50/)
  })

  it('returns empty string for null', () => {
    expect(formatPrice(null)).toBe('')
    expect(formatPrice(undefined)).toBe('')
    expect(formatPrice('')).toBe('')
  })

  it('passes non-numeric values through', () => {
    expect(formatPrice('not-a-number')).toBe('not-a-number')
  })
})

describe('initials', () => {
  it('returns first letters of up to two words', () => {
    expect(initials('Ava Chen')).toBe('AC')
    expect(initials('Liam')).toBe('L')
    expect(initials('Nora Singh Kapoor')).toBe('NS')
  })

  it('handles empty input', () => {
    expect(initials('')).toBe('·')
    expect(initials(null)).toBe('·')
  })
})

describe('avatarBackground', () => {
  it('is deterministic for the same name', () => {
    expect(avatarBackground('Ava Chen')).toBe(avatarBackground('Ava Chen'))
  })

  it('always returns a hex color', () => {
    expect(avatarBackground('anything')).toMatch(/^#[0-9a-f]{6}$/i)
  })
})
