<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import AuthLayout from '../components/AuthLayout.vue'
import PasswordField from '../components/PasswordField.vue'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { authErrorKey, safeAuthRedirect } from '../utils/auth'

const { t } = useI18n()
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToastStore()
const form = ref({ email: '', displayName: '', password: '', location: '' })
const submitting = ref(false)
const errorMessage = ref('')

async function submit() {
  if (submitting.value) return
  submitting.value = true
  errorMessage.value = ''
  try {
    await auth.signup({ ...form.value, email: form.value.email.trim() })
    toast.success(t('auth.signUp'))
    await router.push(safeAuthRedirect(route.query.redirect))
  } catch (error) {
    errorMessage.value = t(`authUi.${authErrorKey(error)}`)
  } finally {
    submitting.value = false
  }
}

const loginLink = computed(() => ({
  name: 'login',
  query: route.query.redirect ? { redirect: safeAuthRedirect(route.query.redirect) } : {}
}))
</script>

<template>
  <AuthLayout :title="t('auth.signUp')" :subtitle="t('auth.signUpSubtitle')" :note="t('authUi.signupNote')">
    <form class="account-form" :aria-describedby="errorMessage ? 'auth-error' : undefined" @submit.prevent="submit">
      <div class="account-field">
        <label for="signup-name">{{ t('auth.displayName') }}</label>
        <input id="signup-name" v-model="form.displayName" name="displayName" autocomplete="name" required minlength="2" maxlength="80" />
      </div>
      <div class="account-field">
        <label for="signup-email">{{ t('auth.email') }}</label>
        <input id="signup-email" v-model="form.email" name="email" type="email" autocomplete="email" required maxlength="180" />
      </div>
      <PasswordField id="signup-password" v-model="form.password" :label="t('auth.password')" autocomplete="new-password" :help="t('auth.passwordHint')" :minlength="8" :maxlength="80" />
      <div class="account-field">
        <label for="signup-location">{{ t('auth.location') }}</label>
        <input id="signup-location" v-model="form.location" name="location" autocomplete="address-level2" maxlength="120" />
      </div>
      <p v-if="errorMessage" id="auth-error" class="account-error" role="alert">{{ errorMessage }}</p>
      <button class="account-submit" :disabled="submitting" type="submit">{{ submitting ? t('authUi.creatingAccount') : t('common.signup') }}</button>
    </form>
    <p class="account-switch">{{ t('auth.hasAccount') }} <RouterLink :to="loginLink">{{ t('auth.signInLink') }}</RouterLink></p>
  </AuthLayout>
</template>

