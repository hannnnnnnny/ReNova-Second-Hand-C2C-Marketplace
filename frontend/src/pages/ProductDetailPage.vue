<template>
  <section class="page-section">
    <LoadingState v-if="loading" message="Loading product..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else>
      <div class="detail-layout product-detail-layout">
        <div class="product-media-panel">
          <img :src="product.imageUrl" :alt="product.name" />
        </div>
        <div class="product-buy-panel">
          <p class="eyebrow">{{ product.category?.name }}</p>
          <h1>{{ product.name }}</h1>
          <p class="product-description">{{ product.description }}</p>
          <strong class="price">{{ formatCurrency(product.price) }}</strong>
          <StatusBadge :value="stockStatus" :label="stockLabel" />

          <div class="purchase-box">
            <label>
              Quantity
              <QuantityStepper v-model="quantity" :max="Math.max(product.stockQuantity, 1)" />
            </label>
            <button class="primary-button" type="button" :disabled="product.stockQuantity < 1" @click="addToCart">
              Add to Cart
            </button>
            <RouterLink class="secondary-button" to="/cart">View Cart</RouterLink>
          </div>
        </div>
      </div>

      <section class="product-info-grid">
        <article class="summary-panel">
          <h2>Product Details</h2>
          <p>{{ product.description }}</p>
        </article>
        <article class="summary-panel">
          <h2>Availability</h2>
          <p>{{ product.stockQuantity }} units currently available.</p>
          <p class="muted">Inventory updates when checkout is completed.</p>
        </article>
      </section>

      <section v-if="relatedProducts.length" class="related-section">
        <SectionHeader
          eyebrow="More to consider"
          title="Related Products"
          description="Products from the same category that may fit the same workflow."
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
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
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
const cartStore = useCartStore()
const loading = ref(true)
const error = ref('')
const product = ref(null)
const relatedProducts = ref([])
const quantity = ref(1)
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

onMounted(async () => {
  try {
    product.value = await fetchProduct(route.params.id)
    const categoryProducts = await fetchProducts(product.value.category?.id)
    relatedProducts.value = categoryProducts
      .filter((entry) => entry.id !== product.value.id)
      .slice(0, 3)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product could not be loaded.')
  } finally {
    loading.value = false
  }
})

function addToCart() {
  cartStore.addItem(product.value, quantity.value)
  showAddedMessage(product.value)
}

function showAddedMessage(addedProduct) {
  toastMessage.value = `${addedProduct.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2600)
}
</script>
