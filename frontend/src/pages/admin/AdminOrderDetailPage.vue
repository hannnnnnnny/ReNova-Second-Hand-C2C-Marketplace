<template>
  <section class="admin-page">
    <div class="admin-page-header">
      <h1>Order #{{ route.params.id }}</h1>
      <RouterLink class="secondary-button" to="/admin/orders">Back to Orders</RouterLink>
    </div>
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
        <h2>Status</h2>
        <label>
          Order Status
          <select v-model="selectedStatus" @change="saveStatus">
            <option v-for="status in statuses" :key="status" :value="status">
              {{ formatStatus(status) }}
            </option>
          </select>
        </label>
        <p class="muted">Placed {{ formatDate(order.createdAt) }}</p>
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
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchAdminOrder, updateOrderStatus } from '../../api/admin'
import { getApiError } from '../../api/client'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import { formatCurrency, formatDate, formatStatus } from '../../utils/format'

const route = useRoute()
const statuses = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED']
const loading = ref(true)
const error = ref('')
const order = ref(null)
const selectedStatus = ref('')

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
  try {
    order.value = await updateOrderStatus(route.params.id, selectedStatus.value)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Order status could not be updated.')
  }
}
</script>
