<template>
  <section v-if="order" class="page-section order-tracking-page">
    <PageHeader
      eyebrow="Order tracking"
      :title="`Order ${order.id}`"
      :description="`${order.tracking?.currentStatus || order.shippingStatus || 'Processing'} with ${order.tracking?.carrier || 'NovaPost Standard'}.`"
    />
    <div class="order-tracking-grid">
      <div class="tracking-main-panel summary-panel">
        <div class="tracking-header-row">
          <div>
            <p class="eyebrow">Fulfillment status</p>
            <h2>{{ order.tracking?.currentStatus || 'Processing' }}</h2>
          </div>
          <RouterLink class="secondary-button" :to="`/store/${store.slug}/orders`">Order history</RouterLink>
        </div>
        <ol class="tracking-timeline" aria-label="Order status timeline">
          <li v-for="event in timeline" :key="event.label" :class="event.status">
            <span aria-hidden="true"></span>
            <div>
              <strong>{{ event.label }}</strong>
              <small>{{ event.timestamp ? formatDateTime(event.timestamp) : 'Pending' }}</small>
              <p>{{ event.description }}</p>
            </div>
          </li>
        </ol>
      </div>
      <aside class="tracking-side-stack">
        <section class="summary-panel">
          <h2>Shipment</h2>
          <div class="summary-line"><span>Carrier</span><strong>{{ order.tracking?.carrier }}</strong></div>
          <div class="summary-line"><span>Tracking number</span><strong>{{ order.tracking?.trackingNumber }}</strong></div>
          <div class="summary-line"><span>Estimated delivery</span><strong>{{ formatDate(order.tracking?.estimatedDeliveryDate) }}</strong></div>
          <div class="summary-line"><span>Delivery method</span><strong>{{ deliveryMethodLabel(order.deliveryMethod) }}</strong></div>
        </section>
        <section class="summary-panel">
          <h2>Delivery address</h2>
          <p class="tracking-address">{{ formatOrderAddress(order) }}</p>
        </section>
        <section class="summary-panel">
          <h2>Payment</h2>
          <div class="summary-line"><span>Method</span><strong>{{ order.paymentMethodLabel || paymentMethodLabel(order.paymentMethod) }}</strong></div>
          <div class="summary-line"><span>Status</span><strong>{{ order.paymentStatus }}</strong></div>
          <div v-if="order.paymentDetails?.cardLast4" class="summary-line"><span>Card</span><strong>{{ order.paymentDetails.cardLast4 }}</strong></div>
        </section>
      </aside>
    </div>
    <section class="summary-panel order-detail-items-panel">
      <div class="tracking-header-row">
        <h2>Order summary</h2>
        <strong>{{ formatCurrency(order.total) }}</strong>
      </div>
      <article v-for="item in order.items" :key="item.itemId || item.productId" class="order-line-item">
        <img :src="item.imageUrl" :alt="item.name" loading="lazy" decoding="async" />
        <div>
          <strong>{{ item.name }}</strong>
          <span>{{ item.quantity }} item{{ item.quantity === 1 ? '' : 's' }}{{ selectedOptionsLabel(item) ? ` / ${selectedOptionsLabel(item)}` : '' }}</span>
        </div>
        <span>{{ formatCurrency(item.price * item.quantity) }}</span>
      </article>
    </section>
    <div class="hero-actions">
      <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Continue shopping</RouterLink>
      <RouterLink class="secondary-button" :to="{ path: `/store/${store.slug}/support`, query: { order: order.id } }">Need help?</RouterLink>
    </div>
  </section>
  <EmptyState v-else title="Order not found" message="This demo order is not saved in this browser.">
    <RouterLink class="primary-button" :to="`/store/${store.slug}/orders`">View order history</RouterLink>
  </EmptyState>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import { formatCurrency } from '../../utils/format'
import { deliveryMethodLabel, formatOrderAddress, loadOrder, paymentMethodLabel } from '../../utils/orderTracking'

defineProps({
  store: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const order = computed(() => loadOrder(route.params.orderId))
const timeline = computed(() => order.value?.tracking?.timeline || [])

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

function formatDateTime(value) {
  if (!value) return 'Pending'
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  }).format(new Date(value))
}
</script>
