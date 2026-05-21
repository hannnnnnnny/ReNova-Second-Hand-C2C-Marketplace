<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Inventory"
      title="Stock Watch"
      description="Review fashion products that need replenishment before customers hit checkout limits."
    >
      <template #actions>
        <RouterLink class="secondary-button" to="/admin/products">Manage Products</RouterLink>
      </template>
    </PageHeader>
    <div class="inventory-controls">
      <label class="threshold-control">
        Low-stock threshold
        <input v-model.number="threshold" min="0" type="number" @change="loadWarnings" />
      </label>
      <button class="secondary-button" type="button" @click="loadWarnings">Refresh</button>
    </div>
    <p class="inventory-guidance">
      Products at or below this threshold deserve attention before customers run into size, color, or checkout limits.
    </p>
    <section class="inventory-adjustment-section">
      <h2>Manual Stock Adjustment</h2>
      <form class="admin-form compact-admin-form" @submit.prevent="submitAdjustment">
        <label>
          Product
          <select v-model="adjustment.productId" required>
            <option disabled value="">Select a product</option>
            <option v-for="product in products" :key="product.id" :value="String(product.id)">
              {{ product.name }} / {{ product.stockQuantity }} in stock
            </option>
          </select>
        </label>
        <label>
          Quantity Change
          <input v-model.number="adjustment.quantityChange" required step="1" type="number" placeholder="12 or -3" />
        </label>
        <label class="wide-field">
          Reason
          <textarea v-model.trim="adjustment.reason" required rows="3" maxlength="500" placeholder="Received replenishment, corrected count, or damaged stock removal."></textarea>
        </label>
        <p v-if="selectedAdjustmentProduct" class="muted wide-field">
          Current stock: {{ selectedAdjustmentProduct.stockQuantity }}.
          New stock after adjustment: {{ projectedStock }}.
        </p>
        <button class="primary-button" type="submit" :disabled="adjusting || !adjustmentIsValid">
          {{ adjusting ? 'Saving Adjustment...' : 'Save Stock Adjustment' }}
        </button>
      </form>
    </section>
    <LoadingState v-if="loading" message="Loading inventory warnings..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else>
      <EmptyState
        v-if="!warnings.length"
        title="Inventory is healthy"
        message="No products are at or below the current warning threshold."
      />
      <div v-else class="warning-grid">
        <article v-for="warning in warnings" :key="warning.productId" class="warning-card">
          <div>
            <p class="eyebrow">{{ warning.categoryName }}</p>
            <h2>{{ warning.productName }}</h2>
            <StatusBadge :value="warning.status" :label="formatStatus(warning.status)" />
            <p class="muted">Threshold: {{ warning.lowStockThreshold }} units</p>
          </div>
          <div class="inventory-count">
            <strong>{{ warning.stockQuantity }}</strong>
            <span>{{ warning.stockQuantity === 1 ? 'unit left' : 'units left' }}</span>
          </div>
        </article>
      </div>

      <section class="inventory-movement-section">
        <h2>Recent Stock Movements</h2>
        <EmptyState
          v-if="!movements.length"
          title="No stock movement yet"
          message="Checkout deductions and cancellation restorations will appear here."
        />
        <div v-else class="admin-table-wrap">
          <table class="admin-table compact-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Movement</th>
                <th>Change</th>
                <th>Stock After</th>
                <th>Recorded</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="movement in movements" :key="movement.id">
                <td>
                  <strong>{{ movement.productName }}</strong>
                  <span v-if="movement.orderId">Order #{{ movement.orderId }}</span>
                </td>
                <td><StatusBadge :value="movement.type" :label="formatStatus(movement.type)" /></td>
                <td>{{ movement.quantityChange > 0 ? '+' : '' }}{{ movement.quantityChange }}</td>
                <td>{{ movement.stockAfter }}</td>
                <td>{{ formatDate(movement.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { adjustInventory, fetchAdminProducts, fetchInventoryMovements, fetchInventoryWarnings } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useAuthStore } from '../../stores/auth'
import { usePlatformStore } from '../../stores/platform'
import { isLocalDemoAdminSession, localInventoryMovementsForStore, localInventoryWarnings, shouldUseLocalDemoFallback } from '../../utils/demoAdmin'
import { formatDate, formatStatus } from '../../utils/format'
import { saveStoreStockMovement } from '../../utils/orderTracking'

const platformStore = usePlatformStore()
const authStore = useAuthStore()
const threshold = ref(5)
const warnings = ref([])
const movements = ref([])
const products = ref([])
const loading = ref(true)
const error = ref('')
const adjusting = ref(false)
const usingLocalDemo = ref(false)
const toastMessage = ref('')
let toastTimer
const adjustment = reactive({
  productId: '',
  quantityChange: 0,
  reason: ''
})
const currentStore = computed(() => platformStore.currentStore)
const selectedAdjustmentProduct = computed(() => {
  return products.value.find((product) => String(product.id) === adjustment.productId)
})
const projectedStock = computed(() => {
  if (!selectedAdjustmentProduct.value) return 0
  return selectedAdjustmentProduct.value.stockQuantity + Number(adjustment.quantityChange || 0)
})
const adjustmentIsValid = computed(() => {
  return Boolean(selectedAdjustmentProduct.value)
    && Number.isInteger(Number(adjustment.quantityChange))
    && Number(adjustment.quantityChange) !== 0
    && projectedStock.value >= 0
    && Boolean(adjustment.reason)
})

onMounted(loadWarnings)

async function loadWarnings() {
  platformStore.loadPlatform()
  if (!Number.isFinite(threshold.value) || threshold.value < 0) {
    error.value = 'Inventory threshold cannot be negative.'
    warnings.value = []
    return
  }

  threshold.value = Math.floor(threshold.value)
  loading.value = true
  error.value = ''
  if (isLocalDemoAdminSession(authStore)) {
    loadLocalInventoryData()
    loading.value = false
    return
  }
  try {
    const [warningData, movementData, productData] = await Promise.all([
      fetchInventoryWarnings(threshold.value),
      fetchInventoryMovements(),
      fetchAdminProducts()
    ])
    warnings.value = warningData
    movements.value = movementData
    products.value = productData
    usingLocalDemo.value = false
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) {
      error.value = getApiError(requestError, 'Inventory warnings could not be loaded.')
    } else {
      loadLocalInventoryData()
      error.value = ''
    }
  } finally {
    loading.value = false
  }
}

function loadLocalInventoryData() {
  warnings.value = localInventoryWarnings(currentStore.value, threshold.value)
  movements.value = localInventoryMovementsForStore(currentStore.value.slug)
  products.value = currentStore.value.products || []
  usingLocalDemo.value = true
}

async function submitAdjustment() {
  if (!adjustmentIsValid.value) {
    error.value = projectedStock.value < 0
      ? 'Inventory adjustment cannot reduce stock below zero.'
      : 'Choose a product, enter a non-zero quantity change, and add a reason.'
    return
  }

  adjusting.value = true
  error.value = ''
  try {
    if (usingLocalDemo.value) {
      const quantityChange = Number(adjustment.quantityChange)
      platformStore.applyInventoryChange(
        currentStore.value.slug,
        [{ productId: Number(adjustment.productId), quantity: Math.abs(quantityChange) }],
        quantityChange > 0 ? 1 : -1
      ).forEach((movement) => {
        saveStoreStockMovement({
          ...movement,
          storeSlug: currentStore.value.slug,
          type: 'MANUAL_ADJUSTMENT',
          reason: adjustment.reason
        })
      })
      adjustment.productId = ''
      adjustment.quantityChange = 0
      adjustment.reason = ''
      showToast('Local demo inventory adjustment saved.')
      await loadWarnings()
      return
    }
    await adjustInventory({
      productId: Number(adjustment.productId),
      quantityChange: Number(adjustment.quantityChange),
      reason: adjustment.reason
    })
    adjustment.productId = ''
    adjustment.quantityChange = 0
    adjustment.reason = ''
    showToast('Inventory adjustment saved.')
    await loadWarnings()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Inventory adjustment could not be saved.')
  } finally {
    adjusting.value = false
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
