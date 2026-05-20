<template>
  <section class="page-section order-history-page">
    <PageHeader
      eyebrow="Customer orders"
      :title="`${store.name} order history`"
      description="Local demo orders created in this browser, including payment and fulfillment status."
    />
    <EmptyState v-if="!orders.length" title="No orders yet" message="Complete a demo checkout to see tracking and fulfillment details here.">
      <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Start shopping</RouterLink>
    </EmptyState>
    <div v-else class="order-history-list">
      <article v-for="order in orders" :key="order.id" class="summary-panel order-history-card">
        <div class="order-history-main">
          <div>
            <p class="eyebrow">{{ formatDate(order.createdAt) }}</p>
            <h2>{{ order.id }}</h2>
            <p>{{ order.items.length }} item{{ order.items.length === 1 ? '' : 's' }} to {{ order.customer?.city || 'customer address' }}</p>
          </div>
          <div class="order-history-statuses">
            <span>{{ order.paymentStatus }}</span>
            <span>{{ order.tracking?.currentStatus || order.fulfillmentStatus || 'Processing' }}</span>
          </div>
        </div>
        <div class="summary-line"><span>Payment</span><strong>{{ order.paymentMethodLabel || paymentMethodLabel(order.paymentMethod) }}</strong></div>
        <div class="summary-line"><span>Delivery</span><strong>{{ deliveryMethodLabel(order.deliveryMethod) }}</strong></div>
        <div class="summary-line"><span>Estimated arrival</span><strong>{{ formatDate(order.tracking?.estimatedDeliveryDate) }}</strong></div>
        <div class="summary-line"><span>Total</span><strong>{{ formatCurrency(order.total) }}</strong></div>
        <div class="order-history-actions">
          <RouterLink class="primary-button" :to="`/store/${store.slug}/orders/${order.id}`">Track order</RouterLink>
          <button class="secondary-button" type="button" @click="reorder(order)">Reorder</button>
          <RouterLink class="text-link" :to="{ path: `/store/${store.slug}/support`, query: { order: order.id } }">Support</RouterLink>
        </div>
      </article>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useStorefrontCartStore } from '../../stores/storefrontCart'
import { formatCurrency } from '../../utils/format'
import { deliveryMethodLabel, loadStoreOrders, paymentMethodLabel } from '../../utils/orderTracking'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const cartStore = useStorefrontCartStore()
const toastMessage = ref('')
let toastTimer
const orders = computed(() => loadStoreOrders(props.store.slug))

function reorder(order) {
  order.items.forEach((item) => {
    cartStore.addItem(props.store.slug, {
      id: item.productId,
      name: item.name,
      price: item.price,
      compareAtPrice: item.compareAtPrice,
      imageUrl: item.imageUrl,
      stockQuantity: item.stockQuantity || item.quantity || 1,
      discountPercent: item.discountPercent || 0
    }, item.quantity, item.options || {})
  })
  toastMessage.value = `${order.items.length} order line${order.items.length === 1 ? '' : 's'} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
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
