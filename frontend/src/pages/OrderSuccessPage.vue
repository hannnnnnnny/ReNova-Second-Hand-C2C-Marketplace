<template>
  <section class="page-section">
    <LoadingState v-if="loading" message="Loading order..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else class="success-layout">
      <section class="success-panel">
        <p class="eyebrow">Order Confirmed</p>
        <h1>Thank you, {{ order.customerName }}</h1>
        <p>Your fashion order has been received and is ready for merchant review.</p>
        <div class="confirmation-number">
          <span>Order Number</span>
          <strong>{{ order.orderNumber || `#${order.id}` }}</strong>
        </div>
        <div class="status-pair">
          <StatusBadge :value="order.paymentStatus" :label="formatStatus(order.paymentStatus)" />
          <StatusBadge :value="order.status" :label="formatStatus(order.status)" />
        </div>
        <p class="muted">Payment was handled by NovaCart demo checkout. No real card was charged.</p>
        <div class="success-actions">
          <RouterLink class="primary-button" to="/products">Continue Shopping</RouterLink>
          <RouterLink class="secondary-button" to="/">Return Home</RouterLink>
        </div>
      </section>

      <aside class="summary-panel order-summary-card">
        <h2>Order Summary</h2>
        <div class="checkout-items">
          <div v-for="item in order.items" :key="item.id" class="checkout-item">
            <span>{{ item.productName }} x {{ item.quantity }}</span>
            <strong>{{ formatCurrency(item.lineTotal) }}</strong>
          </div>
        </div>
        <div class="summary-line">
          <span>Subtotal</span>
          <strong>{{ formatCurrency(order.subtotalAmount) }}</strong>
        </div>
        <div class="summary-line">
          <span>Shipping</span>
          <strong>{{ formatCurrency(order.shippingAmount) }}</strong>
        </div>
        <div class="summary-line">
          <span>Estimated Tax</span>
          <strong>{{ formatCurrency(order.taxAmount) }}</strong>
        </div>
        <div class="summary-line total-line">
          <span>Total</span>
          <strong>{{ formatCurrency(order.totalAmount) }}</strong>
        </div>
        <p class="muted">{{ formatStatus(order.shippingMethod) }} selected. A merchant can update the order status from the admin workspace.</p>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchOrder } from '../api/orders'
import { getApiError } from '../api/client'
import ErrorMessage from '../components/ErrorMessage.vue'
import LoadingState from '../components/LoadingState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { formatCurrency, formatStatus } from '../utils/format'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const order = ref(null)

onMounted(async () => {
  try {
    order.value = await fetchOrder(route.params.id)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Order could not be loaded.')
  } finally {
    loading.value = false
  }
})
</script>
