<template>
  <section class="page-section">
    <div class="success-panel">
      <p class="eyebrow">Order placed</p>
      <h1>Thanks for shopping {{ store.name }}</h1>
      <p>Your demo order has been saved locally for this generated merchant storefront.</p>
      <div v-if="order" class="summary-panel">
        <div class="summary-line"><span>Order number</span><strong>{{ order.id }}</strong></div>
        <div class="summary-line"><span>Payment method</span><strong>{{ order.paymentMethodLabel || paymentMethodLabel(order.paymentMethod) }}</strong></div>
        <div class="summary-line"><span>Payment status</span><strong>{{ order.paymentStatus }}</strong></div>
        <div class="summary-line"><span>Delivery</span><strong>{{ deliveryMethodLabel(order.deliveryMethod) }}</strong></div>
        <div class="summary-line"><span>Estimated arrival</span><strong>{{ formatDate(order.tracking?.estimatedDeliveryDate) }}</strong></div>
        <div class="summary-line"><span>Carrier</span><strong>{{ order.tracking?.carrier || 'NovaPost Standard' }}</strong></div>
        <div class="summary-line"><span>Tracking number</span><strong>{{ order.tracking?.trackingNumber || 'Pending' }}</strong></div>
        <div class="summary-line"><span>Ship to</span><strong>{{ formatOrderAddress(order) }}</strong></div>
        <div class="summary-line"><span>Total</span><strong>{{ formatCurrency(order.total) }}</strong></div>
        <div class="summary-line"><span>Refund window</span><strong>30 days</strong></div>
      </div>
      <div v-if="order?.items?.length" class="order-success-items">
        <article v-for="item in order.items" :key="item.itemId || item.productId">
          <img :src="item.imageUrl" :alt="item.name" loading="lazy" decoding="async" />
          <div>
            <strong>{{ item.name }}</strong>
            <span>{{ item.quantity }} item{{ item.quantity === 1 ? '' : 's' }}{{ selectedOptionsLabel(item) ? ` / ${selectedOptionsLabel(item)}` : '' }}</span>
          </div>
        </article>
      </div>
      <div class="hero-actions">
        <RouterLink v-if="order" class="primary-button" :to="`/store/${store.slug}/orders/${order.id}`">Track order</RouterLink>
        <RouterLink class="secondary-button" :to="`/store/${store.slug}`">Continue shopping</RouterLink>
        <RouterLink class="secondary-button" :to="{ path: `/store/${store.slug}/support`, query: { order: order?.id || '' } }">Request support</RouterLink>
        <RouterLink class="secondary-button" :to="{ path: `/store/${store.slug}/support`, query: { order: order?.id || '', mode: 'refund' } }">Request refund</RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { formatCurrency } from '../../utils/format'
import { deliveryMethodLabel, formatOrderAddress, loadOrder, paymentMethodLabel } from '../../utils/orderTracking'

defineProps({
  store: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const order = computed(() => loadOrder(route.query.order))

function selectedOptionsLabel(item) {
  return [item.options?.size ? `Size ${item.options.size}` : '', item.options?.color ? `Color ${item.options.color}` : '']
    .filter(Boolean)
    .join(' / ')
}

function formatDate(value) {
  if (!value) return 'Pending'
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  }).format(new Date(value))
}
</script>
