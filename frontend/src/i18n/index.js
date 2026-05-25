import { createI18n } from 'vue-i18n'
import { messages, DEFAULT_LOCALE, AVAILABLE_LOCALES } from './messages'

const LOCALE_KEY = 'renova.locale'

function detectInitialLocale() {
  if (typeof window === 'undefined') return DEFAULT_LOCALE
  const stored = window.localStorage.getItem(LOCALE_KEY)
  if (stored && AVAILABLE_LOCALES.some((l) => l.code === stored)) return stored
  const browser = (navigator.language || '').toLowerCase()
  if (browser.startsWith('zh')) return 'zh'
  return DEFAULT_LOCALE
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
    window.localStorage.setItem(LOCALE_KEY, code)
    document.documentElement.setAttribute('lang', code)
  }
}

if (typeof document !== 'undefined') {
  document.documentElement.setAttribute('lang', i18n.global.locale.value)
}

export { AVAILABLE_LOCALES }
