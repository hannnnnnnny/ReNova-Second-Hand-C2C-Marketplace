import { describe, expect, it } from 'vitest'
import { messages, AVAILABLE_LOCALES, DEFAULT_LOCALE } from './messages'

describe('i18n bundles', () => {
  it('exposes en and zh', () => {
    expect(Object.keys(messages).sort()).toEqual(['en', 'zh'])
  })

  it('lists locales for the switcher', () => {
    expect(AVAILABLE_LOCALES.map((l) => l.code).sort()).toEqual(['en', 'zh'])
  })

  it('declares en as the default', () => {
    expect(DEFAULT_LOCALE).toBe('en')
  })

  it('has matching top-level keys across locales', () => {
    const enKeys = Object.keys(messages.en).sort()
    const zhKeys = Object.keys(messages.zh).sort()
    expect(zhKeys).toEqual(enKeys)
  })
})
