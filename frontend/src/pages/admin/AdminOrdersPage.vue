<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Fulfillment"
      title="Orders"
      :description="`Track orders for ${currentStore.name}, payment state, fulfillment progress, and refund context.`"
    />
    <section class="order-status-tabs" aria-label="Order status filters">
      <button
        v-for="tab in orderTabs"
        :key="tab.value"
        class="status-tab-button"
        type="button"
        :class="{ active: statusFilter === tab.value }"
        @click="statusFilter = tab.value"
      >
        <span>{{ tab.label }}</span>
        <strong>{{ tab.count }}</strong>
      </button>
    </section>
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
      <label>
        Payment
        <select v-model="paymentFilter">
          <option value="all">All payments</option>
          <option v-for="status in paymentStatuses" :key="status" :value="status">{{ formatStatus(status) }}</option>
        </select>
      </label>
      <label>
        Refund
        <select v-model="refundFilter">
          <option value="all">All refunds</option>
          <option v-for="status in refundStatuses" :key="status" :value="status">{{ formatStatus(status) }}</option>
        </select>
      </label>
      <label>
        Region
        <input v-model.trim="regionFilter" placeholder="Country or region" />
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
            <th>Payment</th>
            <th>Fulfillment</th>
            <th>Refund</th>
            <th>Region</th>
            <th>Total</th>
            <th>Placed</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
            <td>{{ order.orderNumber || `#${order.id}` }}</td>
            <td>
              <strong>{{ order.customerName }}</strong>
              <span>{{ order.customerEmail }} / {{ order.customerPhone || 'No phone' }}</span>
            </td>
            <td><StatusBadge :value="order.paymentStatus" /></td>
            <td><StatusBadge :value="order.status" /></td>
            <td><StatusBadge :value="order.refundStatus" /></td>
            <td>{{ order.region || order.city }}, {{ order.country }}</td>
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
import { useAuthStore } from '../../stores/auth'
import { usePlatformStore } from '../../stores/platform'
import { isLocalDemoAdminSession, localAdminOrdersForStore, shouldUseLocalDemoFallback } from '../../utils/demoAdmin'
import { formatCurrency, formatDate, formatStatus } from '../../utils/format'

const platformStore = usePlatformStore()
const authStore = useAuthStore()
const statuses = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED']
const paymentStatuses = ['UNPAID', 'PAID', 'FAILED', 'REFUNDED']
const refundStatuses = ['NONE', 'REQUESTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'REFUNDED']
const loading = ref(true)
const error = ref('')
const orders = ref([])
const searchTerm = ref('')
const statusFilter = ref('all')
const paymentFilter = ref('all')
const refundFilter = ref('all')
const regionFilter = ref('')
const currentStore = computed(() => platformStore.currentStore)
const filteredOrders = computed(() => {
  const query = searchTerm.value.toLowerCase()
  const regionQuery = regionFilter.value.toLowerCase()
  return orders.value.filter((order) => {
    const matchesQuery = query
      ? `${order.orderNumber || ''} #${order.id} ${order.customerName} ${order.customerEmail}`.toLowerCase().includes(query)
      : true
    const matchesStatus = statusFilter.value === 'all' || order.status === statusFilter.value
    const matchesPayment = paymentFilter.value === 'all' || order.paymentStatus === paymentFilter.value
    const matchesRefund = refundFilter.value === 'all' || order.refundStatus === refundFilter.value
    const matchesRegion = regionQuery
      ? `${order.region || ''} ${order.city || ''} ${order.country || ''}`.toLowerCase().includes(regionQuery)
      : true
    return matchesQuery && matchesStatus && matchesPayment && matchesRefund && matchesRegion
  })
})
const orderTabs = computed(() => {
  const baseTabs = [{ value: 'all', label: 'All' }, ...statuses.map((status) => ({ value: status, label: formatStatus(status) }))]
  return baseTabs.map((tab) => ({
    ...tab,
    count: tab.value === 'all'
      ? orders.value.length
      : orders.value.filter((order) => order.status === tab.value).length
  }))
})

onMounted(async () => {
  platformStore.loadPlatform()
  if (isLocalDemoAdminSession(authStore)) {
    orders.value = localAdminOrdersForStore(currentStore.value.slug)
    loading.value = false
    return
  }
  try {
    orders.value = await fetchAdminOrders()
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) {
      error.value = getApiError(requestError, 'Orders could not be loaded.')
    } else {
      orders.value = localAdminOrdersForStore(currentStore.value.slug)
      error.value = ''
    }
  } finally {
    loading.value = false
  }
})
</script>
