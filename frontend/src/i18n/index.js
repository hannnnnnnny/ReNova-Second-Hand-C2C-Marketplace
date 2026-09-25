import { createI18n } from 'vue-i18n'
import { messages, DEFAULT_LOCALE, AVAILABLE_LOCALES } from './messages'
import { readStorageItem, writeStorageItem } from '../utils/browserStorage'

const LOCALE_KEY = 'renova.locale'

// English is the default for every visitor, regardless of browser language;
// only an explicit choice from the language switcher is remembered.
export function pickInitialLocale(stored) {
  if (stored && AVAILABLE_LOCALES.some((l) => l.code === stored)) return stored
  return DEFAULT_LOCALE
}

function detectInitialLocale() {
  if (typeof window === 'undefined') return DEFAULT_LOCALE
  return pickInitialLocale(readStorageItem(LOCALE_KEY))
}

export const i18n = createI18n({
  legacy: false,
  locale: detectInitialLocale(),
  fallbackLocale: 'en',
  messages,
  warnHtmlMessage: false,
  missingWarn: false,
  fallbackWarn: false
})

export function setLocale(code) {
  if (!AVAILABLE_LOCALES.some((l) => l.code === code)) return
  i18n.global.locale.value = code
  if (typeof window !== 'undefined') {
    writeStorageItem(LOCALE_KEY, code)
    document.documentElement.setAttribute('lang', code)
  }
}

if (typeof document !== 'undefined') {
  document.documentElement.setAttribute('lang', i18n.global.locale.value)
}

export { AVAILABLE_LOCALES }
