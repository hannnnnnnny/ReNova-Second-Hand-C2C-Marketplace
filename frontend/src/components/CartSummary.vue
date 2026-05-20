<template>
  <aside class="summary-panel order-summary-card">
    <h2>Cart Summary</h2>
    <div class="summary-line"><span>Items</span><strong>{{ itemCount }}</strong></div>
    <div class="summary-line"><span>Subtotal</span><strong>{{ formatCurrency(subtotal) }}</strong></div>
    <div v-if="discountTotal" class="summary-line"><span>Discounts</span><strong>-{{ formatCurrency(discountTotal) }}</strong></div>
    <div class="summary-line"><span>Shipping estimate</span><strong>{{ formatCurrency(shipping) }}</strong></div>
    <div class="summary-line total-line"><span>Total</span><strong>{{ formatCurrency(total) }}</strong></div>
    <slot />
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { formatCurrency } from '../utils/format'

const props = defineProps({
  itemCount: {
    type: Number,
    required: true
  },
  subtotal: {
    type: Number,
    required: true
  },
  discountTotal: {
    type: Number,
    default: 0
  },
  shipping: {
    type: Number,
    default: 6
  }
})

const total = computed(() => Math.max(0, props.subtotal - props.discountTotal) + props.shipping)
</script>
