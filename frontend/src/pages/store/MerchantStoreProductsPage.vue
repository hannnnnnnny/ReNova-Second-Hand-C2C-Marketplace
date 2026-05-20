<template>
  <section class="merchant-products-page">
    <div class="merchant-products-header">
      <div>
        <p class="eyebrow">{{ store.name }}</p>
        <h1>{{ selectedCategory || 'All products' }}</h1>
        <p>{{ filteredProducts.length }} {{ filteredProducts.length === 1 ? 'product' : 'products' }} from this merchant storefront.</p>
      </div>
      <div class="catalog-shop-actions">
        <SearchInput v-model="searchTerm" label="Search store" placeholder="Search products" />
        <button class="secondary-button filter-drawer-button" type="button" :aria-expanded="filtersOpen" @click="filtersOpen = !filtersOpen">
          {{ filtersOpen ? 'Hide filters' : 'Filters' }}
        </button>
        <button class="secondary-button filter-drawer-button" type="button" :aria-pressed="savedOnly" @click="savedOnly = !savedOnly">
          {{ savedOnly ? 'Showing saved' : 'Saved' }}
        </button>
      </div>
    </div>
    <nav class="merchant-category-pills" aria-label="Product categories">
      <button type="button" :class="{ active: !selectedCategory }" @click="selectedCategory = ''">All</button>
      <button
        v-for="category in store.categories"
        :key="category"
        type="button"
        :class="{ active: selectedCategory === category }"
        @click="selectedCategory = category"
      >
        {{ category }}
      </button>
    </nav>

    <div class="merchant-products-layout" :class="{ 'filters-open': filtersOpen }">
      <aside v-if="filtersOpen" class="catalog-filter-panel catalog-filter-drawer merchant-filter-panel">
        <div class="filter-panel-heading">
          <strong>Filters</strong>
          <span>{{ filteredProducts.length }} results</span>
        </div>
        <label>Category
          <select v-model="selectedCategory">
            <option value="">All categories</option>
            <option v-for="category in store.categories" :key="category" :value="category">{{ category }}</option>
          </select>
        </label>
        <label>Sort
          <select v-model="sortMode">
            <option value="featured">Featured</option>
            <option value="price-low">Price: low to high</option>
            <option value="price-high">Price: high to low</option>
            <option value="sale">Sale first</option>
          </select>
        </label>
        <label class="toggle-control"><input v-model="saleOnly" type="checkbox" /> Sale only</label>
        <label class="toggle-control"><input v-model="savedOnly" type="checkbox" /> Saved products</label>
        <button class="secondary-button" type="button" @click="clearFilters">Clear filters</button>
      </aside>
      <div class="catalog-results-panel">
        <ProductGrid v-if="filteredProducts.length" :products="filteredProducts" :store="store" @add="addToCart" />
        <EmptyState v-else title="No products match" message="Try another category or search term." />
      </div>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import EmptyState from '../../components/EmptyState.vue'
import ProductGrid from '../../components/ProductGrid.vue'
import SearchInput from '../../components/SearchInput.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useStorefrontCartStore } from '../../stores/storefrontCart'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const cartStore = useStorefrontCartStore()
const searchTerm = ref('')
const selectedCategory = ref('')
const sortMode = ref('featured')
const saleOnly = ref(false)
const savedOnly = ref(false)
const filtersOpen = ref(false)
const toastMessage = ref('')
let toastTimer

watch(
  () => route.query.category,
  (category) => {
    selectedCategory.value = category ? String(category) : ''
  },
  { immediate: true }
)

watch(
  () => route.query.saved,
  (saved) => {
    savedOnly.value = saved === 'true'
  },
  { immediate: true }
)

const filteredProducts = computed(() => {
  const query = searchTerm.value.toLowerCase()
  const products = props.store.products.filter((product) => {
    const matchesCategory = !selectedCategory.value || product.category === selectedCategory.value
    const matchesSearch = query
      ? `${product.name} ${product.category} ${product.description} ${(product.badges || []).join(' ')} ${product.merchandisingLabel || ''}`.toLowerCase().includes(query)
      : true
    const matchesSale = !saleOnly.value || product.discountPercent
    const matchesSaved = !savedOnly.value || cartStore.isFavoriteForStore(props.store.slug, product.id)
    return matchesCategory && matchesSearch && matchesSale && matchesSaved
  })
  return products.sort((a, b) => {
    if (sortMode.value === 'price-low') return a.price - b.price
    if (sortMode.value === 'price-high') return b.price - a.price
    if (sortMode.value === 'sale') return (b.discountPercent || 0) - (a.discountPercent || 0)
    return 0
  })
})

function clearFilters() {
  searchTerm.value = ''
  selectedCategory.value = ''
  sortMode.value = 'featured'
  saleOnly.value = false
  savedOnly.value = false
}

function addToCart(product) {
  cartStore.addItem(props.store.slug, product, 1)
  toastMessage.value = `${product.name} added to cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
