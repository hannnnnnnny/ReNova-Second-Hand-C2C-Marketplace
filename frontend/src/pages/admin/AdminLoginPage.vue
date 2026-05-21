<template>
  <main class="login-page">
    <section class="login-brand-panel">
      <div class="login-mark">
        <ShieldCheck aria-hidden="true" />
      </div>
      <p class="eyebrow">Merchant workspace</p>
      <h1>Sign in to manage your NovaCart store.</h1>
      <p>Choose a store, customize the storefront, manage products and orders, and review analytics from one workspace.</p>
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
      <p class="login-hint">Local demo account: admin@novacart.local</p>
      <RouterLink class="text-link" to="/signup">Create a merchant workspace</RouterLink>
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
import { LOCAL_DEMO_ADMIN_TOKEN } from '../../utils/demoAdmin'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)
const error = ref('')
const showPassword = ref(false)
const form = reactive({ email: '', password: '' })
const demoCredentials = {
  email: 'admin@novacart.local',
  password: 'NovaCartAdmin123!'
}
const noticeMessage = computed(() => {
  if (error.value) return error.value
  if (route.query.session === 'expired') {
    return 'Your session expired. Please sign in again.'
  }
  return ''
})

async function submitLogin() {
  if (submitting.value) return
  error.value = ''
  submitting.value = true
  try {
    const session = await loginAdmin(form)
    authStore.setSession(session)
    router.push(route.query.redirect || '/admin/dashboard')
  } catch (requestError) {
    if (canUseLocalDemoSession(requestError)) {
      authStore.setSession({
        token: LOCAL_DEMO_ADMIN_TOKEN,
        email: demoCredentials.email,
        role: 'ROLE_ADMIN',
        expiresInMinutes: 120
      })
      router.push(route.query.redirect || '/admin/dashboard')
      return
    }
    error.value = getApiError(requestError, 'Sign in failed.')
  } finally {
    submitting.value = false
  }
}

function canUseLocalDemoSession(requestError) {
  if (form.email !== demoCredentials.email || form.password !== demoCredentials.password) return false
  const status = requestError.response?.status
  return !requestError.response || status === 404 || status >= 500
}
</script>
