<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Fulfillment"
      title="Orders"
      description="Track customer fashion orders, payment state, and fulfillment progress."
    />
    <div class="catalog-toolbar admin-toolbar">
      <label class="search-field">
        Search orders
        <input v-model.trim="searchTerm" type="search" placeholder="Search by order number or customer" />
      </label>
      <label>
        Status
        <select v-model="statusFilter">
          <option value="all">All statuses</option>
          <option v-for="status in statuses" :key="status" :value="status">{{ formatStatus(status) }}</option>
        </select>
      </label>
    </div>
    <LoadingState v-if="loading" message="Loading orders..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState
      v-else-if="!filteredOrders.length"
      title="No orders yet"
      message="New checkout orders will appear here, or adjust the current filters."
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
          <tr v-for="order in filteredOrders" :key="order.id">
            <td>#{{ order.id }}</td>
            <td>
              <strong>{{ order.customerName }}</strong>
              <span>{{ order.customerEmail }}</span>
            </td>
            <td><StatusBadge :value="order.status" /></td>
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
import { computed, onMounted, ref } from 'vue'
import { fetchAdminOrders } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatCurrency, formatDate, formatStatus } from '../../utils/format'

const statuses = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED']
const loading = ref(true)
const error = ref('')
const orders = ref([])
const searchTerm = ref('')
const statusFilter = ref('all')
const filteredOrders = computed(() => {
  const query = searchTerm.value.toLowerCase()
  return orders.value.filter((order) => {
    const matchesQuery = query
      ? `#${order.id} ${order.customerName} ${order.customerEmail}`.toLowerCase().includes(query)
      : true
    const matchesStatus = statusFilter.value === 'all' || order.status === statusFilter.value
    return matchesQuery && matchesStatus
  })
})

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
