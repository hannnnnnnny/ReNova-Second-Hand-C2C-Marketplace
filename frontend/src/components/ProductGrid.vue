<template>
  <div class="generated-product-grid">
    <article v-for="product in products" :key="product.id" class="generated-product-card">
      <RouterLink class="generated-product-image" :to="`/store/${store.slug}/products/${product.id}`">
        <img :src="product.imageUrl" :alt="product.name" loading="lazy" decoding="async" />
        <span v-if="primaryBadge(product)" class="product-image-badge">{{ primaryBadge(product) }}</span>
      </RouterLink>
      <button
        class="favorite-button generated-favorite-button"
        type="button"
        :aria-label="favoriteLabel(product)"
        :aria-pressed="cartStore.isFavoriteForStore(store.slug, product.id)"
        @click="toggleFavorite(product)"
      >
        <Heart aria-hidden="true" />
      </button>
      <div class="generated-product-body">
        <div class="generated-product-meta">
          <span>{{ product.category }}</span>
          <span :class="{ warning: product.stockQuantity <= (product.lowStockThreshold || 5) }">
            {{ stockLabel(product) }}
          </span>
        </div>
        <RouterLink class="generated-product-title" :to="`/store/${store.slug}/products/${product.id}`">
          <h3>{{ product.name }}</h3>
        </RouterLink>
        <p>{{ product.description }}</p>
        <div v-if="hasOptions(product)" class="generated-option-summary" aria-label="Available product options">
          <span v-if="product.colors?.length">{{ product.colors.slice(0, 3).join(', ') }}</span>
          <span v-if="product.sizes?.length">{{ product.sizes.slice(0, 4).join(' / ') }}</span>
        </div>
        <div class="generated-product-purchase-row">
          <div class="price-stack">
            <strong>{{ formatCurrency(product.price) }}</strong>
            <span v-if="product.compareAtPrice">{{ formatCurrency(product.compareAtPrice) }}</span>
          </div>
          <RouterLink
            v-if="requiresSelection(product) && product.stockQuantity > 0"
            class="secondary-button compact-button"
            :to="`/store/${store.slug}/products/${product.id}`"
          >
            Choose
          </RouterLink>
          <button v-else class="primary-button compact-button" type="button" :disabled="product.stockQuantity < 1" @click="$emit('add', product)">
            <span>{{ product.stockQuantity < 1 ? 'Unavailable' : 'Add' }}</span>
          </button>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup>
import { Heart } from 'lucide-vue-next'
import { useStorefrontCartStore } from '../stores/storefrontCart'
import { formatCurrency } from '../utils/format'

const props = defineProps({
  products: {
    type: Array,
    required: true
  },
  store: {
    type: Object,
    required: true
  }
})

defineEmits(['add'])
const cartStore = useStorefrontCartStore()

function primaryBadge(product) {
  if (product.discountPercent) return `${product.discountPercent}% off`
  if (product.stockQuantity <= (product.lowStockThreshold || 5)) return 'Low stock'
  return product.badges?.[0] || ''
}

function stockLabel(product) {
  if (product.stockQuantity < 1) return 'Out'
  if (product.stockQuantity <= (product.lowStockThreshold || 5)) return `${product.stockQuantity} left`
  return 'In stock'
}

function hasOptions(product) {
  return Boolean(product.sizes?.length || product.colors?.length)
}

function requiresSelection(product) {
  return (product.sizes?.length || 0) > 1 || (product.colors?.length || 0) > 1
}

function favoriteLabel(product) {
  return cartStore.isFavoriteForStore(props.store.slug, product.id)
    ? `Remove ${product.name} from favorites`
    : `Save ${product.name} to favorites`
}

function toggleFavorite(product) {
  cartStore.toggleFavorite(props.store.slug, product.id)
}
</script>
