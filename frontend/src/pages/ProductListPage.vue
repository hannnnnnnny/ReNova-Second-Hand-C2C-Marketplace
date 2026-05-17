<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Storefront"
      title="Fashion Catalog"
      description="Search clothing, bags, jewelry, shoes, sportswear, equipment, accessories, seasonal edits, and sale pieces."
    />

    <button class="secondary-button mobile-filter-toggle" type="button" @click="filtersOpen = !filtersOpen">
      {{ filtersOpen ? 'Hide Filters' : 'Show Filters' }}
    </button>

    <div class="catalog-browse-layout">
      <aside class="catalog-filter-panel" :class="{ open: filtersOpen }" aria-label="Catalog filters">
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
          <label>
            Collection
            <select v-model="selectedCollectionId">
              <option value="">All collections</option>
              <option v-for="collection in collections" :key="collection.id" :value="String(collection.id)">
                {{ collection.name }}
              </option>
            </select>
          </label>
          <label>
            Size
            <select v-model="selectedSize">
              <option value="">All sizes</option>
              <option v-for="size in sizeOptions" :key="size" :value="size">{{ size }}</option>
            </select>
          </label>
          <label>
            Color
            <select v-model="selectedColor">
              <option value="">All colors</option>
              <option v-for="color in colorOptions" :key="color" :value="color">{{ color }}</option>
            </select>
          </label>
          <label>
            Material
            <select v-model="selectedMaterial">
              <option value="">All materials</option>
              <option v-for="material in materialOptions" :key="material" :value="material">{{ material }}</option>
            </select>
          </label>
          <label>
            Label
            <select v-model="selectedBrand">
              <option value="">All labels</option>
              <option v-for="brand in brandOptions" :key="brand" :value="brand">{{ brand }}</option>
            </select>
          </label>
          <label>
            Season
            <select v-model="selectedSeason">
              <option value="">All seasons</option>
              <option v-for="season in seasonOptions" :key="season" :value="season">{{ season }}</option>
            </select>
          </label>
          <label>
            Style Tag
            <select v-model="selectedTag">
              <option value="">All tags</option>
              <option v-for="tag in tagOptions" :key="tag" :value="tag">{{ tag }}</option>
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
              <option value="best-selling">Best selling</option>
              <option value="discount">Discount</option>
            </select>
          </label>
          <label class="toggle-control">
            <input v-model="availableOnly" type="checkbox" @change="applyFilters" />
            Show available only
          </label>
          <label class="toggle-control">
            <input v-model="saleOnly" type="checkbox" @change="applyFilters" />
            Sale only
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
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchCategories, fetchCollections, fetchProductPage } from '../api/catalog'
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
const collections = ref([])
const selectedCategoryId = ref('')
const selectedCollectionId = ref('')
const selectedSize = ref('')
const selectedColor = ref('')
const selectedMaterial = ref('')
const selectedBrand = ref('')
const selectedSeason = ref('')
const selectedTag = ref('')
const searchTerm = ref('')
const sortMode = ref('name')
const availableOnly = ref(false)
const saleOnly = ref(false)
const filtersOpen = ref(false)
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
const selectedCollectionLabel = computed(() => {
  return collections.value.find((collection) => String(collection.id) === selectedCollectionId.value)?.name || ''
})
const sizeOptions = ['One Size', 'XS', 'S', 'M', 'L', 'XL', '5', '6', '7', '8', '9', '10', '11']
const colorOptions = ['Black', 'Ivory', 'Taupe', 'Sand', 'Sky', 'Pearl', 'Wine', 'Slate', 'Pine', 'Gold', 'Silver']
const materialOptions = ['Cotton blend', 'Linen', 'Silk blend', 'Leather', 'Satin', 'Performance blend', 'Wool blend']
const brandOptions = ['Aster Row', 'Linden Vale', 'Rue Forme', 'Meridian Atelier', 'Harbor Finch', 'Northline Studio', 'Kinetic Loom', 'Solace Field', 'Vale & Thread']
const seasonOptions = ['Spring 2026', 'Summer 2026', 'Fall Winter 2026', 'Active Weekend', 'Evening Edit', 'Last Season']
const tagOptions = ['active-weekend', 'capsule', 'equipment', 'event', 'jewelry', 'linen', 'resort', 'sale', 'tailoring', 'workwear']

const hasActiveFilters = computed(() => {
  return Boolean(searchTerm.value)
    || Boolean(selectedCategoryId.value)
    || Boolean(selectedCollectionId.value)
    || Boolean(selectedSize.value)
    || Boolean(selectedColor.value)
    || Boolean(selectedMaterial.value)
    || Boolean(selectedBrand.value)
    || Boolean(selectedSeason.value)
    || Boolean(selectedTag.value)
    || sortMode.value !== 'name'
    || availableOnly.value
    || saleOnly.value
    || hasPrice(minPrice.value)
    || hasPrice(maxPrice.value)
})

const activeFilterChips = computed(() => {
  const chips = []
  if (searchTerm.value) chips.push({ key: 'search', label: `Search: ${searchTerm.value}` })
  if (selectedCategoryLabel.value) chips.push({ key: 'category', label: selectedCategoryLabel.value })
  if (selectedCollectionLabel.value) chips.push({ key: 'collection', label: selectedCollectionLabel.value })
  if (selectedSize.value) chips.push({ key: 'size', label: `Size: ${selectedSize.value}` })
  if (selectedColor.value) chips.push({ key: 'color', label: selectedColor.value })
  if (selectedMaterial.value) chips.push({ key: 'material', label: selectedMaterial.value })
  if (selectedBrand.value) chips.push({ key: 'brand', label: selectedBrand.value })
  if (selectedSeason.value) chips.push({ key: 'season', label: selectedSeason.value })
  if (selectedTag.value) chips.push({ key: 'tag', label: `Tag: ${selectedTag.value}` })
  if (availableOnly.value) chips.push({ key: 'available', label: 'Available only' })
  if (saleOnly.value) chips.push({ key: 'sale', label: 'Sale only' })
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
    'best-selling': 'Best selling',
    discount: 'Discount'
  }
  return labels[sortMode.value] || 'Name'
})

watch(
  () => route.query,
  async (query) => {
    syncFiltersFromQuery(query)
    await loadProducts(0)
  },
  { immediate: true }
)

async function loadProducts(page = 0) {
  loading.value = true
  error.value = ''
  try {
    if (!categories.value.length) {
      const [categoryData, collectionData] = await Promise.all([fetchCategories(), fetchCollections()])
      categories.value = categoryData
      collections.value = collectionData
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
    collectionId: selectedCollectionId.value || undefined,
    sizeFilter: selectedSize.value || undefined,
    color: selectedColor.value || undefined,
    material: selectedMaterial.value || undefined,
    brand: selectedBrand.value || undefined,
    season: selectedSeason.value || undefined,
    tag: selectedTag.value || undefined,
    saleOnly: saleOnly.value || undefined,
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
  selectedCollectionId.value = ''
  selectedSize.value = ''
  selectedColor.value = ''
  selectedMaterial.value = ''
  selectedBrand.value = ''
  selectedSeason.value = ''
  selectedTag.value = ''
  searchTerm.value = ''
  sortMode.value = 'name'
  availableOnly.value = false
  saleOnly.value = false
  minPrice.value = ''
  maxPrice.value = ''
  loadProducts(0)
}

function removeFilter(key) {
  if (key === 'search') searchTerm.value = ''
  if (key === 'category') selectedCategoryId.value = ''
  if (key === 'collection') selectedCollectionId.value = ''
  if (key === 'size') selectedSize.value = ''
  if (key === 'color') selectedColor.value = ''
  if (key === 'material') selectedMaterial.value = ''
  if (key === 'brand') selectedBrand.value = ''
  if (key === 'season') selectedSeason.value = ''
  if (key === 'tag') selectedTag.value = ''
  if (key === 'available') availableOnly.value = false
  if (key === 'sale') saleOnly.value = false
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

function syncFiltersFromQuery(query) {
  const categoryFromQuery = Number(query.category ?? query.categoryId)
  const collectionFromQuery = Number(query.collectionId)
  selectedCategoryId.value = Number.isFinite(categoryFromQuery) && categoryFromQuery > 0 ? String(categoryFromQuery) : ''
  selectedCollectionId.value = Number.isFinite(collectionFromQuery) && collectionFromQuery > 0 ? String(collectionFromQuery) : ''
  searchTerm.value = query.search ? String(query.search) : ''
  selectedSeason.value = query.season ? String(query.season) : ''
  selectedTag.value = query.tag ? String(query.tag) : ''
  saleOnly.value = query.sale === 'true' || query.saleOnly === 'true'
}

function showAddedMessage(product) {
  toastMessage.value = `${product.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2600)
}
</script>
