<template>
  <RouterLink
    class="store-template-preview"
    :to="{ name: 'platform-template-detail', params: { templateId: store.slug } }"
    :style="{ '--store-accent': store.brandColor }"
    :aria-label="`Open ${store.name} page previews`"
  >
    <div class="store-preview-window">
      <div class="store-preview-browser-bar" aria-hidden="true">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <section class="store-preview-hero-shot">
        <img :src="store.heroImage || template.previewImage" :alt="`${store.name} homepage preview`" loading="lazy" decoding="async" />
        <div class="store-preview-copy">
          <span>{{ template.name }}</span>
          <strong>{{ store.heroTitle }}</strong>
          <small>{{ store.shippingMessage }}</small>
        </div>
      </section>
      <div class="store-preview-mini-products" aria-label="Featured products preview">
        <span v-for="product in store.products.slice(0, 3)" :key="product.id">
          <img :src="product.imageUrl" :alt="product.name" loading="lazy" decoding="async" />
        </span>
      </div>
    </div>
    <footer class="store-preview-footer">
      <div>
        <p class="eyebrow">{{ store.category }}</p>
        <h3>{{ store.name }}</h3>
        <p>{{ store.description }}</p>
      </div>
      <span class="preview-open-label">View page previews</span>
    </footer>
    <div class="preview-product-row">
      <span v-for="product in store.products.slice(0, 3)" :key="product.id">{{ product.name }}</span>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'
import { getTemplateById } from '../data/platform'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const template = computed(() => getTemplateById(props.store.template))
</script>
