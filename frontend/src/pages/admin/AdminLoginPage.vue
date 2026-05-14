<template>
  <main class="login-page">
    <section class="login-brand-panel">
      <div class="login-mark">
        <ShieldCheck aria-hidden="true" />
      </div>
      <p class="eyebrow">Merchant workspace</p>
      <h1>Manage NovaCart with a focused admin console.</h1>
      <p>Review catalog health, keep inventory visible, and move orders through fulfillment.</p>
    </section>
    <form class="login-panel" @submit.prevent="submitLogin">
      <div>
        <p class="eyebrow">Secure Access</p>
        <h2>Sign in</h2>
      </div>
      <ErrorMessage v-if="noticeMessage" :message="noticeMessage" />
      <label>Email<input v-model.trim="form.email" required type="email" placeholder="admin@novacart.local" /></label>
      <label>
        Password
        <div class="password-field">
          <input
            v-model="form.password"
            required
            :type="showPassword ? 'text' : 'password'"
            placeholder="Enter your password"
          />
          <button type="button" @click="showPassword = !showPassword">
            {{ showPassword ? 'Hide' : 'Show' }}
          </button>
        </div>
      </label>
      <button class="primary-button" type="submit" :disabled="submitting">
        {{ submitting ? 'Signing In...' : 'Sign In' }}
      </button>
      <p class="login-hint">Local account: admin@novacart.local</p>
    </form>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ShieldCheck } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'
import { loginAdmin } from '../../api/admin'
import { getApiError } from '../../api/client'
import ErrorMessage from '../../components/ErrorMessage.vue'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)
const error = ref('')
const showPassword = ref(false)
const form = reactive({ email: '', password: '' })
const noticeMessage = computed(() => {
  if (error.value) return error.value
  if (route.query.session === 'expired') {
    return 'Your session expired. Please sign in again.'
  }
  return ''
})

async function submitLogin() {
  error.value = ''
  submitting.value = true
  try {
    const session = await loginAdmin(form)
    authStore.setSession(session)
    router.push(route.query.redirect || '/admin/dashboard')
  } catch (requestError) {
    error.value = getApiError(requestError, 'Sign in failed.')
  } finally {
    submitting.value = false
  }
}
</script>
