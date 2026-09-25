import { describe, expect, it } from 'vitest'
import { pickInitialLocale } from './index'

describe('pickInitialLocale', () => {
  it('defaults to English when nothing was chosen', () => {
    expect(pickInitialLocale('')).toBe('en')
  })

  it('keeps a language the visitor chose earlier', () => {
    expect(pickInitialLocale('zh')).toBe('zh')
  })

  it('ignores an unknown stored value', () => {
    expect(pickInitialLocale('fr')).toBe('en')
  })
})
