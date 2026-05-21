<template>
  <div class="merchant-store-page" :data-template="template.id">
    <section class="merchant-store-hero" :class="`merchant-store-hero--${template.id}`">
      <div class="merchant-hero-copy">
        <p class="eyebrow">{{ store.category }} edit / {{ template.layoutName }}</p>
        <h1>{{ store.heroTitle }}</h1>
        <p>{{ store.heroText }}</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">{{ heroButtonLabel }}</RouterLink>
          <RouterLink class="secondary-button" :to="{ path: `/store/${store.slug}/products`, query: { category: primaryCategory } }">{{ secondaryButtonLabel }}</RouterLink>
        </div>
        <div class="merchant-store-proof">
          <span>{{ store.products.length }} products</span>
          <span>{{ store.shippingMessage }}</span>
          <span>Demo checkout only</span>
        </div>
      </div>
      <figure class="merchant-hero-media">
        <img :src="store.heroImage || template.previewImage" :alt="`${store.name} storefront visual`" />
        <figcaption>
          <span>{{ template.name }}</span>
          <strong>{{ primaryCategory }}</strong>
        </figcaption>
      </figure>
    </section>

    <section class="template-focus-panel" :data-template="template.id" aria-label="Template merchandising focus">
      <div>
        <p class="eyebrow">{{ template.name }}</p>
        <h2>{{ template.primaryGoal }}</h2>
        <p>{{ template.focus }}</p>
      </div>
      <div class="template-focus-grid">
        <article v-for="(item, index) in templateProofItems" :key="item.label">
          <span>{{ String(index + 1).padStart(2, '0') }}</span>
          <strong>{{ item.label }}</strong>
          <p>{{ item.copy }}</p>
        </article>
      </div>
    </section>

    <section class="storefront-story-section" aria-label="Store story">
      <div>
        <p class="eyebrow">{{ store.name }}</p>
        <h2>{{ aboutTitle }}</h2>
      </div>
      <div>
        <p>{{ aboutText }}</p>
        <strong>{{ customerPromise }}</strong>
      </div>
    </section>

    <section class="storefront-service-strip" aria-label="Store service promises">
      <article>
        <span>01</span>
        <strong>Clear delivery</strong>
        <p>Shipping estimates and order tracking are shown before and after checkout.</p>
      </article>
      <article>
        <span>02</span>
        <strong>Easy returns</strong>
        <p>Every demo order includes a 30-day support and refund request window.</p>
      </article>
      <article>
        <span>03</span>
        <strong>Safe demo payment</strong>
        <p>Payment UI is realistic, but this portfolio checkout never processes real cards.</p>
      </article>
    </section>

    <section class="storefront-category-strip">
      <div>
        <p class="eyebrow">Shop by category</p>
        <h2>{{ categoryHeading }}</h2>
      </div>
      <CategoryTiles :categories="visibleCategories" :products-path="`/store/${store.slug}/products`" />
    </section>

    <section class="storefront-product-section">
      <div class="storefront-section-header">
        <div>
          <p class="eyebrow">Featured products</p>
          <h2>{{ productHeading }}</h2>
        </div>
        <RouterLink class="text-link" :to="`/store/${store.slug}/products`">View all products</RouterLink>
      </div>
      <ProductGrid :products="featuredProducts" :store="store" @add="addToCart" />
    </section>
    <ToastMessage :message="toastMessage" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import CategoryTiles from '../../components/CategoryTiles.vue'
import ProductGrid from '../../components/ProductGrid.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { getTemplateById } from '../../data/platform'
import { useStorefrontCartStore } from '../../stores/storefrontCart'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const cartStore = useStorefrontCartStore()
const toastMessage = ref('')
let toastTimer
const template = computed(() => getTemplateById(props.store.template))
const featuredProducts = computed(() => {
  const selectedIds = new Set((props.store.featuredProductIds || []).map(Number))
  const selectedProducts = selectedIds.size
    ? props.store.products.filter((product) => selectedIds.has(Number(product.id)))
    : []
  return (selectedProducts.length ? selectedProducts : props.store.products).slice(0, 6)
})
const templateProofItems = computed(() => {
  const copies = templateCopyById[template.value.id] || templateCopyById.minimal
  return (template.value.homepageModules || []).map((label, index) => ({
    label,
    copy: copies[index] || 'This section is editable from the merchant setup screen.'
  }))
})
const visibleCategories = computed(() => {
  const seen = new Set()
  return (props.store.categories || []).filter((category) => {
    const key = String(category || '').trim().toLowerCase()
    if (!key || seen.has(key)) return false
    seen.add(key)
    return true
  })
})
const primaryCategory = computed(() => visibleCategories.value[0] || 'New Arrivals')
const heroButtonLabel = computed(() => props.store.heroButtonLabel || 'Shop products')
const secondaryButtonLabel = computed(() => props.store.secondaryButtonLabel || 'Browse new arrivals')
const aboutTitle = computed(() => props.store.aboutTitle || `${props.store.name} story`)
const aboutText = computed(() => props.store.aboutText || props.store.description)
const customerPromise = computed(() => props.store.customerPromise || 'Clear product details, safe demo checkout, and responsive merchant support.')
const categoryHeading = computed(() => categoryHeadingByTemplate[template.value.id] || 'Find the right part of the edit')
const productHeading = computed(() => productHeadingByTemplate[template.value.id] || 'Pieces worth opening first')

const templateCopyById = {
  fashion: [
    'Use the main image and headline to sell the collection mood before products appear.',
    'Lead shoppers into seasonal edits without forcing them through a dense menu.',
    'Keep hero products visible for quick add-to-cart behavior.'
  ],
  thrift: [
    'Announce the newest drop so repeat shoppers know what changed.',
    'Help shoppers scan condition, category, and rarity before opening a product.',
    'Keep limited pieces easy to find before they sell out.'
  ],
  sports: [
    'Prioritize action-led messaging for shoppers who already know what they need.',
    'Surface training categories as shortcuts instead of decorative navigation.',
    'Show stock and delivery confidence near the first buying decision.'
  ],
  home: [
    'Set the room feeling first so products make sense in context.',
    'Make gifting and room-based browsing obvious without extra instructions.',
    'Feature calm daily-use products instead of a loud sale grid.'
  ],
  minimal: [
    'Keep the introduction short so new merchants can publish quickly.',
    'Put delivery, support, and demo-payment trust signals near the top.',
    'Let the product grid carry the store without extra decoration.'
  ]
}

const categoryHeadingByTemplate = {
  fashion: 'Browse the edit by wardrobe moment',
  thrift: 'Scan the newest finds by type',
  sports: 'Jump straight to the gear category',
  home: 'Shop by room, ritual, or gift need',
  minimal: 'Move quickly through the catalog'
}

const productHeadingByTemplate = {
  fashion: 'The pieces anchoring this edit',
  thrift: 'One-off finds to open first',
  sports: 'Ready-to-move products in stock',
  home: 'Useful pieces for calm routines',
  minimal: 'Clear picks from the starter catalog'
}

function addToCart(product) {
  cartStore.addItem(props.store.slug, product, 1)
  toastMessage.value = `${product.name} added to ${props.store.name} cart.`
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
