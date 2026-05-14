<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Inventory"
      title="Stock Watch"
      description="Review products that need replenishment before customers hit checkout limits."
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
    <LoadingState v-if="loading" message="Loading inventory warnings..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState
      v-else-if="!warnings.length"
      title="Inventory is healthy"
      message="No products are at or below the current warning threshold."
    />
    <div v-else class="warning-grid">
      <article v-for="warning in warnings" :key="warning.productId" class="warning-card">
        <div>
          <p class="eyebrow">{{ warning.categoryName }}</p>
          <h2>{{ warning.productName }}</h2>
          <StatusBadge :value="warning.active ? 'active' : 'inactive'" :label="warning.active ? 'Active product' : 'Inactive product'" />
        </div>
        <div class="inventory-count">
          <strong>{{ warning.stockQuantity }}</strong>
          <span>{{ warning.stockQuantity === 1 ? 'unit left' : 'units left' }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchInventoryWarnings } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'

const threshold = ref(5)
const warnings = ref([])
const loading = ref(true)
const error = ref('')

onMounted(loadWarnings)

async function loadWarnings() {
  loading.value = true
  error.value = ''
  try {
    warnings.value = await fetchInventoryWarnings(threshold.value)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Inventory warnings could not be loaded.')
  } finally {
    loading.value = false
  }
}
</script>
