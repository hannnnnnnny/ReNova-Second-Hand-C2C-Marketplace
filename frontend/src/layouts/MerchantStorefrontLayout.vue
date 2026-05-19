<template>
  <div v-if="store" class="generated-store-shell" :style="{ '--store-accent': store.brandColor }" :data-template="store.template">
    <a class="skip-link" href="#merchant-store-content">Skip to content</a>
    <div class="store-announcement">{{ store.announcement || store.shippingMessage }}</div>
    <StorefrontHeader :store="store" />
    <main id="merchant-store-content" tabindex="-1">
      <RouterView :store="store" />
    </main>
    <footer class="generated-store-footer">
      <strong>{{ store.name }}</strong>
      <span>{{ store.description }}</span>
      <RouterLink :to="`/store/${store.slug}/support`">Support and refunds</RouterLink>
      <RouterLink :to="`/admin/dashboard`">Merchant admin</RouterLink>
    </footer>
  </div>
  <main v-else class="platform-page-hero">
    <p class="eyebrow">Store not found</p>
    <h1>This merchant storefront is not available</h1>
    <p>Choose another demo storefront or create a new one through NovaCart onboarding.</p>
    <RouterLink class="primary-button" to="/templates">View templates</RouterLink>
  </main>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import StorefrontHeader from '../components/StorefrontHeader.vue'
import { getTemplateById } from '../data/platform'
import { usePlatformStore } from '../stores/platform'
import { useStorefrontCartStore } from '../stores/storefrontCart'

const route = useRoute()
const platformStore = usePlatformStore()
const cartStore = useStorefrontCartStore()
const baseStore = computed(() => platformStore.getStore(String(route.params.storeSlug || '')))
const store = computed(() => {
  if (!baseStore.value) return null
  const templatePreview = route.query.templatePreview ? String(route.query.templatePreview) : ''
  if (!templatePreview) return baseStore.value
  const template = getTemplateById(templatePreview)
  if (template.id !== templatePreview) return baseStore.value
  return {
    ...baseStore.value,
    template: template.id,
    brandColor: template.accentColor,
    heroImage: template.previewImage
  }
})

onMounted(() => {
  platformStore.loadPlatform()
  cartStore.loadCarts()
  syncCurrentStore()
})

watch(() => route.params.storeSlug, syncCurrentStore)

function syncCurrentStore() {
  if (route.params.storeSlug && platformStore.getStore(String(route.params.storeSlug))) {
    platformStore.setCurrentStore(String(route.params.storeSlug))
  }
}
</script>
