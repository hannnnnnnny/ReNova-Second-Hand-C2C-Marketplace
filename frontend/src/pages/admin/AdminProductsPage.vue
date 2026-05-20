<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Store catalog"
      title="Products"
      :description="`Search products for ${currentStore.name}, review inventory levels, and manage storefront visibility.`"
    >
      <template #actions>
        <RouterLink class="primary-button" to="/admin/products/new">New Product</RouterLink>
      </template>
    </PageHeader>
    <section class="admin-visual-summary">
      <article>
        <span>Visible Products</span>
        <strong>{{ products.filter((product) => product.status === 'ACTIVE').length }}</strong>
      </article>
      <article>
        <span>Sale Products</span>
        <strong>{{ products.filter((product) => product.discountPercent || product.tags?.includes('sale')).length }}</strong>
      </article>
      <article>
        <span>Low Stock</span>
        <strong>{{ products.filter((product) => product.stockQuantity > 0 && product.stockQuantity <= (product.lowStockThreshold ?? 5)).length }}</strong>
      </article>
      <article>
        <span>Archived</span>
        <strong>{{ products.filter((product) => product.status === 'ARCHIVED').length }}</strong>
      </article>
    </section>
    <div class="catalog-toolbar admin-toolbar">
      <label class="search-field">
        Search products
        <input v-model.trim="searchTerm" type="search" placeholder="Search by product, label, SKU, or category" />
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
      <label>
        Category
        <select v-model="categoryFilter">
          <option value="all">All categories</option>
          <option v-for="category in categories" :key="category.id" :value="String(category.id)">
            {{ category.name }}
          </option>
        </select>
      </label>
      <label>
        Collection
        <select v-model="collectionFilter">
          <option value="all">All collections</option>
          <option v-for="collection in collections" :key="collection.id" :value="String(collection.id)">
            {{ collection.name }}
          </option>
        </select>
      </label>
      <label class="toggle-control">
        <input v-model="saleFilter" type="checkbox" />
        Sale products
      </label>
      <button class="secondary-button" type="button" :disabled="!selectedIds.length" @click="bulkArchive">
        Archive Selected
      </button>
      <label>
        Bulk collection
        <select v-model="bulkCollectionId">
          <option value="">Choose collection</option>
          <option v-for="collection in collections" :key="collection.id" :value="String(collection.id)">
            {{ collection.name }}
          </option>
        </select>
      </label>
      <button class="secondary-button" type="button" :disabled="!selectedIds.length || !bulkCollectionId" @click="bulkAssignCollection">
        Assign Collection
      </button>
    </div>
    <LoadingState v-if="loading" message="Loading products..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState
      v-else-if="!filteredProducts.length"
      title="No products yet"
      message="Adjust the search or create a new product for the catalog."
    />
    <div v-else class="admin-product-workspace">
      <div v-if="selectedIds.length" class="bulk-action-bar">
        <strong>{{ selectedIds.length }} selected</strong>
        <span>Archive, assign a collection, or create a markdown for the selected fashion products.</span>
        <label class="bulk-inline-field">
          Markdown %
          <input v-model.number="bulkDiscountValue" min="1" max="95" step="1" type="number" />
        </label>
        <button
          class="secondary-button compact-button"
          type="button"
          :disabled="bulkDiscountSaving || !bulkDiscountIsValid"
          @click="bulkApplyDiscount"
        >
          {{ bulkDiscountSaving ? 'Creating...' : 'Create Discount' }}
        </button>
      </div>
      <div class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th><input type="checkbox" :checked="allSelected" aria-label="Select all visible products" @change="toggleAll($event.target.checked)" /></th>
            <th>Product</th>
            <th>Category</th>
            <th>Collection</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in filteredProducts" :key="product.id">
            <td><input v-model="selectedIds" type="checkbox" :value="product.id" :aria-label="`Select ${product.name}`" /></td>
            <td>
              <div class="admin-product-cell">
                <img :src="product.imageUrl" :alt="product.name" />
                <div>
                  <strong>{{ product.name }}</strong>
                  <span>{{ product.brand || 'Unbranded' }} / {{ product.sku || product.slug }}</span>
                  <small v-if="product.sizes?.length || product.colors?.length">{{ (product.sizes || []).slice(0, 4).join(' / ') }} / {{ (product.colors || []).slice(0, 3).join(', ') }}</small>
                </div>
              </div>
            </td>
            <td>{{ product.category?.name }}</td>
            <td>{{ product.collection?.name || 'No collection' }}</td>
            <td>
              <strong>{{ formatCurrency(product.effectivePrice ?? product.price) }}</strong>
              <span v-if="product.compareAtPrice || Number(product.discountAmount) > 0">{{ formatCurrency(product.compareAtPrice || product.price) }} compare-at</span>
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
                <RouterLink class="text-link" :to="{ path: `/store/${currentStore.slug}/products`, query: { category: product.category?.name || '' } }">Preview</RouterLink>
                <RouterLink class="text-link" :to="`/admin/products/${product.id}/edit`">Edit</RouterLink>
                <button
                  class="text-button danger"
                  type="button"
                  @click="toggleArchive(product)"
                >
                  {{ product.status === 'ARCHIVED' ? 'Reactivate' : 'Archive' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      </div>
    </div>
    <ConfirmationDialog v-bind="confirmation" @confirm="confirm" @cancel="cancel" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { createAdminPromotion, fetchAdminCategories, fetchAdminCollections, fetchAdminProducts, updateAdminProduct } from '../../api/admin'
import { getApiError } from '../../api/client'
import ConfirmationDialog from '../../components/ConfirmationDialog.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { useConfirmDialog } from '../../composables/useConfirmDialog'
import { usePlatformStore } from '../../stores/platform'
import { formatCurrency, formatStatus } from '../../utils/format'

const platformStore = usePlatformStore()
const { confirmation, requestConfirmation, confirm, cancel } = useConfirmDialog()
const loading = ref(true)
const error = ref('')
const route = useRoute()
const products = ref([])
const categories = ref([])
const collections = ref([])
const searchTerm = ref('')
const statusFilter = ref('all')
const categoryFilter = ref('all')
const collectionFilter = ref('all')
const saleFilter = ref(false)
const selectedIds = ref([])
const bulkCollectionId = ref('')
const bulkDiscountValue = ref(15)
const bulkDiscountSaving = ref(false)
const currentStore = computed(() => platformStore.currentStore)
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
    const matchesCategory = categoryFilter.value === 'all' || String(product.category?.id) === categoryFilter.value
    const matchesCollection = collectionFilter.value === 'all' || String(product.collection?.id) === collectionFilter.value
    const matchesSale = !saleFilter.value || Boolean(product.discountPercent || product.tags?.includes('sale'))
    return matchesQuery && matchesStatus && matchesCategory && matchesCollection && matchesSale
  })
})
const allSelected = computed(() => filteredProducts.value.length > 0 && filteredProducts.value.every((product) => selectedIds.value.includes(product.id)))
const bulkDiscountIsValid = computed(() => {
  const value = Number(bulkDiscountValue.value)
  return selectedIds.value.length > 0 && Number.isFinite(value) && value > 0 && value <= 95
})

onMounted(() => {
  platformStore.loadPlatform()
  loadProducts()
})

watch(
  () => route.query.search,
  (value) => {
    searchTerm.value = value ? String(value) : ''
  },
  { immediate: true }
)

async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    const [productData, categoryData, collectionData] = await Promise.all([
      fetchAdminProducts(),
      categories.value.length ? Promise.resolve(categories.value) : fetchAdminCategories(),
      collections.value.length ? Promise.resolve(collections.value) : fetchAdminCollections()
    ])
    products.value = productData
    categories.value = categoryData
    collections.value = collectionData
    selectedIds.value = []
  } catch (requestError) {
    error.value = getApiError(requestError, 'Products could not be loaded.')
  } finally {
    loading.value = false
  }
}

async function toggleArchive(product) {
  const archived = product.status === 'ARCHIVED'
  const nextStatus = archived ? 'ACTIVE' : 'ARCHIVED'
  const confirmed = await requestConfirmation({
    eyebrow: 'Product visibility',
    title: `${archived ? 'Reactivate' : 'Archive'} ${product.name}?`,
    message: archived
      ? 'The product will become visible again anywhere active catalog filters include it.'
      : 'The product will be hidden from storefront shopping while remaining available in admin records.',
    confirmLabel: archived ? 'Reactivate product' : 'Archive product',
    tone: archived ? 'default' : 'danger'
  })
  if (!confirmed) return

  try {
    const updated = await updateAdminProduct(product.id, productPayload(product, {
      status: nextStatus,
      active: nextStatus === 'ACTIVE'
    }))
    Object.assign(product, updated)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product status could not be updated.')
  }
}

function toggleAll(checked) {
  selectedIds.value = checked ? filteredProducts.value.map((product) => product.id) : []
}

async function bulkArchive() {
  const confirmed = await requestConfirmation({
    eyebrow: 'Bulk catalog action',
    title: `Archive ${selectedIds.value.length} selected products?`,
    message: 'Selected products will leave the customer storefront but remain in merchant admin for reporting and reactivation.',
    confirmLabel: 'Archive selected',
    tone: 'danger'
  })
  if (!confirmed) return
  try {
    await Promise.all(selectedProducts().map((product) => updateAdminProduct(product.id, productPayload(product, { status: 'ARCHIVED', active: false }))))
    await loadProducts()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Selected products could not be archived.')
  }
}

async function bulkAssignCollection() {
  const collection = collections.value.find((entry) => String(entry.id) === bulkCollectionId.value)
  if (!collection) {
    error.value = 'Choose a collection before assigning selected products.'
    return
  }
  const confirmed = await requestConfirmation({
    eyebrow: 'Bulk merchandising',
    title: `Assign ${selectedIds.value.length} selected products to ${collection.name}?`,
    message: 'Collection placement affects merchandising stories, promotion targeting, and admin reporting.',
    confirmLabel: 'Assign collection'
  })
  if (!confirmed) return

  try {
    await Promise.all(selectedProducts().map((product) => updateAdminProduct(
      product.id,
      productPayload(product, { collectionId: collection.id })
    )))
    bulkCollectionId.value = ''
    await loadProducts()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Selected products could not be assigned to the collection.')
  }
}

async function bulkApplyDiscount() {
  const discountValue = Number(bulkDiscountValue.value)
  if (!bulkDiscountIsValid.value) {
    error.value = 'Enter a percentage discount between 1 and 95 for selected products.'
    return
  }
  bulkDiscountSaving.value = true
  try {
    await createAdminPromotion({
      name: `Selected product markdown ${new Date().toISOString().slice(0, 10)}`,
      description: 'Bulk promotion created from selected admin products.',
      discountType: 'PERCENTAGE',
      discountValue,
      startDate: null,
      endDate: null,
      active: true,
      targetType: 'SELECTED_PRODUCTS',
      targetValues: selectedIds.value.map(String)
    })
    bulkDiscountValue.value = 15
    await loadProducts()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Bulk discount could not be created.')
  } finally {
    bulkDiscountSaving.value = false
  }
}

function selectedProducts() {
  return products.value.filter((product) => selectedIds.value.includes(product.id))
}

function productPayload(product, overrides = {}) {
  return {
    name: product.name,
    slug: product.slug,
    sku: product.sku,
    brand: product.brand,
    description: product.description,
    price: product.price,
    compareAtPrice: product.compareAtPrice,
    stockQuantity: product.stockQuantity,
    lowStockThreshold: product.lowStockThreshold,
    imageUrl: product.imageUrl,
    imageGallery: product.imageGallery || [product.imageUrl],
    tags: product.tags || [],
    sizes: product.sizes || [],
    colors: product.colors || [],
    material: product.material || null,
    careInstructions: product.careInstructions || null,
    season: product.season || null,
    genderTarget: product.genderTarget || 'UNISEX',
    featured: product.featured,
    status: product.status,
    active: product.active,
    categoryId: product.category?.id,
    collectionId: product.collection?.id || null,
    ...overrides
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
