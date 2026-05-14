<template>
  <section class="admin-page">
    <h1>Orders</h1>
    <LoadingState v-if="loading" message="Loading orders..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState
      v-else-if="!orders.length"
      title="No orders yet"
      message="New checkout orders will appear here."
    />
    <div v-else class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>Order</th>
            <th>Customer</th>
            <th>Status</th>
            <th>Total</th>
            <th>Placed</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td>#{{ order.id }}</td>
            <td>
              <strong>{{ order.customerName }}</strong>
              <span>{{ order.customerEmail }}</span>
            </td>
            <td><span class="status-pill">{{ formatStatus(order.status) }}</span></td>
            <td>{{ formatCurrency(order.totalAmount) }}</td>
            <td>{{ formatDate(order.createdAt) }}</td>
            <td><RouterLink class="text-link" :to="`/admin/orders/${order.id}`">View</RouterLink></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchAdminOrders } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import { formatCurrency, formatDate, formatStatus } from '../../utils/format'

const loading = ref(true)
const error = ref('')
const orders = ref([])

onMounted(async () => {
  try {
    orders.value = await fetchAdminOrders()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Orders could not be loaded.')
  } finally {
    loading.value = false
  }
})
</script>
