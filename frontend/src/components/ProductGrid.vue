<template>
  <div class="generated-product-grid">
    <article v-for="product in products" :key="product.id" class="generated-product-card">
      <RouterLink class="generated-product-image" :to="`/store/${store.slug}/products/${product.id}`">
        <img :src="product.imageUrl" :alt="product.name" />
        <span v-if="primaryBadge(product)" class="product-image-badge">{{ primaryBadge(product) }}</span>
      </RouterLink>
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
        <div class="generated-product-purchase-row">
          <div class="price-stack">
            <strong>{{ formatCurrency(product.price) }}</strong>
            <span v-if="product.compareAtPrice">{{ formatCurrency(product.compareAtPrice) }}</span>
          </div>
          <button class="primary-button compact-button" type="button" :disabled="product.stockQuantity < 1" @click="$emit('add', product)">
            <span>{{ product.stockQuantity < 1 ? 'Unavailable' : 'Add' }}</span>
          </button>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup>
import { formatCurrency } from '../utils/format'

defineProps({
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
</script>
