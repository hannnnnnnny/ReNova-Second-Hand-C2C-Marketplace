<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Operations"
      title="Dashboard"
      description="Monitor fashion catalog health, orders, revenue, and inventory warnings from one overview."
    />
    <LoadingState v-if="loading" message="Loading dashboard..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else class="dashboard-layout">
      <div class="metric-grid">
        <MetricCard label="Total Products" :value="metrics.totalProducts || 0" detail="All catalog records" />
        <MetricCard label="Active Products" :value="metrics.activeProducts || 0" detail="Visible in storefront" />
        <MetricCard label="Categories" :value="metrics.totalCategories || 0" detail="Catalog groups" />
        <MetricCard label="Total Orders" :value="metrics.totalOrders || 0" detail="Orders received" />
        <MetricCard label="Pending Orders" :value="metrics.pendingOrders || 0" detail="Need review" />
        <MetricCard label="Revenue" :value="formatCurrency(metrics.revenue)" detail="Excluding cancelled orders" />
      </div>

      <section class="operations-strip" aria-label="Admin shortcuts">
        <RouterLink to="/admin/products">
          <strong>Manage Catalog</strong>
          <span>Create fashion products, adjust stock, and control visibility.</span>
        </RouterLink>
        <RouterLink to="/admin/orders">
          <strong>Review Orders</strong>
          <span>Move new orders through fulfillment.</span>
        </RouterLink>
        <RouterLink to="/admin/inventory">
          <strong>Check Inventory</strong>
          <span>Find low-stock products before checkout fails.</span>
        </RouterLink>
      </section>

      <section class="dashboard-section">
        <div class="admin-page-header">
          <h2>Recent Orders</h2>
          <RouterLink class="text-link" to="/admin/orders">View all</RouterLink>
        </div>
        <EmptyState v-if="!recentOrders.length" title="No orders yet" message="Orders will appear here after checkout." />
        <div v-else class="admin-table-wrap">
          <table class="admin-table compact-table">
            <thead>
              <tr>
                <th>Order</th>
                <th>Customer</th>
                <th>Status</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id">
                <td><RouterLink class="text-link" :to="`/admin/orders/${order.id}`">#{{ order.id }}</RouterLink></td>
                <td>{{ order.customerName }}</td>
                <td><StatusBadge :value="order.status" /></td>
                <td>{{ formatCurrency(order.totalAmount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="dashboard-section">
        <div class="admin-page-header">
          <h2>Inventory Watch</h2>
          <RouterLink class="text-link" to="/admin/inventory">Review inventory</RouterLink>
        </div>
        <EmptyState
          v-if="!warnings.length"
          title="Inventory is healthy"
          message="No products are at or below the default warning threshold."
        />
        <div v-else class="warning-grid compact-warning-grid">
          <article v-for="warning in warnings" :key="warning.productId" class="warning-card">
            <div>
              <p class="eyebrow">{{ warning.categoryName }}</p>
              <h3>{{ warning.productName }}</h3>
            </div>
            <strong>{{ warning.stockQuantity }} left</strong>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAdminOrders, fetchDashboardMetrics, fetchInventoryWarnings } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import MetricCard from '../../components/MetricCard.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatCurrency } from '../../utils/format'

const loading = ref(true)
const error = ref('')
const metrics = ref({})
const orders = ref([])
const warnings = ref([])
const recentOrders = computed(() => orders.value.slice(0, 5))

onMounted(async () => {
  try {
    const [metricData, orderData, warningData] = await Promise.all([
      fetchDashboardMetrics(),
      fetchAdminOrders(),
      fetchInventoryWarnings(5)
    ])
    metrics.value = metricData
    orders.value = orderData
    warnings.value = warningData
  } catch (requestError) {
    error.value = getApiError(requestError, 'Dashboard metrics could not be loaded.')
  } finally {
    loading.value = false
  }
})
</script>
