<template>
  <article class="product-card">
    <RouterLink :to="`/products/${product.id}`">
      <img :src="product.imageUrl" :alt="product.name" />
    </RouterLink>
    <div>
      <div class="product-card-meta">
        <p class="eyebrow">{{ product.brand || product.category?.name }}</p>
        <StatusBadge :value="stockStatus" :label="stockLabel" />
      </div>
      <RouterLink class="product-card-title" :to="`/products/${product.id}`">
        <h3>{{ product.name }}</h3>
      </RouterLink>
      <p v-if="product.category?.name" class="product-card-category">{{ product.category.name }}</p>
      <p>{{ product.description }}</p>
      <div v-if="product.tags?.length" class="mini-tag-row">
        <span v-for="tag in product.tags.slice(0, 2)" :key="tag">{{ tag }}</span>
      </div>
      <div class="card-row">
        <div class="price-stack">
          <strong>{{ formatCurrency(product.price) }}</strong>
          <span v-if="product.compareAtPrice">{{ formatCurrency(product.compareAtPrice) }}</span>
        </div>
        <div class="card-actions">
          <RouterLink class="text-link" :to="`/products/${product.id}`">Details</RouterLink>
          <button
            v-if="showCartAction"
            class="secondary-button compact-button"
            type="button"
            :disabled="product.stockQuantity < 1"
            @click="addProduct"
          >
            {{ product.stockQuantity < 1 ? 'Unavailable' : 'Add' }}
          </button>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
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

function addProduct() {
  cartStore.addItem(props.product, 1)
  emit('added', props.product)
}
</script>
