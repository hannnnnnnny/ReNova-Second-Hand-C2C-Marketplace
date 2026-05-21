<template>
  <section class="admin-page admin-dashboard-page">
    <AdminPageHeader
      eyebrow="Merchant workspace"
      :title="`${currentStore.name} dashboard`"
      description="Manage the current store setup, catalog health, sales signals, customer care, and storefront actions."
    >
      <template #actions>
        <RouterLink class="primary-button" to="/admin/products/new">Add product</RouterLink>
        <RouterLink class="secondary-button" :to="`/store/${currentStore.slug}`">Preview store</RouterLink>
      </template>
    </AdminPageHeader>

    <LoadingState v-if="loading" message="Loading dashboard..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else class="dashboard-layout professional-dashboard">
      <section class="admin-overview-hero">
        <div class="overview-hero-copy">
          <p class="eyebrow">Today in store</p>
          <h2>{{ currentStore.description }}</h2>
          <p>NovaCart is managing this as one merchant store inside a multi-store platform workspace.</p>
          <div class="overview-action-strip" aria-label="Dashboard operating signals">
            <span>Care queue: {{ openSupportCount }}</span>
            <span>Refund review: {{ pendingRefundCount }}</span>
            <span>Conversion: {{ conversionRate }}</span>
          </div>
        </div>
        <div class="overview-mini-grid">
          <article>
            <span>Store slug</span>
            <strong>/store/{{ currentStore.slug }}</strong>
          </article>
          <article>
            <span>Next action</span>
            <strong>{{ nextAction }}</strong>
          </article>
        </div>
      </section>

      <SetupChecklist :store="currentStore" />

      <div class="stat-grid">
        <StatCard label="Total sales" :value="formatCurrency(analytics.totalRevenue || metrics.revenue || 0)" detail="All non-cancelled orders" />
        <StatCard label="Orders" :value="analytics.totalOrders || metrics.totalOrders || 0" detail="Orders received" />
        <StatCard label="Visitors" :value="currentStore.analytics?.visitors || 0" detail="Current store demo signal" />
        <StatCard label="Conversion rate" :value="conversionRate" detail="Demo storefront signal" />
        <StatCard label="Average order value" :value="formatCurrency(analytics.averageOrderValue || currentStore.analytics?.averageOrderValue || 0)" detail="Paid order signal" />
        <StatCard label="Pending refunds" :value="pendingRefundCount" detail="Needs merchant review" />
        <StatCard label="Open support tickets" :value="openSupportCount" detail="Customer care queue" />
      </div>

      <div class="dashboard-main-grid">
        <ChartCard
          class="dashboard-chart-card"
          eyebrow="Revenue trend"
          title="Sales over the last 7 days"
          :points="trendPoints"
          :formatter="formatCurrency"
        >
          <template #actions>
            <RouterLink class="text-link" to="/admin/analytics">Open analytics</RouterLink>
          </template>
        </ChartCard>

        <section class="dashboard-section quick-action-panel">
          <div class="admin-page-header">
            <h2>Quick Actions</h2>
          </div>
          <QuickActionCard title="Edit website content" description="Change the live storefront copy, navigation, hero, SEO, and featured products." to="/admin/store-content" />
          <QuickActionCard title="Add product" description="Create an item for the current store catalog." to="/admin/products/new" />
          <QuickActionCard title="Customize theme" description="Edit logo, brand color, hero text, and announcement." to="/admin/theme-editor" />
          <QuickActionCard title="Create discount" description="Launch a product, category, collection, season, or tag markdown." to="/admin/promotions" />
          <QuickActionCard title="Preview store" description="Open the generated customer storefront." :to="`/store/${currentStore.slug}`" />
        </section>
      </div>

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
                <th>Payment</th>
                <th>Status</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id">
                <td><RouterLink class="text-link" :to="`/admin/orders/${order.id}`">{{ order.orderNumber || `#${order.id}` }}</RouterLink></td>
                <td>{{ order.customerEmail }}</td>
                <td><StatusBadge :value="order.paymentStatus" /></td>
                <td><StatusBadge :value="order.status" /></td>
                <td>{{ formatCurrency(order.totalAmount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="dashboard-widgets-grid">
        <article class="dashboard-section">
          <div class="admin-page-header">
            <h2>Top Selling Products</h2>
            <RouterLink class="text-link" to="/admin/products">Products</RouterLink>
          </div>
          <p v-for="product in analytics.bestSellingProducts" :key="product.productId" class="summary-line">
            <span>{{ product.productName }}</span>
            <strong>{{ product.unitsSold }} sold</strong>
          </p>
          <p v-if="!analytics.bestSellingProducts?.length" class="muted">Top products appear after checkout.</p>
        </article>

        <article class="dashboard-section">
          <div class="admin-page-header">
            <h2>Low Stock Products</h2>
            <RouterLink class="text-link" to="/admin/inventory">Inventory</RouterLink>
          </div>
          <p v-for="warning in warnings.slice(0, 5)" :key="warning.productId" class="summary-line">
            <span>{{ warning.productName }}</span>
            <strong>{{ warning.stockQuantity }} left</strong>
          </p>
          <p v-if="!warnings.length" class="muted">No low-stock products at the current threshold.</p>
        </article>

        <article class="dashboard-section">
          <div class="admin-page-header">
            <h2>Support & Refunds</h2>
            <RouterLink class="text-link" to="/admin/support">Care queue</RouterLink>
          </div>
          <p class="summary-line"><span>Open support tickets</span><strong>{{ openSupportCount }}</strong></p>
          <p class="summary-line"><span>Pending refund requests</span><strong>{{ pendingRefundCount }}</strong></p>
          <p class="summary-line"><span>Total refund records</span><strong>{{ refunds.length }}</strong></p>
        </article>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  fetchAdminAnalytics,
  fetchAdminCustomers,
  fetchAdminOrders,
  fetchAdminRefunds,
  fetchAdminSupportTickets,
  fetchDashboardMetrics,
  fetchInventoryWarnings
} from '../../api/admin'
import { getApiError } from '../../api/client'
import AdminPageHeader from '../../components/AdminPageHeader.vue'
import ChartCard from '../../components/ChartCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import QuickActionCard from '../../components/QuickActionCard.vue'
import SetupChecklist from '../../components/SetupChecklist.vue'
import StatCard from '../../components/StatCard.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { useAuthStore } from '../../stores/auth'
import { usePlatformStore } from '../../stores/platform'
import { buildLocalDashboardData, isLocalDemoAdminSession, shouldUseLocalDemoFallback } from '../../utils/demoAdmin'
import { formatCurrency } from '../../utils/format'

const platformStore = usePlatformStore()
const authStore = useAuthStore()
const loading = ref(true)
const error = ref('')
const metrics = ref({})
const analytics = ref({})
const orders = ref([])
const warnings = ref([])
const customers = ref([])
const supportTickets = ref([])
const refunds = ref([])
const currentStore = computed(() => platformStore.currentStore)
const recentOrders = computed(() => orders.value.slice(0, 6))
const openSupportCount = computed(() => supportTickets.value.filter((ticket) => !['RESOLVED', 'CLOSED'].includes(ticket.status)).length)
const pendingRefundCount = computed(() => refunds.value.filter((refund) => ['REQUESTED', 'UNDER_REVIEW'].includes(refund.status)).length)
const conversionRate = computed(() => {
  const ordersCount = Number(analytics.value.totalOrders || metrics.value.totalOrders || 0)
  if (!ordersCount) return '0.0%'
  return `${Math.min(9.5, Math.max(1.2, ordersCount * 0.35)).toFixed(1)}%`
})
const nextAction = computed(() => {
  if (pendingRefundCount.value) return 'Review refunds'
  if (openSupportCount.value) return 'Answer support'
  if (warnings.value.length) return 'Restock products'
  return 'Review catalog'
})
const trendPoints = computed(() => {
  return (analytics.value.salesTrend || []).map((point) => ({
    label: shortDate(point.date),
    value: Number(point.revenue)
  }))
})

onMounted(async () => {
  platformStore.loadPlatform()
  if (isLocalDemoAdminSession(authStore)) {
    applyLocalDashboardData()
    loading.value = false
    return
  }
  try {
    const [metricData, analyticsData, orderData, warningData, customerData, supportData, refundData] = await Promise.all([
      fetchDashboardMetrics(),
      fetchAdminAnalytics(),
      fetchAdminOrders(),
      fetchInventoryWarnings(5),
      fetchAdminCustomers(),
      fetchAdminSupportTickets(),
      fetchAdminRefunds()
    ])
    metrics.value = metricData
    analytics.value = analyticsData
    orders.value = orderData
    warnings.value = warningData
    customers.value = customerData
    supportTickets.value = supportData
    refunds.value = refundData
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) {
      error.value = getApiError(requestError, 'Dashboard metrics could not be loaded.')
    } else {
      applyLocalDashboardData()
      error.value = ''
    }
  } finally {
    loading.value = false
  }
})

function applyLocalDashboardData() {
  const localData = buildLocalDashboardData(currentStore.value)
  metrics.value = localData.metrics
  analytics.value = localData.analytics
  orders.value = localData.orders
  warnings.value = localData.warnings
  customers.value = localData.customers
  supportTickets.value = localData.supportTickets
  refunds.value = localData.refunds
}

function shortDate(value) {
  return new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric' }).format(new Date(value))
}
</script>
