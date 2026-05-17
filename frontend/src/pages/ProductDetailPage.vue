<template>
  <section class="page-section">
    <LoadingState v-if="loading" message="Loading product..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else>
      <div class="detail-layout product-detail-layout">
        <div class="product-media-panel">
          <img :src="selectedImage" :alt="product.name" />
          <div v-if="product.imageGallery?.length > 1" class="thumbnail-row" aria-label="Product image gallery">
            <button
              v-for="image in product.imageGallery"
              :key="image"
              class="thumbnail-button"
              type="button"
              :class="{ active: selectedImage === image }"
              @click="selectedImage = image"
            >
              <img :src="image" :alt="`${product.name} view`" />
            </button>
          </div>
        </div>
        <div class="product-buy-panel">
          <p class="eyebrow">{{ product.collection?.name || product.category?.name }}</p>
          <h1>{{ product.name }}</h1>
          <p class="product-description">{{ product.description }}</p>
          <div class="price-stack detail-price">
            <strong class="price">{{ formatCurrency(product.effectivePrice ?? product.price) }}</strong>
            <span v-if="product.compareAtPrice || Number(product.discountAmount) > 0">
              {{ formatCurrency(product.compareAtPrice || product.price) }}
            </span>
          </div>
          <span v-if="product.discountPercent" class="discount-badge">{{ product.discountPercent }}% off</span>
          <StatusBadge :value="stockStatus" :label="stockLabel" />

          <div class="purchase-box">
            <label v-if="product.sizes?.length">
              Size
              <select v-model="selectedSize">
                <option v-for="size in product.sizes" :key="size" :value="size">{{ size }}</option>
              </select>
            </label>
            <label v-if="product.colors?.length">
              Color
              <select v-model="selectedColor">
                <option v-for="color in product.colors" :key="color" :value="color">{{ color }}</option>
              </select>
            </label>
            <label>
              Quantity
              <QuantityStepper v-model="quantity" :max="Math.max(product.stockQuantity, 1)" />
            </label>
            <button class="primary-button" type="button" :disabled="product.stockQuantity < 1" @click="addToCart">
              Add to Cart
            </button>
            <button class="secondary-button" type="button" :disabled="product.stockQuantity < 1" @click="buyNow">
              Buy Now
            </button>
            <RouterLink class="secondary-button" to="/cart">View Cart</RouterLink>
          </div>
        </div>
      </div>

      <section class="product-info-grid">
        <article class="summary-panel">
          <h2>Product Details</h2>
          <p>{{ product.description }}</p>
          <p v-if="product.material"><strong>Material:</strong> {{ product.material }}</p>
          <p v-if="product.careInstructions"><strong>Care:</strong> {{ product.careInstructions }}</p>
          <p v-if="product.season"><strong>Season:</strong> {{ product.season }}</p>
        </article>
        <article class="summary-panel">
          <h2>Shipping and Returns</h2>
          <p>{{ product.stockQuantity }} units currently available.</p>
          <p class="muted">Standard delivery arrives in 3 to 6 business days. Returns and refund requests are supported within 30 days for eligible paid orders.</p>
          <RouterLink v-if="product.collection?.id" class="text-link" :to="{ name: 'products', query: { collectionId: product.collection.id } }">
            View {{ product.collection.name }}
          </RouterLink>
        </article>
        <article class="summary-panel">
          <h2>Customer Care</h2>
          <p class="muted">Need help with sizing, delivery, or a completed order? The merchant support flow records customer requests for admin review.</p>
          <RouterLink class="text-link" to="/support">Contact Support</RouterLink>
        </article>
      </section>

      <section v-if="relatedProducts.length" class="related-section">
        <SectionHeader
          eyebrow="More to consider"
          title="Related Products"
          description="Fashion products from the same category that can complete the look or collection story."
        />
        <div class="product-grid">
          <ProductCard
            v-for="relatedProduct in relatedProducts"
            :key="relatedProduct.id"
            :product="relatedProduct"
            @added="showAddedMessage"
          />
        </div>
      </section>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProduct, fetchProducts } from '../api/catalog'
import { getApiError } from '../api/client'
import ErrorMessage from '../components/ErrorMessage.vue'
import LoadingState from '../components/LoadingState.vue'
import ProductCard from '../components/ProductCard.vue'
import QuantityStepper from '../components/QuantityStepper.vue'
import SectionHeader from '../components/SectionHeader.vue'
import StatusBadge from '../components/StatusBadge.vue'
import ToastMessage from '../components/ToastMessage.vue'
import { useCartStore } from '../stores/cart'
import { formatCurrency } from '../utils/format'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const loading = ref(true)
const error = ref('')
const product = ref(null)
const relatedProducts = ref([])
const quantity = ref(1)
const selectedImage = ref('')
const selectedSize = ref('')
const selectedColor = ref('')
const toastMessage = ref('')
let toastTimer
const stockStatus = computed(() => {
  if (!product.value || product.value.stockQuantity < 1) return 'out of stock'
  if (product.value.stockQuantity <= 5) return 'low stock'
  return 'in stock'
})
const stockLabel = computed(() => {
  if (!product.value || product.value.stockQuantity < 1) return 'Out of stock'
  if (product.value.stockQuantity <= 5) return `${product.value.stockQuantity} left`
  return 'In stock'
})

watch(
  () => route.params.id,
  (productId) => loadProduct(productId),
  { immediate: true }
)

async function loadProduct(productId) {
  loading.value = true
  error.value = ''
  quantity.value = 1
  relatedProducts.value = []

  try {
    product.value = await fetchProduct(productId)
    selectedImage.value = product.value.imageGallery?.[0] || product.value.imageUrl
    selectedSize.value = product.value.sizes?.[0] || ''
    selectedColor.value = product.value.colors?.[0] || ''
    const categoryProducts = await fetchProducts(product.value.category?.id)
    relatedProducts.value = categoryProducts
      .filter((entry) => entry.id !== product.value.id)
      .slice(0, 3)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product could not be loaded.')
  } finally {
    loading.value = false
  }
}

function addToCart() {
  cartStore.addItem(product.value, quantity.value, {
    selectedSize: selectedSize.value,
    selectedColor: selectedColor.value
  })
  showAddedMessage(product.value)
}

function buyNow() {
  addToCart()
  router.push('/checkout')
}

function showAddedMessage(addedProduct) {
  toastMessage.value = `${addedProduct.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2600)
}
</script>
