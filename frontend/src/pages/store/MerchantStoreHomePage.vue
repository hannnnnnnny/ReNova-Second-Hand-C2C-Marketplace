<template>
  <div class="merchant-store-page">
    <section class="merchant-store-hero">
      <div class="merchant-hero-copy">
        <p class="eyebrow">{{ store.category }} store / {{ template.name }}</p>
        <h1>{{ store.heroTitle }}</h1>
        <p>{{ store.heroText }}</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Shop {{ store.name }}</RouterLink>
          <RouterLink class="secondary-button" :to="{ path: `/store/${store.slug}/products`, query: { category: store.categories[0] } }">Browse {{ store.categories[0] }}</RouterLink>
        </div>
        <div class="merchant-store-proof">
          <span>{{ store.products.length }} products</span>
          <span>{{ store.shippingMessage }}</span>
          <span>{{ store.analytics?.conversionRate }} conversion demo</span>
        </div>
      </div>
      <figure class="merchant-hero-media">
        <img :src="store.heroImage || template.previewImage" :alt="`${store.name} storefront visual`" />
        <figcaption>
          <span>Current edit</span>
          <strong>{{ store.categories[0] }}</strong>
        </figcaption>
      </figure>
    </section>

    <section class="storefront-category-strip">
      <div>
        <p class="eyebrow">Shop by category</p>
        <h2>Start with the section that fits</h2>
      </div>
      <CategoryTiles :categories="store.categories" :products-path="`/store/${store.slug}/products`" />
    </section>

    <section class="storefront-product-section">
      <div class="storefront-section-header">
        <div>
          <p class="eyebrow">Featured products</p>
          <h2>Ready-to-shop picks from {{ store.name }}</h2>
        </div>
        <RouterLink class="text-link" :to="`/store/${store.slug}/products`">View all products</RouterLink>
      </div>
      <ProductGrid :products="featuredProducts" :store="store" @add="addToCart" />
    </section>
    <ToastMessage :message="toastMessage" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import CategoryTiles from '../../components/CategoryTiles.vue'
import ProductGrid from '../../components/ProductGrid.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { getTemplateById } from '../../data/platform'
import { useStorefrontCartStore } from '../../stores/storefrontCart'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const cartStore = useStorefrontCartStore()
const toastMessage = ref('')
let toastTimer
const template = computed(() => getTemplateById(props.store.template))
const featuredProducts = computed(() => props.store.products.slice(0, 6))

function addToCart(product) {
  cartStore.addItem(props.store.slug, product, 1)
  toastMessage.value = `${product.name} added to ${props.store.name} cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
