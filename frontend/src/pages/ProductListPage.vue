<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Storefront"
      title="Fashion Catalog"
      description="Search clothing, bags, jewelry, shoes, sportswear, equipment, accessories, seasonal edits, and sale pieces."
    />

    <div class="catalog-browse-layout">
      <aside class="catalog-filter-panel" aria-label="Catalog filters">
        <form class="filter-form" @submit.prevent="applyFilters">
          <label class="search-field">
            Search products
            <input v-model.trim="searchTerm" type="search" placeholder="Search by style, label, category, tag, or SKU" />
          </label>
          <label>
            Category
            <select v-model="selectedCategoryId">
              <option value="">All categories</option>
              <option v-for="category in categories" :key="category.id" :value="String(category.id)">
                {{ category.name }}
              </option>
            </select>
          </label>
          <div class="price-filter-grid">
            <label>
              Min price
              <input v-model.number="minPrice" min="0" step="1" type="number" placeholder="0" />
            </label>
            <label>
              Max price
              <input v-model.number="maxPrice" min="0" step="1" type="number" placeholder="200" />
            </label>
          </div>
          <label>
            Sort by
            <select v-model="sortMode" @change="applyFilters">
              <option value="name">Name</option>
              <option value="newest">Newest</option>
              <option value="price-low">Price: low to high</option>
              <option value="price-high">Price: high to low</option>
              <option value="stock">Stock availability</option>
            </select>
          </label>
          <label class="toggle-control">
            <input v-model="availableOnly" type="checkbox" @change="applyFilters" />
            Show available only
          </label>
          <div class="filter-actions">
            <button class="primary-button" type="submit">Apply Filters</button>
            <button v-if="hasActiveFilters" class="secondary-button" type="button" @click="clearFilters">Clear</button>
          </div>
        </form>
      </aside>

      <div class="catalog-results-panel">
        <LoadingState v-if="loading" message="Loading products..." />
        <ErrorMessage v-else-if="error" :message="error" />
        <div v-else-if="products.length">
          <div class="catalog-result-bar">
            <p class="result-count">
              {{ pageInfo.totalElements }} {{ pageInfo.totalElements === 1 ? 'product' : 'products' }} found
              <span v-if="selectedCategoryLabel">in {{ selectedCategoryLabel }}</span>
            </p>
            <div v-if="activeFilterChips.length" class="filter-chip-row" aria-label="Active filters">
              <button
                v-for="chip in activeFilterChips"
                :key="chip.key"
                class="filter-chip"
                type="button"
                @click="removeFilter(chip.key)"
              >
                {{ chip.label }}
              </button>
            </div>
          </div>
          <div class="product-grid">
            <ProductCard
              v-for="product in products"
              :key="product.id"
              :product="product"
              @added="showAddedMessage"
            />
          </div>
          <nav v-if="pageInfo.totalPages > 1" class="pagination-bar" aria-label="Catalog pagination">
            <button class="secondary-button" type="button" :disabled="pageInfo.first" @click="goToPage(pageInfo.page - 1)">
              Previous
            </button>
            <span>Page {{ pageInfo.page + 1 }} of {{ pageInfo.totalPages }}</span>
            <button class="secondary-button" type="button" :disabled="pageInfo.last" @click="goToPage(pageInfo.page + 1)">
              Next
            </button>
          </nav>
        </div>
        <EmptyState
          v-else
          title="No matching fashion products"
          message="Try clearing filters, widening the price range, or searching another category, color story, or collection tag."
        >
          <button class="secondary-button" type="button" @click="clearFilters">Clear Filters</button>
        </EmptyState>
      </div>
    </div>

    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchCategories, fetchProductPage } from '../api/catalog'
import EmptyState from '../components/EmptyState.vue'
import ErrorMessage from '../components/ErrorMessage.vue'
import LoadingState from '../components/LoadingState.vue'
import PageHeader from '../components/PageHeader.vue'
import ProductCard from '../components/ProductCard.vue'
import ToastMessage from '../components/ToastMessage.vue'
import { getApiError } from '../api/client'
import { formatCurrency } from '../utils/format'

const route = useRoute()
const pageSize = 9
const loading = ref(true)
const error = ref('')
const products = ref([])
const categories = ref([])
const selectedCategoryId = ref('')
const searchTerm = ref('')
const sortMode = ref('name')
const availableOnly = ref(false)
const minPrice = ref('')
const maxPrice = ref('')
const pageInfo = ref({
  page: 0,
  size: pageSize,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  empty: true
})
const toastMessage = ref('')
let toastTimer

const selectedCategoryLabel = computed(() => {
  return categories.value.find((category) => String(category.id) === selectedCategoryId.value)?.name || ''
})

const hasActiveFilters = computed(() => {
  return Boolean(searchTerm.value)
    || Boolean(selectedCategoryId.value)
    || sortMode.value !== 'name'
    || availableOnly.value
    || hasPrice(minPrice.value)
    || hasPrice(maxPrice.value)
})

const activeFilterChips = computed(() => {
  const chips = []
  if (searchTerm.value) chips.push({ key: 'search', label: `Search: ${searchTerm.value}` })
  if (selectedCategoryLabel.value) chips.push({ key: 'category', label: selectedCategoryLabel.value })
  if (availableOnly.value) chips.push({ key: 'available', label: 'Available only' })
  if (hasPrice(minPrice.value)) chips.push({ key: 'minPrice', label: `From ${formatCurrency(minPrice.value)}` })
  if (hasPrice(maxPrice.value)) chips.push({ key: 'maxPrice', label: `Up to ${formatCurrency(maxPrice.value)}` })
  if (sortMode.value !== 'name') chips.push({ key: 'sort', label: sortLabel.value })
  return chips
})

const sortLabel = computed(() => {
  const labels = {
    newest: 'Newest',
    'price-low': 'Price: low to high',
    'price-high': 'Price: high to low',
    stock: 'Stock availability'
  }
  return labels[sortMode.value] || 'Name'
})

onMounted(async () => {
  const categoryFromQuery = Number(route.query.category)
  selectedCategoryId.value = Number.isFinite(categoryFromQuery) && categoryFromQuery > 0 ? String(categoryFromQuery) : ''
  await loadProducts(0)
})

async function loadProducts(page = 0) {
  loading.value = true
  error.value = ''
  try {
    if (!categories.value.length) {
      categories.value = await fetchCategories()
    }
    const productPage = await fetchProductPage(buildQuery(page))
    products.value = productPage.content
    pageInfo.value = productPage
  } catch (requestError) {
    error.value = getApiError(requestError, 'Products could not be loaded.')
  } finally {
    loading.value = false
  }
}

function buildQuery(page) {
  return {
    search: searchTerm.value || undefined,
    categoryId: selectedCategoryId.value || undefined,
    minPrice: hasPrice(minPrice.value) ? minPrice.value : undefined,
    maxPrice: hasPrice(maxPrice.value) ? maxPrice.value : undefined,
    availableOnly: availableOnly.value,
    sort: sortMode.value,
    page,
    size: pageSize
  }
}

function applyFilters() {
  loadProducts(0)
}

function clearFilters() {
  selectedCategoryId.value = ''
  searchTerm.value = ''
  sortMode.value = 'name'
  availableOnly.value = false
  minPrice.value = ''
  maxPrice.value = ''
  loadProducts(0)
}

function removeFilter(key) {
  if (key === 'search') searchTerm.value = ''
  if (key === 'category') selectedCategoryId.value = ''
  if (key === 'available') availableOnly.value = false
  if (key === 'minPrice') minPrice.value = ''
  if (key === 'maxPrice') maxPrice.value = ''
  if (key === 'sort') sortMode.value = 'name'
  loadProducts(0)
}

function goToPage(page) {
  loadProducts(page)
}

function hasPrice(value) {
  return value !== '' && Number.isFinite(Number(value))
}

function showAddedMessage(product) {
  toastMessage.value = `${product.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2600)
}
</script>
