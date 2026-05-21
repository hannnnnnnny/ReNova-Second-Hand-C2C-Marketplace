<template>
  <OnboardingLayout :steps="steps" :current-step="currentStep">
    <form class="onboarding-form" @submit.prevent="nextStep">
      <StoreBasicsStep v-if="currentStep === 0" :model="form" />
      <TemplateSelectStep v-else-if="currentStep === 1" :model="form" :templates="storeTemplates" />
      <FirstProductsStep v-else-if="currentStep === 2" :model="form" />
      <StoreConfigStep v-else-if="currentStep === 3" :model="form" />
      <LaunchStep v-else :model="form" :templates="storeTemplates" />

      <p v-if="error" class="field-error">{{ error }}</p>
      <div class="onboarding-actions">
        <button class="secondary-button" type="button" :disabled="currentStep === 0 || submitting" @click="currentStep -= 1">Back</button>
        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? 'Creating...' : currentStep === steps.length - 1 ? 'Create store' : 'Continue' }}
        </button>
      </div>
    </form>
  </OnboardingLayout>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FirstProductsStep from '../../components/FirstProductsStep.vue'
import LaunchStep from '../../components/LaunchStep.vue'
import OnboardingLayout from '../../components/OnboardingLayout.vue'
import StoreBasicsStep from '../../components/StoreBasicsStep.vue'
import StoreConfigStep from '../../components/StoreConfigStep.vue'
import TemplateSelectStep from '../../components/TemplateSelectStep.vue'
import { createSlug, storeTemplates } from '../../data/platform'
import { useAuthStore } from '../../stores/auth'
import { usePlatformStore } from '../../stores/platform'
import { LOCAL_DEMO_ADMIN_TOKEN } from '../../utils/demoAdmin'
import { publicAsset } from '../../utils/publicPath'

const route = useRoute()
const router = useRouter()
const platformStore = usePlatformStore()
const authStore = useAuthStore()
const steps = ['Store basics', 'Choose template', 'Add products', 'Configure store', 'Finish']
const currentStep = ref(0)
const error = ref('')
const submitting = ref(false)
const form = reactive({
  name: '',
  slug: '',
  category: 'Fashion',
  description: '',
  template: 'fashion',
  products: [
    { name: 'Signature launch product', price: 68, category: 'New Arrivals', imageUrl: publicAsset('demo-images/products/boutique-shirt.jpg') }
  ],
  logoText: 'NS',
  brandColor: '#6f4f45',
  currency: 'USD',
  shippingMessage: 'Free shipping on orders over $75'
})

onMounted(() => {
  platformStore.loadPlatform()
  if (route.query.template && storeTemplates.some((template) => template.id === route.query.template)) {
    form.template = String(route.query.template)
  }
})

watch(
  () => form.name,
  (value) => {
    if (!form.slug) form.slug = createSlug(value)
    if (value && form.logoText === 'NS') {
      form.logoText = value.split(/\s+/).slice(0, 2).map((part) => part[0]?.toUpperCase()).join('').slice(0, 3)
    }
  }
)

function nextStep() {
  if (submitting.value) return
  error.value = ''
  if (currentStep.value === 0 && (!form.name || !form.slug)) {
    error.value = 'Store name and slug are required.'
    return
  }
  if (currentStep.value === 2 && !form.products.some((product) => product.name && Number(product.price) > 0)) {
    error.value = 'Add at least one product with a valid price.'
    return
  }
  if (currentStep.value < steps.length - 1) {
    currentStep.value += 1
    return
  }
  submitting.value = true
  try {
    const createdStore = platformStore.createStore({
      ...form,
      products: form.products.filter((product) => product.name && Number(product.price) > 0)
    })
    authStore.setSession({
      token: LOCAL_DEMO_ADMIN_TOKEN,
      email: 'admin@novacart.local',
      role: 'ROLE_ADMIN',
      expiresInMinutes: 120
    })
    router.push('/admin/store-content')
  } finally {
    window.setTimeout(() => {
      submitting.value = false
    }, 500)
  }
}
</script>
