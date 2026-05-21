<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Fulfillment"
      :title="`Order #${route.params.id}`"
      description="Review customer information, fashion items, totals, payment, and fulfillment status."
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
        <p>{{ order.customerPhone || 'No phone provided' }}</p>
        <p>{{ order.shippingAddress }}</p>
        <p>{{ order.city }}, {{ order.region }}, {{ order.postalCode }}</p>
        <p>{{ order.country }}</p>
      </section>
      <section class="summary-panel">
        <div class="admin-page-header">
          <h2>Status</h2>
          <div class="status-pair">
            <StatusBadge :value="order.paymentStatus" />
            <StatusBadge :value="order.refundStatus" />
            <StatusBadge :value="order.status" />
          </div>
        </div>
        <label>
          Order Status
          <select
            v-model="selectedStatus"
            :disabled="savingStatus || !canChangeStatus"
            @change="saveStatus"
          >
            <option v-for="status in availableStatuses" :key="status" :value="status">
              {{ formatStatus(status) }}
            </option>
          </select>
        </label>
        <p :class="['status-transition-note', { 'terminal-note': !canChangeStatus }]">
          {{ statusTransitionMessage }}
        </p>
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
              <td>
                {{ item.productName }}
                <span v-if="item.selectedSize || item.selectedColor">{{ item.selectedSize }} {{ item.selectedColor }}</span>
              </td>
              <td>{{ item.quantity }}</td>
              <td>{{ formatCurrency(item.unitPrice) }}</td>
              <td>{{ formatCurrency(item.lineTotal) }}</td>
            </tr>
          </tbody>
        </table>
        <p class="order-total">Order Total: {{ formatCurrency(order.totalAmount) }}</p>
      </section>
    </div>
    <ConfirmationDialog v-bind="confirmation" @confirm="confirm" @cancel="cancel" />
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchAdminOrder, updateOrderStatus } from '../../api/admin'
import { getApiError } from '../../api/client'
import ConfirmationDialog from '../../components/ConfirmationDialog.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useConfirmDialog } from '../../composables/useConfirmDialog'
import { usePlatformStore } from '../../stores/platform'
import { localAdminOrderById, shouldUseLocalDemoFallback, updateLocalAdminOrderStatus } from '../../utils/demoAdmin'
import { formatCurrency, formatDate, formatStatus } from '../../utils/format'
import { saveStoreStockMovement } from '../../utils/orderTracking'

const route = useRoute()
const { confirmation, requestConfirmation, confirm, cancel } = useConfirmDialog()
const platformStore = usePlatformStore()
const statuses = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED']
const statusTransitions = {
  PENDING: ['PAID', 'PROCESSING', 'CANCELLED'],
  PAID: ['PROCESSING', 'CANCELLED'],
  PROCESSING: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['COMPLETED'],
  COMPLETED: [],
  CANCELLED: []
}
const loading = ref(true)
const error = ref('')
const order = ref(null)
const selectedStatus = ref('')
const savingStatus = ref(false)
const toastMessage = ref('')
let toastTimer

const availableStatuses = computed(() => {
  if (!order.value?.status) return statuses

  const nextStatuses = statusTransitions[order.value.status] || []
  return [order.value.status, ...nextStatuses]
})

const canChangeStatus = computed(() => {
  if (!order.value?.status) return false
  return (statusTransitions[order.value.status] || []).length > 0
})

const statusTransitionMessage = computed(() => {
  if (!order.value?.status) return 'Order status options will appear after the order loads.'

  const nextStatuses = statusTransitions[order.value.status] || []
  if (nextStatuses.length === 0) {
    return 'This order is in a terminal state and cannot be changed.'
  }

  return `Next valid statuses: ${nextStatuses.map(formatStatus).join(', ')}.`
})

onMounted(loadOrder)

async function loadOrder() {
  platformStore.loadPlatform()
  if (String(route.params.id).startsWith('local-')) {
    const localOrder = localAdminOrderById(route.params.id)
    if (localOrder) {
      order.value = localOrder
      selectedStatus.value = localOrder.status
    } else {
      error.value = 'Local demo order could not be found in this browser.'
    }
    loading.value = false
    return
  }

  try {
    order.value = await fetchAdminOrder(route.params.id)
    selectedStatus.value = order.value.status
  } catch (requestError) {
    const localOrder = shouldUseLocalDemoFallback(requestError) ? localAdminOrderById(route.params.id) : null
    if (localOrder) {
      order.value = localOrder
      selectedStatus.value = localOrder.status
    } else {
      error.value = getApiError(requestError, 'Order could not be loaded.')
    }
  } finally {
    loading.value = false
  }
}

async function saveStatus() {
  if (selectedStatus.value === order.value.status) return

  const confirmed = await requestConfirmation({
    eyebrow: 'Fulfillment workflow',
    title: `Update order #${order.value.id}?`,
    message: `Move this order from ${formatStatus(order.value.status)} to ${formatStatus(selectedStatus.value)}. Cancellation restores item stock automatically.`,
    confirmLabel: 'Update status',
    tone: selectedStatus.value === 'CANCELLED' ? 'danger' : 'default'
  })
  if (!confirmed) {
    selectedStatus.value = order.value.status
    return
  }

  savingStatus.value = true
  try {
    const previousStatus = order.value.status
    if (order.value.localDemo) {
      const updatedOrder = updateLocalAdminOrderStatus(route.params.id, selectedStatus.value)
      if (!updatedOrder) {
        throw new Error('Local demo order status could not be updated.')
      }
      if (selectedStatus.value === 'CANCELLED' && previousStatus !== 'CANCELLED') {
        platformStore.applyInventoryChange(order.value.storeSlug, order.value.items, 1).forEach((movement) => {
          saveStoreStockMovement({
            ...movement,
            storeSlug: order.value.storeSlug,
            orderId: order.value.id,
            type: 'ORDER_CANCELLED',
            reason: `Stock restored after local order ${order.value.id} was cancelled.`
          })
        })
      }
      order.value = updatedOrder
    } else {
      order.value = await updateOrderStatus(route.params.id, selectedStatus.value)
    }
    selectedStatus.value = order.value.status
    showToast('Order status updated.')
  } catch (requestError) {
    error.value = requestError.response
      ? getApiError(requestError, 'Order status could not be updated.')
      : 'Order status could not be updated.'
    selectedStatus.value = order.value.status
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
