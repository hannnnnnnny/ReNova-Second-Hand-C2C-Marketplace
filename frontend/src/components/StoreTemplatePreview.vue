<template>
  <article class="store-template-preview" :style="{ '--store-accent': store.brandColor }">
    <div class="store-preview-announcement">{{ store.announcement || store.shippingMessage }}</div>
    <header>
      <span>{{ store.logoText }}</span>
      <strong>{{ store.name }}</strong>
    </header>
    <section>
      <div>
        <p class="eyebrow">{{ template.name }}</p>
        <h3>{{ store.heroTitle }}</h3>
        <p>{{ store.heroText }}</p>
      </div>
      <img :src="store.heroImage || template.previewImage" :alt="`${template.name} storefront preview`" loading="lazy" decoding="async" />
    </section>
    <div class="preview-product-row">
      <span v-for="product in store.products.slice(0, 3)" :key="product.id">{{ product.name }}</span>
    </div>
  </article>
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
