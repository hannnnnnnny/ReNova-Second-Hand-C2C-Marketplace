<template>
  <div>
    <section class="hero-section">
      <div class="hero-copy">
        <p class="eyebrow">NovaCart Fashion Commerce</p>
        <h1>Seasonal style, ready to sell.</h1>
        <p>Browse clothing, bags, jewelry, shoes, active pieces, and lifestyle accessories from a modern storefront built for fashion merchants.</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" :to="{ name: 'products', query: { search: 'spring' } }">Shop Spring Edit</RouterLink>
          <RouterLink class="secondary-button hero-secondary" :to="{ name: 'products', query: { search: 'sale' } }">Shop Sale</RouterLink>
        </div>
      </div>
      <div class="hero-stats" aria-label="Storefront highlights">
        <article>
          <strong>{{ products.length || '60' }}</strong>
          <span>Fashion products</span>
        </article>
        <article>
          <strong>{{ categories.length || '10' }}</strong>
          <span>Retail categories</span>
        </article>
        <article>
          <strong>{{ collections.length || '6' }}</strong>
          <span>Seasonal collections</span>
        </article>
      </div>
    </section>

    <section class="page-section category-section">
      <SectionHeader
        eyebrow="Category navigation"
        title="Shop the fashion floor"
        description="Move quickly between wardrobe, accessory, footwear, sportswear, sale, and campaign edits."
      />
      <LoadingState v-if="loading" message="Loading categories..." />
      <ErrorMessage v-else-if="error" :message="error" />
      <div v-else class="category-highlight-grid">
        <RouterLink
          v-for="category in categories"
          :key="category.id"
          class="category-highlight"
          :to="{ name: 'products', query: { category: category.id } }"
        >
          <span>{{ category.name }}</span>
          <small>{{ category.description }}</small>
        </RouterLink>
      </div>
    </section>

    <section class="page-section featured-section">
      <SectionHeader
        eyebrow="Featured collections"
        title="New arrivals, best sellers, and seasonal edits"
        description="A storefront preview of the products merchants can feature, mark down, and move through checkout."
      />
      <LoadingState v-if="loading" message="Loading featured products..." />
      <ErrorMessage v-else-if="error" :message="error" />
      <div v-else class="campaign-grid">
        <RouterLink
          v-for="collection in featuredCollections"
          :key="collection.id"
          class="campaign-card"
          :to="{ name: 'products', query: { collectionId: collection.id } }"
        >
          <strong>{{ collection.name }}</strong>
          <span>{{ collection.description }}</span>
        </RouterLink>
      </div>
      <div v-if="!loading && !error" class="product-grid featured-product-row">
        <ProductCard v-for="product in featuredProducts" :key="product.id" :product="product" />
      </div>
    </section>

    <section class="page-section campaign-section">
      <SectionHeader
        eyebrow="Seasonal campaign"
        title="Spring Edit"
        description="Soft tailoring, fresh color, and flexible pieces for a premium fashion storefront."
      />
      <div class="campaign-grid">
        <RouterLink class="campaign-card" :to="{ name: 'products', query: { search: 'spring' } }">
          <strong>Spring layers</strong>
          <span>Trench coats, silk blouses, and lighter tailoring.</span>
        </RouterLink>
        <RouterLink class="campaign-card" :to="{ name: 'products', query: { tag: 'active-weekend' } }">
          <strong>Active weekend</strong>
          <span>Sportswear and original equipment for studio, court, and travel.</span>
        </RouterLink>
        <RouterLink class="campaign-card" :to="{ name: 'products', query: { search: 'evening' } }">
          <strong>Evening details</strong>
          <span>Jewelry, satin, clutches, and finishers for occasion styling.</span>
        </RouterLink>
      </div>
    </section>

    <section class="page-section value-section">
      <div class="value-grid">
        <article v-for="item in valueCards" :key="item.title" class="value-card">
          <component :is="item.icon" aria-hidden="true" />
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ShieldCheck, Sparkles, Truck } from 'lucide-vue-next'
import { fetchCategories, fetchFeaturedCollections, fetchProducts } from '../api/catalog'
import { getApiError } from '../api/client'
import ErrorMessage from '../components/ErrorMessage.vue'
import LoadingState from '../components/LoadingState.vue'
import ProductCard from '../components/ProductCard.vue'
import SectionHeader from '../components/SectionHeader.vue'

const loading = ref(true)
const error = ref('')
const products = ref([])
const categories = ref([])
const collections = ref([])
const featuredProducts = computed(() => products.value.slice(0, 3))
const featuredCollections = computed(() => collections.value.slice(0, 3))
const valueCards = [
  {
    title: 'Fashion Checkout',
    description: 'Demo payment, order totals, stock checks, and customer details stay clear from cart to confirmation.',
    icon: Truck
  },
  {
    title: 'Curated Catalog',
    description: 'Fashion categories, sale edits, new arrivals, and fictional private labels keep browsing focused.',
    icon: Sparkles
  },
  {
    title: 'Merchant Workspace',
    description: 'Admin operators can manage products, categories, orders, and inventory from protected screens.',
    icon: ShieldCheck
  }
]

onMounted(async () => {
  try {
    const [categoryData, collectionData, productData] = await Promise.all([fetchCategories(), fetchFeaturedCollections(), fetchProducts()])
    categories.value = categoryData
    collections.value = collectionData
    products.value = productData
  } catch (requestError) {
    error.value = getApiError(requestError, 'Storefront content could not be loaded.')
  } finally {
    loading.value = false
  }
})
</script>
