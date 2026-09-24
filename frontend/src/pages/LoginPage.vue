<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import AuthLayout from '../components/AuthLayout.vue'
import PasswordField from '../components/PasswordField.vue'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { DEMO_MODE } from '../api/demo'
import { authErrorKey, safeAuthRedirect } from '../utils/auth'

const { t } = useI18n()
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToastStore()
const email = ref('')
const password = ref('')
const submitting = ref(false)
const errorMessage = ref('')

async function submit() {
  if (submitting.value) return
  submitting.value = true
  errorMessage.value = ''
  try {
    await auth.login({ email: email.value.trim(), password: password.value })
    toast.success(t('auth.signIn'))
    await router.push(safeAuthRedirect(route.query.redirect))
  } catch (error) {
    errorMessage.value = t(`authUi.${authErrorKey(error)}`)
  } finally {
    submitting.value = false
  }
}

function tryDemo(account) {
  email.value = account
  password.value = 'DemoPassword1!'
  errorMessage.value = ''
}

const signupLink = computed(() => ({
  name: 'signup',
  query: route.query.redirect ? { redirect: safeAuthRedirect(route.query.redirect) } : {}
}))
</script>

<template>
  <AuthLayout :title="t('auth.signIn')" :subtitle="t('auth.signInSubtitle')" :note="t('authUi.loginNote')">
    <form class="account-form" :aria-describedby="errorMessage ? 'auth-error' : undefined" @submit.prevent="submit">
      <div class="account-field">
        <label for="login-email">{{ t('auth.email') }}</label>
        <input id="login-email" v-model="email" name="email" type="email" autocomplete="email" required />
      </div>
      <PasswordField id="login-password" v-model="password" :label="t('auth.password')" autocomplete="current-password" />
      <p v-if="errorMessage" id="auth-error" class="account-error" role="alert">{{ errorMessage }}</p>
      <button class="account-submit" :disabled="submitting" type="submit">
        {{ submitting ? t('authUi.signingIn') : t('common.login') }}
      </button>
    </form>
    <section v-if="DEMO_MODE" class="demo-block" aria-labelledby="demo-title">
      <h2 id="demo-title">{{ t('authUi.demoTitle') }}</h2>
      <p>{{ t('authUi.demoHint') }}</p>
      <div class="demo-actions">
        <button type="button" @click="tryDemo('ava@renova.local')">{{ t('authUi.useDemoAccount', { name: 'Ava' }) }}</button>
        <button type="button" @click="tryDemo('liam@renova.local')">{{ t('authUi.useDemoAccount', { name: 'Liam' }) }}</button>
      </div>
    </section>
    <p class="account-switch">{{ t('auth.noAccount') }} <RouterLink :to="signupLink">{{ t('auth.createAccount') }}</RouterLink></p>
  </AuthLayout>
</template>

<style scoped>
.account-form { display: grid; }
.account-field { display: grid; gap: 7px; margin-bottom: 17px; }
.account-field label { color: var(--text); font-size: 13px; font-weight: 750; }
.account-field input { width: 100%; min-width: 0; height: 46px; padding: 0 13px; border: 1px solid var(--border-strong); border-radius: 11px; background: var(--bg-elevated); color: var(--text); font: inherit; }
.account-field input:focus { outline: 3px solid var(--primary-soft); border-color: var(--primary); }
.account-error { margin: 0 0 16px; padding: 11px 13px; border: 1px solid #c96755; border-radius: 10px; background: #fff1ed; color: #8e3022; font-size: 13px; line-height: 1.45; }
.account-submit { min-height: 46px; border: 1px solid var(--primary-strong); border-radius: 11px; background: var(--primary); color: white; font: inherit; font-weight: 800; cursor: pointer; }
.account-submit:hover:not(:disabled) { background: var(--primary-strong); }
.account-submit:focus-visible { outline: 3px solid var(--primary-soft); outline-offset: 2px; }
.account-submit:disabled { cursor: wait; opacity: .65; }
.demo-block { margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--border); }
.demo-block h2 { margin: 0 0 5px; font-size: 14px; }
.demo-block p, .account-switch { color: var(--text-soft); font-size: 12px; line-height: 1.5; }
.demo-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.demo-actions button { padding: 8px 11px; border: 1px solid var(--border); border-radius: 9px; background: var(--bg); color: var(--text); cursor: pointer; font: inherit; font-size: 12px; }
.demo-actions button:hover { border-color: var(--primary); color: var(--primary-strong); }
.account-switch { margin: 22px 0 0; text-align: center; }
.account-switch a { color: var(--primary-strong); font-weight: 800; }
@media (prefers-reduced-motion: no-preference) { .account-submit, .demo-actions button { transition: background .16s ease, border-color .16s ease, color .16s ease; } }
</style>
