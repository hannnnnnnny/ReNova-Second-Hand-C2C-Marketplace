<template>
  <div class="merchant-store-page">
    <section class="merchant-store-hero">
      <div class="merchant-hero-copy">
        <p class="eyebrow">{{ store.category }} edit / {{ template.name }}</p>
        <h1>{{ store.heroTitle }}</h1>
        <p>{{ store.heroText }}</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Shop the edit</RouterLink>
          <RouterLink class="secondary-button" :to="{ path: `/store/${store.slug}/products`, query: { category: primaryCategory } }">New arrivals</RouterLink>
        </div>
        <div class="merchant-store-proof">
          <span>{{ store.products.length }} products</span>
          <span>{{ store.shippingMessage }}</span>
          <span>Demo checkout only</span>
        </div>
      </div>
      <figure class="merchant-hero-media">
        <img :src="store.heroImage || template.previewImage" :alt="`${store.name} storefront visual`" />
        <figcaption>
          <span>Current edit</span>
          <strong>{{ primaryCategory }}</strong>
        </figcaption>
      </figure>
    </section>

    <section class="storefront-service-strip" aria-label="Store service promises">
      <article>
        <span>01</span>
        <strong>Clear delivery</strong>
        <p>Shipping estimates and order tracking are shown before and after checkout.</p>
      </article>
      <article>
        <span>02</span>
        <strong>Easy returns</strong>
        <p>Every demo order includes a 30-day support and refund request window.</p>
      </article>
      <article>
        <span>03</span>
        <strong>Safe demo payment</strong>
        <p>Payment UI is realistic, but this portfolio checkout never processes real cards.</p>
      </article>
    </section>

    <section class="storefront-category-strip">
      <div>
        <p class="eyebrow">Shop by category</p>
        <h2>Find the right part of the edit</h2>
      </div>
      <CategoryTiles :categories="visibleCategories" :products-path="`/store/${store.slug}/products`" />
    </section>

    <section class="storefront-product-section">
      <div class="storefront-section-header">
        <div>
          <p class="eyebrow">Featured products</p>
          <h2>Pieces worth opening first</h2>
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
const visibleCategories = computed(() => {
  const seen = new Set()
  return (props.store.categories || []).filter((category) => {
    const key = String(category || '').trim().toLowerCase()
    if (!key || seen.has(key)) return false
    seen.add(key)
    return true
  })
})
const primaryCategory = computed(() => visibleCategories.value[0] || 'New Arrivals')

function addToCart(product) {
  cartStore.addItem(props.store.slug, product, 1)
  toastMessage.value = `${product.name} added to ${props.store.name} cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
