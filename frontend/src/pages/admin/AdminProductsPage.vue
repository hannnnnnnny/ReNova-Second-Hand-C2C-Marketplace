<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Catalog"
      title="Products"
      description="Search products, review inventory levels, and manage storefront visibility."
    >
      <template #actions>
        <RouterLink class="primary-button" to="/admin/products/new">New Product</RouterLink>
      </template>
    </PageHeader>
    <div class="catalog-toolbar admin-toolbar">
      <label class="search-field">
        Search products
        <input v-model.trim="searchTerm" type="search" placeholder="Search by product or category" />
      </label>
      <label>
        Status
        <select v-model="statusFilter">
          <option value="all">All statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="DRAFT">Draft</option>
          <option value="ARCHIVED">Archived</option>
          <option value="low-stock">Low stock</option>
          <option value="out-of-stock">Out of stock</option>
        </select>
      </label>
    </div>
    <LoadingState v-if="loading" message="Loading products..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState
      v-else-if="!filteredProducts.length"
      title="No products yet"
      message="Adjust the search or create a new product for the catalog."
    />
    <div v-else class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Category</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in filteredProducts" :key="product.id">
            <td>
              <strong>{{ product.name }}</strong>
              <span>{{ product.brand || 'Unbranded' }} · {{ product.sku || product.slug }}</span>
            </td>
            <td>{{ product.category?.name }}</td>
            <td>
              <strong>{{ formatCurrency(product.price) }}</strong>
              <span v-if="product.compareAtPrice">{{ formatCurrency(product.compareAtPrice) }} compare-at</span>
            </td>
            <td><StatusBadge :value="stockStatus(product)" :label="stockLabel(product)" /></td>
            <td>
              <div class="status-stack">
                <StatusBadge :value="product.status" :label="formatStatus(product.status)" />
                <span v-if="product.featured" class="featured-marker">Featured</span>
              </div>
            </td>
            <td>
              <div class="table-actions">
                <RouterLink class="text-link" :to="`/admin/products/${product.id}/edit`">Edit</RouterLink>
                <button class="text-button danger" type="button" @click="removeProduct(product)">Delete</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { deleteAdminProduct, fetchAdminProducts } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatCurrency, formatStatus } from '../../utils/format'

const loading = ref(true)
const error = ref('')
const products = ref([])
const searchTerm = ref('')
const statusFilter = ref('all')
const filteredProducts = computed(() => {
  const query = searchTerm.value.toLowerCase()
  return products.value.filter((product) => {
    const matchesQuery = query
      ? `${product.name} ${product.slug} ${product.sku || ''} ${product.brand || ''} ${product.category?.name || ''}`.toLowerCase().includes(query)
      : true
    const matchesStatus = statusFilter.value === 'all'
      || (statusFilter.value === product.status)
      || (statusFilter.value === 'low-stock' && product.stockQuantity > 0 && product.stockQuantity <= (product.lowStockThreshold ?? 5))
      || (statusFilter.value === 'out-of-stock' && product.stockQuantity === 0)
    return matchesQuery && matchesStatus
  })
})

onMounted(loadProducts)

async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    products.value = await fetchAdminProducts()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Products could not be loaded.')
  } finally {
    loading.value = false
  }
}

async function removeProduct(product) {
  const confirmed = window.confirm(`Delete ${product.name}? This action cannot be undone.`)
  if (!confirmed) return

  try {
    await deleteAdminProduct(product.id)
    products.value = products.value.filter((entry) => entry.id !== product.id)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product could not be deleted.')
  }
}

function stockStatus(product) {
  if (product.stockQuantity < 1) return 'out of stock'
  if (product.stockQuantity <= (product.lowStockThreshold ?? 5)) return 'low stock'
  return 'in stock'
}

function stockLabel(product) {
  if (product.stockQuantity < 1) return 'Out of stock'
  if (product.stockQuantity <= (product.lowStockThreshold ?? 5)) return `${product.stockQuantity} left`
  return `${product.stockQuantity} in stock`
}
</script>
