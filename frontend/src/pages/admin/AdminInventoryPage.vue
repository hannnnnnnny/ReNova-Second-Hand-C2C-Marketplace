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
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchInventoryMovements, fetchInventoryWarnings } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatDate, formatStatus } from '../../utils/format'

const threshold = ref(5)
const warnings = ref([])
const movements = ref([])
const loading = ref(true)
const error = ref('')

onMounted(loadWarnings)

async function loadWarnings() {
  if (!Number.isFinite(threshold.value) || threshold.value < 0) {
    error.value = 'Inventory threshold cannot be negative.'
    warnings.value = []
    return
  }

  threshold.value = Math.floor(threshold.value)
  loading.value = true
  error.value = ''
  try {
    const [warningData, movementData] = await Promise.all([
      fetchInventoryWarnings(threshold.value),
      fetchInventoryMovements()
    ])
    warnings.value = warningData
    movements.value = movementData
  } catch (requestError) {
    error.value = getApiError(requestError, 'Inventory warnings could not be loaded.')
  } finally {
    loading.value = false
  }
}
</script>
