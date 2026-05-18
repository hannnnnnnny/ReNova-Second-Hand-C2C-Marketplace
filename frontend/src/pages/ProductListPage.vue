<template>
  <div class="catalog-page retail-catalog-page">
    <StorefrontHero
      title="Classic, elegant, uniquely yours"
      description="Shop timeless fashion and one-of-a-kind finds."
      cta-label="Shop Now"
      :cta-to="{ name: 'products', query: { tag: 'new-arrival', sort: 'newest' } }"
    />

    <section class="page-section quick-shop-section">
      <div class="retail-section-heading">
        <p class="eyebrow">Quick shop</p>
        <h2>Start with the edit that fits your day</h2>
        <p>Browse the storefront visually first, then refine by size, color, price, material, label, or collection when you need to.</p>
      </div>
      <div class="category-tile-grid">
        <CategoryTile
          v-for="tile in quickShopTiles"
          :key="tile.title"
          :title="tile.title"
          :description="tile.description"
          :image="tile.image"
          :to="tile.to"
        />
      </div>
    </section>

    <section class="page-section retail-path-section">
      <div class="retail-path-card">
        <div>
          <p class="eyebrow">Shop departments</p>
          <h2>Fashion, thrift finds, lifestyle extras, and playful pieces.</h2>
        </div>
        <nav class="retail-path-links" aria-label="Additional shopping departments">
          <RouterLink v-for="path in retailPaths" :key="path.label" :to="path.to">
            {{ path.label }}
          </RouterLink>
        </nav>
      </div>
    </section>

    <section class="page-section catalog-shopping-section">
      <div class="catalog-shop-header">
        <div>
          <p class="eyebrow">Shop the catalog</p>
          <h2>{{ selectedCategoryLabel || selectedCollectionLabel || 'All NovaCart finds' }}</h2>
          <p>
            {{ pageInfo.totalElements }} {{ pageInfo.totalElements === 1 ? 'item' : 'items' }}
            <span v-if="hasActiveFilters">with active refinements</span>
          </p>
        </div>
        <div class="catalog-shop-actions">
          <SearchInput
            v-model="searchTerm"
            label="Search catalog"
            placeholder="Search dresses, bags, vintage..."
            @submit="applyFilters"
          />
          <button class="secondary-button filter-drawer-button" type="button" :aria-expanded="filtersOpen" @click="filtersOpen = !filtersOpen">
            {{ filtersOpen ? 'Hide Filters' : 'Refine' }}
          </button>
        </div>
      </div>

      <div v-if="activeFilterChips.length" class="filter-chip-row visible-filter-row" aria-label="Active filters">
        <button
          v-for="chip in activeFilterChips"
          :key="chip.key"
          class="filter-chip"
          type="button"
          @click="removeFilter(chip.key)"
        >
          {{ chip.label }}
        </button>
        <button class="text-button" type="button" @click="clearFilters">Clear all</button>
      </div>

      <div class="retail-catalog-layout" :class="{ 'filters-open': filtersOpen }">
        <aside v-if="filtersOpen" class="catalog-filter-panel catalog-filter-drawer" aria-label="Advanced catalog filters">
          <form class="filter-form" @submit.prevent="applyFilters">
            <div class="filter-panel-heading">
              <strong>Advanced filters</strong>
              <span>{{ pageInfo.totalElements }} results</span>
            </div>
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
              <input v-model="newArrivalOnly" type="checkbox" @change="applyFilters" />
              New arrivals only
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
            <div class="product-grid retail-product-grid">
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
            title="No matching finds"
            message="Try another department, clear filters, or search for a broader style cue."
          >
            <button class="secondary-button" type="button" @click="clearFilters">Clear Filters</button>
          </EmptyState>
        </div>
      </div>
    </section>

    <ToastMessage :message="toastMessage" />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchCategories, fetchCollections, fetchProductPage } from '../api/catalog'
import CategoryTile from '../components/CategoryTile.vue'
import EmptyState from '../components/EmptyState.vue'
import ErrorMessage from '../components/ErrorMessage.vue'
import LoadingState from '../components/LoadingState.vue'
import ProductCard from '../components/ProductCard.vue'
import SearchInput from '../components/SearchInput.vue'
import StorefrontHero from '../components/StorefrontHero.vue'
import ToastMessage from '../components/ToastMessage.vue'
import { getApiError } from '../api/client'
import { formatCurrency } from '../utils/format'

const route = useRoute()
const pageSize = 12
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
const sortMode = ref('newest')
const availableOnly = ref(false)
const saleOnly = ref(false)
const newArrivalOnly = ref(false)
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
const quickShopTiles = computed(() => [
  {
    title: "Women's Fashion",
    description: 'Tailored separates, soft layers, and event-ready favorites.',
    image: '/catalog/women.svg',
    to: categoryRoute('Women', { search: 'women' })
  },
  {
    title: "Men's Fashion",
    description: 'Clean shirting, outerwear, trousers, and everyday essentials.',
    image: '/catalog/men.svg',
    to: categoryRoute('Men', { search: 'men' })
  },
  {
    title: 'Shoes',
    description: 'Loafers, sneakers, sandals, boots, and easy occasion pairs.',
    image: '/catalog/shoes.svg',
    to: categoryRoute('Shoes', { search: 'shoes' })
  },
  {
    title: 'Jewelry',
    description: 'Polished finishing pieces, gifts, and small statement details.',
    image: '/catalog/jewelry.svg',
    to: categoryRoute('Jewelry', { search: 'jewelry' })
  },
  {
    title: 'Activewear',
    description: 'Training layers, sportswear, bags, and movement-ready extras.',
    image: '/catalog/sportswear.svg',
    to: categoryRoute('Sportswear', { search: 'sportswear' })
  },
  {
    title: 'Sale',
    description: 'Markdowns, last-season favorites, and limited-size finds.',
    image: '/catalog/sale.svg',
    to: { name: 'products', query: { sale: 'true', sort: 'discount' } }
  }
])
const retailPaths = [
  { label: 'New Arrivals', to: { name: 'products', query: { tag: 'new-arrival', sort: 'newest' } } },
  { label: 'Household', to: { name: 'products', query: { search: 'home' } } },
  { label: 'Kids Collection', to: { name: 'products', query: { search: 'kids' } } },
  { label: 'Vintage', to: { name: 'products', query: { search: 'vintage' } } },
  { label: 'Sports', to: { name: 'products', query: { search: 'sportswear' } } },
  { label: 'Games & Puzzles', to: { name: 'products', query: { search: 'games puzzles' } } },
  { label: 'Special Interest', to: { name: 'products', query: { search: 'collectible' } } }
]
const sizeOptions = ['One Size', 'XS', 'S', 'M', 'L', 'XL', '5', '6', '7', '8', '9', '10', '11']
const colorOptions = ['Black', 'Ivory', 'Taupe', 'Sand', 'Sky', 'Pearl', 'Wine', 'Slate', 'Pine', 'Gold', 'Silver']
const materialOptions = ['Cotton blend', 'Linen', 'Silk blend', 'Leather', 'Satin', 'Performance blend', 'Wool blend']
const brandOptions = ['Aster Row', 'Linden Vale', 'Rue Forme', 'Meridian Atelier', 'Harbor Finch', 'Northline Studio', 'Kinetic Loom', 'Solace Field', 'Vale & Thread']
const seasonOptions = ['Spring 2026', 'Summer 2026', 'Fall Winter 2026', 'Active Weekend', 'Evening Edit', 'Last Season']
const tagOptions = ['active-weekend', 'bestseller', 'capsule', 'equipment', 'event', 'jewelry', 'linen', 'new-arrival', 'resort', 'sale', 'tailoring', 'workwear']

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
    || sortMode.value !== 'newest'
    || availableOnly.value
    || saleOnly.value
    || newArrivalOnly.value
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
  if (newArrivalOnly.value) chips.push({ key: 'newArrival', label: 'New arrivals' })
  if (selectedTag.value && !newArrivalOnly.value) chips.push({ key: 'tag', label: `Tag: ${selectedTag.value}` })
  if (availableOnly.value) chips.push({ key: 'available', label: 'Available only' })
  if (saleOnly.value) chips.push({ key: 'sale', label: 'Sale only' })
  if (hasPrice(minPrice.value)) chips.push({ key: 'minPrice', label: `From ${formatCurrency(minPrice.value)}` })
  if (hasPrice(maxPrice.value)) chips.push({ key: 'maxPrice', label: `Up to ${formatCurrency(maxPrice.value)}` })
  if (sortMode.value !== 'newest') chips.push({ key: 'sort', label: sortLabel.value })
  return chips
})

const sortLabel = computed(() => {
  const labels = {
    name: 'Name',
    newest: 'Newest',
    'price-low': 'Price: low to high',
    'price-high': 'Price: high to low',
    'best-selling': 'Best selling',
    discount: 'Discount'
  }
  return labels[sortMode.value] || 'Newest'
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
    tag: newArrivalOnly.value ? 'new-arrival' : selectedTag.value || undefined,
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
  sortMode.value = 'newest'
  availableOnly.value = false
  saleOnly.value = false
  newArrivalOnly.value = false
  minPrice.value = ''
  maxPrice.value = ''
  filtersOpen.value = false
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
  if (key === 'newArrival') newArrivalOnly.value = false
  if (key === 'available') availableOnly.value = false
  if (key === 'sale') saleOnly.value = false
  if (key === 'minPrice') minPrice.value = ''
  if (key === 'maxPrice') maxPrice.value = ''
  if (key === 'sort') sortMode.value = 'newest'
  loadProducts(0)
}

function goToPage(page) {
  loadProducts(page)
}

function hasPrice(value) {
  return value !== '' && Number.isFinite(Number(value))
}

function categoryRoute(name, fallbackQuery = {}) {
  const category = categories.value.find((entry) => entry.name.toLowerCase() === name.toLowerCase())
  return category
    ? { name: 'products', query: { category: category.id, sort: 'newest' } }
    : { name: 'products', query: fallbackQuery }
}

function syncFiltersFromQuery(query) {
  const categoryFromQuery = Number(query.category ?? query.categoryId)
  const collectionFromQuery = Number(query.collectionId)
  selectedCategoryId.value = Number.isFinite(categoryFromQuery) && categoryFromQuery > 0 ? String(categoryFromQuery) : ''
  selectedCollectionId.value = Number.isFinite(collectionFromQuery) && collectionFromQuery > 0 ? String(collectionFromQuery) : ''
  searchTerm.value = query.search ? String(query.search) : ''
  selectedSeason.value = query.season ? String(query.season) : ''
  newArrivalOnly.value = query.tag === 'new-arrival'
  selectedTag.value = query.tag && query.tag !== 'new-arrival' ? String(query.tag) : ''
  saleOnly.value = query.sale === 'true' || query.saleOnly === 'true'
  sortMode.value = query.sort ? String(query.sort) : 'newest'
  selectedSize.value = query.sizeFilter ? String(query.sizeFilter) : ''
  selectedColor.value = query.color ? String(query.color) : ''
  selectedMaterial.value = query.material ? String(query.material) : ''
  selectedBrand.value = query.brand ? String(query.brand) : ''
}

function showAddedMessage(product) {
  toastMessage.value = `${product.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2600)
}
</script>
