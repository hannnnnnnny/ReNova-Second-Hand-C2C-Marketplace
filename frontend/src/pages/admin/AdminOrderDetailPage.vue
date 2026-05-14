<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Fulfillment"
      :title="`Order #${route.params.id}`"
      description="Review customer information, order items, totals, and fulfillment status."
    >
      <template #actions>
        <RouterLink class="secondary-button" to="/admin/orders">Back to Orders</RouterLink>
      </template>
    </PageHeader>
    <LoadingState v-if="loading" message="Loading order..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else class="order-detail-grid">
      <section class="summary-panel">
        <h2>Customer</h2>
        <p><strong>{{ order.customerName }}</strong></p>
        <p>{{ order.customerEmail }}</p>
        <p>{{ order.shippingAddress }}</p>
        <p>{{ order.city }}, {{ order.postalCode }}</p>
        <p>{{ order.country }}</p>
      </section>
      <section class="summary-panel">
        <div class="admin-page-header">
          <h2>Status</h2>
          <StatusBadge :value="order.status" />
        </div>
        <label>
          Order Status
          <select v-model="selectedStatus" :disabled="savingStatus" @change="saveStatus">
            <option v-for="status in statuses" :key="status" :value="status">
              {{ formatStatus(status) }}
            </option>
          </select>
        </label>
        <p class="muted">Placed {{ formatDate(order.createdAt) }}</p>
        <p v-if="savingStatus" class="muted">Updating status...</p>
      </section>
      <section class="summary-panel wide-field">
        <h2>Items</h2>
        <table class="admin-table compact-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Quantity</th>
              <th>Unit Price</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in order.items" :key="item.id">
              <td>{{ item.productName }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ formatCurrency(item.unitPrice) }}</td>
              <td>{{ formatCurrency(item.lineTotal) }}</td>
            </tr>
          </tbody>
        </table>
        <p class="order-total">Order Total: {{ formatCurrency(order.totalAmount) }}</p>
      </section>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchAdminOrder, updateOrderStatus } from '../../api/admin'
import { getApiError } from '../../api/client'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { formatCurrency, formatDate, formatStatus } from '../../utils/format'

const route = useRoute()
const statuses = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED']
const loading = ref(true)
const error = ref('')
const order = ref(null)
const selectedStatus = ref('')
const savingStatus = ref(false)
const toastMessage = ref('')
let toastTimer

onMounted(loadOrder)

async function loadOrder() {
  try {
    order.value = await fetchAdminOrder(route.params.id)
    selectedStatus.value = order.value.status
  } catch (requestError) {
    error.value = getApiError(requestError, 'Order could not be loaded.')
  } finally {
    loading.value = false
  }
}

async function saveStatus() {
  savingStatus.value = true
  try {
    order.value = await updateOrderStatus(route.params.id, selectedStatus.value)
    showToast('Order status updated.')
  } catch (requestError) {
    error.value = getApiError(requestError, 'Order status could not be updated.')
  } finally {
    savingStatus.value = false
  }
}

function showToast(message) {
  toastMessage.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
