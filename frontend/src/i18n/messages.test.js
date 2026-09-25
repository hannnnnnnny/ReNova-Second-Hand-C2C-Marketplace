import { describe, expect, it } from 'vitest'
import { messages, AVAILABLE_LOCALES, DEFAULT_LOCALE } from './messages'
import { categoryLabel } from './marketplace-ui'

function leafKeys(bundle, prefix = '') {
  return Object.entries(bundle).flatMap(([key, value]) => {
    const path = prefix ? `${prefix}.${key}` : key
    return value && typeof value === 'object' ? leafKeys(value, path) : [path]
  }).sort()
}

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

  it('translates every nested label in both languages', () => {
    expect(leafKeys(messages.zh)).toEqual(leafKeys(messages.en))
  })

  it('keeps an unknown category readable while translating known categories', () => {
    const labels = messages.zh.marketplaceUi.categoryNames
    const t = (key) => labels[key.split('.').pop()]
    const te = (key) => Boolean(t(key))
    expect(categoryLabel({ slug: 'electronics', name: 'Electronics' }, t, te)).toBe('数码电子')
    expect(categoryLabel({ slug: 'new-category', name: 'Art supplies' }, t, te)).toBe('Art supplies')
  })
})
