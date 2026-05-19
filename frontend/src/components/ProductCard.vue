<template>
  <article class="product-card fashion-product-card">
    <RouterLink class="product-image-frame" :to="`/products/${product.id}`">
      <img :src="product.imageUrl" :alt="product.name" loading="lazy" decoding="async" />
      <span v-if="primaryBadge" class="product-image-badge">{{ primaryBadge }}</span>
      <span v-if="product.collection?.name" class="product-collection-ribbon">{{ product.collection.name }}</span>
    </RouterLink>
    <div class="product-card-body">
      <div class="product-card-meta">
        <p class="eyebrow">{{ product.brand || product.category?.name }}</p>
        <StatusBadge :value="stockStatus" :label="stockLabel" />
      </div>
      <RouterLink class="product-card-title" :to="`/products/${product.id}`">
        <h3>{{ product.name }}</h3>
      </RouterLink>
      <p v-if="product.category?.name" class="product-card-category">{{ product.category.name }}</p>
      <p class="product-card-description">{{ product.description }}</p>
      <div class="variant-summary fashion-variant-summary" aria-label="Available product options">
        <span v-if="displayColors.length" class="swatch-row" aria-label="Available colors">
          <i
            v-for="color in displayColors"
            :key="color"
            class="color-dot"
            :title="color"
            :style="{ background: colorSwatch(color) }"
            aria-hidden="true"
          ></i>
          <small>{{ product.colors.length }} colors</small>
        </span>
        <span v-if="displaySizes.length">{{ displaySizes.join(' / ') }}</span>
      </div>
      <div v-if="product.discountPercent" class="mini-tag-row">
        <span>Save {{ product.discountPercent }}%</span>
        <span v-if="product.collection?.name">{{ product.collection.name }}</span>
      </div>
      <div v-else-if="product.tags?.length" class="mini-tag-row">
        <span v-for="tag in product.tags.slice(0, 2)" :key="tag">{{ tag }}</span>
      </div>
      <div class="card-row retail-card-row">
        <div class="price-stack">
          <strong>{{ formatCurrency(product.effectivePrice ?? product.price) }}</strong>
          <span v-if="product.compareAtPrice || Number(product.discountAmount) > 0">
            {{ formatCurrency(product.compareAtPrice || product.price) }}
          </span>
        </div>
      </div>
      <div class="card-actions retail-card-actions">
        <button
          v-if="showCartAction"
          class="primary-button product-card-cta"
          type="button"
          :disabled="product.stockQuantity < 1"
          @click="addProduct"
        >
          {{ cartActionLabel }}
        </button>
        <RouterLink class="secondary-button product-card-secondary" :to="`/products/${product.id}`">View Details</RouterLink>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusBadge from './StatusBadge.vue'
import { useCartStore } from '../stores/cart'
import { formatCurrency } from '../utils/format'

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  showCartAction: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['added'])
const cartStore = useCartStore()
const router = useRouter()
const stockStatus = computed(() => {
  if (props.product.stockQuantity < 1) return 'out of stock'
  if (props.product.stockQuantity <= lowStockThreshold.value) return 'low stock'
  return 'in stock'
})
const stockLabel = computed(() => {
  if (props.product.stockQuantity < 1) return 'Out of stock'
  if (props.product.stockQuantity <= lowStockThreshold.value) return `${props.product.stockQuantity} left`
  return 'In stock'
})
const lowStockThreshold = computed(() => props.product.lowStockThreshold ?? 5)
const displayColors = computed(() => props.product.colors?.slice(0, 4) || [])
const displaySizes = computed(() => props.product.sizes?.slice(0, 5) || [])
const primaryBadge = computed(() => {
  if (props.product.discountPercent) return `${props.product.discountPercent}% off`
  if (props.product.tags?.includes('new-arrival')) return 'New arrival'
  if (props.product.featured) return 'Featured'
  if (props.product.stockQuantity > 0 && props.product.stockQuantity <= lowStockThreshold.value) return 'Low stock'
  return ''
})
const requiresOptionSelection = computed(() => {
  return (props.product.sizes?.length || 0) > 1 || (props.product.colors?.length || 0) > 1
})
const cartActionLabel = computed(() => {
  if (props.product.stockQuantity < 1) return 'Unavailable'
  return requiresOptionSelection.value ? 'Choose Options' : 'Quick Add'
})

function addProduct() {
  if (requiresOptionSelection.value) {
    router.push(`/products/${props.product.id}`)
    return
  }
  cartStore.addItem(props.product, 1)
  emit('added', props.product)
}

function colorSwatch(color) {
  const swatches = {
    Black: '#141414',
    Ivory: '#f5efe1',
    Taupe: '#97816b',
    Sand: '#d7ba87',
    Sky: '#9dc6dd',
    Pearl: '#f7f5ef',
    Wine: '#7b263d',
    Slate: '#596671',
    Pine: '#284b3a',
    Gold: '#c4a24a',
    Silver: '#c5c9cf'
  }
  return swatches[color] || '#d9ded8'
}
</script>
