<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'

const { t } = useI18n()
const auth = useAuthStore()
const router = useRouter()
const toast = useToastStore()

const form = ref({ email: '', displayName: '', password: '', location: '' })
const submitting = ref(false)

async function submit() {
  submitting.value = true
  try {
    await auth.signup({ ...form.value, email: form.value.email.trim() })
    toast.success(t('auth.signUp'))
    router.push({ name: 'home' })
  } catch (err) { toast.error(apiError(err)) } finally { submitting.value = false }
}
</script>

<template>
  <main class="auth-shell">
    <div class="auth-card">
      <h1 style="margin-bottom: 8px">{{ t('auth.signUp') }}</h1>
      <p class="muted" style="margin-bottom: 24px">{{ t('auth.signUpSubtitle') }}</p>
      <form @submit.prevent="submit">
        <div class="field">
          <label class="label">{{ t('auth.displayName') }}</label>
          <input class="input" v-model="form.displayName" required minlength="2" maxlength="80" />
        </div>
        <div class="field">
          <label class="label">{{ t('auth.email') }}</label>
          <input class="input" type="email" v-model="form.email" required />
        </div>
        <div class="field">
          <label class="label">{{ t('auth.password') }}</label>
          <input class="input" type="password" v-model="form.password" required minlength="8" />
          <span class="help">{{ t('auth.passwordHint') }}</span>
        </div>
        <div class="field">
          <label class="label">{{ t('auth.location') }}</label>
          <input class="input" v-model="form.location" maxlength="120" />
        </div>
        <button class="btn btn-primary btn-lg btn-block" :disabled="submitting" type="submit">{{ submitting ? t('common.loading') : t('common.signup') }}</button>
      </form>
      <p class="text-center muted" style="margin-top: 20px; font-size: 13px">{{ t('auth.hasAccount') }} <RouterLink :to="{ name: 'login' }" class="bold" style="color: var(--primary-strong)">{{ t('auth.signInLink') }}</RouterLink></p>
    </div>
  </main>
</template>
