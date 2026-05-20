<template>
  <section v-if="product" class="page-section merchant-product-detail-page">
    <nav class="product-breadcrumb" aria-label="Product path">
      <RouterLink :to="`/store/${store.slug}`">{{ store.name }}</RouterLink>
      <span>/</span>
      <RouterLink :to="{ path: `/store/${store.slug}/products`, query: { category: product.category } }">{{ product.category }}</RouterLink>
    </nav>
    <div class="merchant-detail-layout">
      <div class="merchant-detail-media">
        <span v-if="product.discountPercent" class="product-image-badge detail-discount-badge">{{ product.discountPercent }}% off</span>
        <img :src="product.imageUrl" :alt="product.name" decoding="async" />
      </div>
      <div class="merchant-detail-buy-panel">
        <p class="eyebrow">{{ product.category }}</p>
        <h1>{{ product.name }}</h1>
        <p class="product-description">{{ product.description }}</p>
        <div v-if="product.rating" class="product-detail-proof" :aria-label="`${product.rating} out of 5 from ${product.reviewCount} reviews`">
          <Star aria-hidden="true" />
          <strong>{{ product.rating.toFixed(1) }}</strong>
          <span>{{ product.reviewCount }} reviews</span>
          <em v-if="product.merchandisingLabel">{{ product.merchandisingLabel }}</em>
        </div>
        <div class="price-stack detail-price">
          <strong class="price">{{ formatCurrency(product.price) }}</strong>
          <span v-if="product.compareAtPrice">{{ formatCurrency(product.compareAtPrice) }}</span>
        </div>
        <p class="muted">{{ product.stockQuantity }} available from {{ store.name }}. Demo-safe checkout, no real payment captured.</p>
        <fieldset v-if="product.colors?.length" class="merchant-option-group">
          <legend>Color</legend>
          <div class="option-button-row">
            <button
              v-for="color in product.colors"
              :key="color"
              type="button"
              :class="{ active: selectedColor === color }"
              @click="selectedColor = color"
            >
              {{ color }}
            </button>
          </div>
        </fieldset>
        <fieldset v-if="product.sizes?.length" class="merchant-option-group">
          <legend>Size</legend>
          <div class="option-button-row">
            <button
              v-for="size in product.sizes"
              :key="size"
              type="button"
              :class="{ active: selectedSize === size }"
              @click="selectedSize = size"
            >
              {{ size }}
            </button>
          </div>
        </fieldset>
        <p v-if="selectionError || selectionHint" class="field-error">{{ selectionError || selectionHint }}</p>
        <label>Quantity<QuantityStepper v-model="quantity" :max="Math.max(product.stockQuantity, 1)" /></label>
        <div class="card-actions">
          <button class="primary-button" type="button" :disabled="!canAddToCart" @click="addToCart">Add to cart</button>
          <button class="secondary-button" type="button" :disabled="!canAddToCart" @click="buyNow">Buy now</button>
          <button class="secondary-button favorite-detail-button" type="button" :aria-pressed="isFavorite" @click="toggleFavorite">
            <Heart aria-hidden="true" />
            {{ isFavorite ? 'Saved' : 'Save' }}
          </button>
        </div>
        <div class="merchant-detail-notes">
          <span>{{ store.shippingMessage }}</span>
          <span>{{ product.deliveryPromise || 'Ships from merchant in 1-3 business days' }}</span>
          <span>30-day refund request window</span>
          <span v-if="product.material">Material: {{ product.material }}</span>
          <span v-if="product.careInstructions">Care: {{ product.careInstructions }}</span>
        </div>
        <div v-if="product.reviewHighlights?.length" class="review-highlight-panel">
          <strong>Customer notes</strong>
          <ul>
            <li v-for="highlight in product.reviewHighlights" :key="highlight">{{ highlight }}</li>
          </ul>
        </div>
      </div>
    </div>
    <section class="related-section">
      <div class="retail-section-heading">
        <p class="eyebrow">More from {{ store.name }}</p>
        <h2>Related products</h2>
      </div>
      <ProductGrid :products="relatedProducts" :store="store" @add="addRelated" />
    </section>
    <section v-if="recentlyViewedProducts.length" class="related-section recently-viewed-section">
      <div class="retail-section-heading">
        <p class="eyebrow">Browsing history</p>
        <h2>Recently viewed in {{ store.name }}</h2>
      </div>
      <ProductGrid :products="recentlyViewedProducts" :store="store" @add="addRelated" />
    </section>
    <ToastMessage :message="toastMessage" />
  </section>
  <EmptyState v-else title="Product not found" message="This product is not available in the merchant storefront.">
    <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Back to products</RouterLink>
  </EmptyState>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Heart, Star } from 'lucide-vue-next'
import EmptyState from '../../components/EmptyState.vue'
import ProductGrid from '../../components/ProductGrid.vue'
import QuantityStepper from '../../components/QuantityStepper.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useStorefrontCartStore } from '../../stores/storefrontCart'
import { formatCurrency } from '../../utils/format'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const router = useRouter()
const cartStore = useStorefrontCartStore()
const quantity = ref(1)
const selectedSize = ref('')
const selectedColor = ref('')
const selectionError = ref('')
const toastMessage = ref('')
let toastTimer
const product = computed(() => props.store.products.find((entry) => String(entry.id) === String(route.params.productId)))
const relatedProducts = computed(() => props.store.products.filter((entry) => entry.id !== product.value?.id).slice(0, 3))
const recentlyViewedProducts = computed(() => {
  return cartStore.recentlyViewedForStore(props.store.slug)
    .map((item) => props.store.products.find((entry) => entry.id === item.productId))
    .filter((entry) => entry && entry.id !== product.value?.id)
    .slice(0, 3)
})
const requiresSize = computed(() => (product.value?.sizes?.length || 0) > 1)
const requiresColor = computed(() => (product.value?.colors?.length || 0) > 1)
const isFavorite = computed(() => product.value ? cartStore.isFavoriteForStore(props.store.slug, product.value.id) : false)
const canAddToCart = computed(() => {
  if (!product.value || product.value.stockQuantity < 1) return false
  if (requiresSize.value && !selectedSize.value) return false
  if (requiresColor.value && !selectedColor.value) return false
  return true
})
const selectionHint = computed(() => {
  if (!product.value || product.value.stockQuantity < 1) return ''
  if (requiresSize.value && !selectedSize.value) return 'Choose a size to continue.'
  if (requiresColor.value && !selectedColor.value) return 'Choose a color to continue.'
  return ''
})

watch(
  product,
  (nextProduct) => {
    if (nextProduct) {
      cartStore.recordRecentlyViewed(props.store.slug, nextProduct)
    }
  },
  { immediate: true }
)

function addToCart() {
  if (!product.value) return false
  selectionError.value = ''
  if (requiresSize.value && !selectedSize.value) {
    selectionError.value = 'Choose a size before adding this product.'
    return false
  }
  if (requiresColor.value && !selectedColor.value) {
    selectionError.value = 'Choose a color before adding this product.'
    return false
  }
  cartStore.addItem(props.store.slug, product.value, quantity.value, {
    size: selectedSize.value || product.value.sizes?.[0] || '',
    color: selectedColor.value || product.value.colors?.[0] || ''
  })
  toastMessage.value = `${product.value.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
  return true
}

function addRelated(relatedProduct) {
  if ((relatedProduct.sizes?.length || 0) > 1 || (relatedProduct.colors?.length || 0) > 1) {
    router.push(`/store/${props.store.slug}/products/${relatedProduct.id}`)
    return
  }
  cartStore.addItem(props.store.slug, relatedProduct, 1, {
    size: relatedProduct.sizes?.[0] || '',
    color: relatedProduct.colors?.[0] || ''
  })
  toastMessage.value = `${relatedProduct.name} added to cart.`
}

function buyNow() {
  if (addToCart()) {
    router.push(`/store/${props.store.slug}/checkout`)
  }
}

function toggleFavorite() {
  if (!product.value) return
  const saved = cartStore.toggleFavorite(props.store.slug, product.value.id)
  toastMessage.value = saved ? `${product.value.name} saved to favorites.` : `${product.value.name} removed from favorites.`
}
</script>
