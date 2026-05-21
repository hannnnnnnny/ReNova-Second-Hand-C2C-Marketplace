<template>
  <section class="admin-page simple-store-builder">
    <AdminPageHeader
      eyebrow="Simple store builder"
      :title="`${store.name} setup`"
      description="Change the public storefront image, words, categories, and products from one calm screen."
    >
      <template #actions>
        <RouterLink class="secondary-button" :to="`/store/${store.slug}`">Preview store</RouterLink>
        <button class="primary-button" type="button" :disabled="publishing || saving || !draft.name || !draft.slug" @click="publishStore">
          {{ publishing ? 'Publishing...' : 'Publish store' }}
        </button>
      </template>
    </AdminPageHeader>
    <div v-if="successMessage" class="success-message simple-save-status" role="status">
      {{ successMessage }}
      <span v-if="lastSavedAt">Last saved {{ lastSavedAt }}</span>
    </div>

    <div class="store-setup-layout simple-builder-layout">
      <aside class="simple-builder-side">
        <SetupChecklist :store="store" />
        <StoreTemplatePreview :store="store" />
        <article class="dashboard-section simple-template-note">
          <p class="eyebrow">Current template</p>
          <h2>{{ template.name }}</h2>
          <p>{{ template.primaryGoal }}</p>
          <ul>
            <li v-for="item in template.easyEdits" :key="item">{{ item }}</li>
          </ul>
        </article>
      </aside>

      <article class="dashboard-section simple-builder-card">
        <div class="simple-step-heading">
          <span>1</span>
          <div>
            <h2>Store words and picture</h2>
            <p>These fields control the first screen shoppers see.</p>
          </div>
        </div>
        <div class="settings-grid compact-settings-grid">
          <label>Store name<input v-model.trim="draft.name" autocomplete="organization" /></label>
          <label>Public link<input v-model.trim="draft.slug" autocomplete="off" /></label>
          <label>Store category<input v-model.trim="draft.category" /></label>
          <label>Logo text<input v-model.trim="draft.logoText" maxlength="3" /></label>
          <label>Brand color<input v-model="draft.brandColor" type="color" /></label>
          <label>Shipping message<input v-model.trim="draft.shippingMessage" /></label>
          <label class="wide-field">Homepage headline<input v-model.trim="draft.heroTitle" /></label>
          <label class="wide-field">Homepage description<textarea v-model.trim="draft.heroText" rows="3"></textarea></label>
          <label class="wide-field">Store description<textarea v-model.trim="draft.description" rows="3"></textarea></label>
          <label class="wide-field">Top announcement<input v-model.trim="draft.announcement" /></label>
        </div>
        <div class="simple-image-picker">
          <p class="eyebrow">Homepage background image</p>
          <div class="image-choice-grid">
            <button
              v-for="option in heroOptions"
              :key="option.src"
              type="button"
              :class="{ active: draft.heroImage === option.src }"
              @click="draft.heroImage = option.src"
            >
              <img :src="option.src" :alt="option.label" loading="lazy" decoding="async" />
              <span>{{ option.label }}</span>
            </button>
          </div>
        </div>
        <div class="form-actions">
          <button class="primary-button" type="button" :disabled="saving || !draft.name || !draft.slug" @click="saveDetails">
            {{ saving ? 'Saving...' : 'Save store words' }}
          </button>
        </div>
      </article>

      <article class="dashboard-section simple-builder-card">
        <div class="simple-step-heading">
          <span>2</span>
          <div>
            <h2>Categories</h2>
            <p>Type a few clear shop sections. Separate them with commas or new lines.</p>
          </div>
        </div>
        <label class="wide-field category-textarea">
          Product categories
          <textarea v-model="categoriesText" rows="4" placeholder="New Arrivals, Bags, Shoes, Sale"></textarea>
        </label>
        <div class="category-preview-chips" aria-label="Category preview">
          <span v-for="category in parsedCategories" :key="category">{{ category }}</span>
        </div>
      </article>

      <article class="dashboard-section simple-builder-card">
        <div class="simple-step-heading">
          <span>3</span>
          <div>
            <h2>Products</h2>
            <p>Add, edit, or remove products without leaving this page.</p>
          </div>
        </div>
        <div class="product-editor-layout">
          <div class="product-editor-list" aria-label="Current products">
            <button
              v-for="product in storeProducts"
              :key="product.id"
              type="button"
              :class="{ active: editingProductId === product.id }"
              @click="editProduct(product)"
            >
              <img :src="product.imageUrl" :alt="product.name" loading="lazy" decoding="async" />
              <span>
                <strong>{{ product.name }}</strong>
                <small>{{ product.category }} / {{ formatMoney(product.price) }} / {{ product.stockQuantity }} in stock</small>
              </span>
            </button>
          </div>
          <form class="product-editor-panel" @submit.prevent="saveProductDraft">
            <label>Product name<input v-model.trim="productDraft.name" placeholder="Soft cotton shirt" /></label>
            <label>Category<input v-model.trim="productDraft.category" placeholder="New Arrivals" /></label>
            <label>Price<input v-model.number="productDraft.price" min="0" step="0.01" type="number" /></label>
            <label>Compare price<input v-model.number="productDraft.compareAtPrice" min="0" step="0.01" type="number" /></label>
            <label>Stock<input v-model.number="productDraft.stockQuantity" min="0" step="1" type="number" /></label>
            <label>Badges<input v-model.trim="productDraft.badges" placeholder="New, Best Seller" /></label>
            <label class="wide-field">Description<textarea v-model.trim="productDraft.description" rows="3"></textarea></label>
            <div class="simple-image-picker product-image-picker">
              <p class="eyebrow">Product image</p>
              <div class="image-choice-grid compact-image-grid">
                <button
                  v-for="option in productImageOptions"
                  :key="option.src"
                  type="button"
                  :class="{ active: productDraft.imageUrl === option.src }"
                  @click="productDraft.imageUrl = option.src"
                >
                  <img :src="option.src" :alt="option.label" loading="lazy" decoding="async" />
                  <span>{{ option.label }}</span>
                </button>
              </div>
            </div>
            <div class="form-actions product-form-actions">
              <button class="primary-button" type="submit" :disabled="!productDraftIsValid">
                {{ editingProductId ? 'Update product' : 'Add product' }}
              </button>
              <button class="secondary-button" type="button" @click="resetProductDraft">Clear form</button>
              <button v-if="editingProductId" class="text-button danger" type="button" @click="deleteProduct">Delete product</button>
            </div>
          </form>
        </div>
      </article>

      <article class="dashboard-section simple-save-panel">
        <div>
          <p class="eyebrow">Ready check</p>
          <h2>Save once, then preview</h2>
          <p>Saving also stores categories and product edits. Publish marks the storefront as live in this demo workspace.</p>
        </div>
        <div class="form-actions">
          <button class="secondary-button" type="button" :disabled="saving || !draft.name || !draft.slug" @click="saveDetails">
            {{ saving ? 'Saving...' : 'Save all changes' }}
          </button>
          <button class="primary-button" type="button" :disabled="publishing || saving || !draft.name || !draft.slug" @click="publishStore">
            {{ publishing ? 'Publishing...' : 'Publish store' }}
          </button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AdminPageHeader from '../../components/AdminPageHeader.vue'
import SetupChecklist from '../../components/SetupChecklist.vue'
import StoreTemplatePreview from '../../components/StoreTemplatePreview.vue'
import { getTemplateById } from '../../data/platform'
import { usePlatformStore } from '../../stores/platform'
import { publicAsset } from '../../utils/publicPath'

const platformStore = usePlatformStore()
const store = computed(() => platformStore.currentStore)
const successMessage = ref('')
const lastSavedAt = ref('')
const saving = ref(false)
const publishing = ref(false)
const categoriesText = ref('')
const editingProductId = ref(null)
let successTimer
const draft = reactive({
  name: '',
  slug: '',
  category: '',
  description: '',
  shippingMessage: '',
  announcement: '',
  heroTitle: '',
  heroText: '',
  heroImage: '',
  brandColor: '',
  logoText: ''
})
const productDraft = reactive({
  name: '',
  category: '',
  price: 48,
  compareAtPrice: '',
  stockQuantity: 12,
  badges: 'New',
  imageUrl: publicAsset('demo-images/products/boutique-shirt.jpg'),
  description: ''
})

const heroOptions = [
  { label: 'Fashion', src: publicAsset('demo-images/heroes/fashion.jpg') },
  { label: 'Vintage', src: publicAsset('demo-images/heroes/boutique.jpg') },
  { label: 'Sports', src: publicAsset('demo-images/heroes/sports.jpg') },
  { label: 'Home', src: publicAsset('demo-images/heroes/home.jpg') }
]

const productImageOptions = [
  { label: 'Shirt', src: publicAsset('demo-images/products/boutique-shirt.jpg') },
  { label: 'Bag', src: publicAsset('demo-images/products/fashion-bag.jpg') },
  { label: 'Sneaker', src: publicAsset('demo-images/products/sports-sneaker.jpg') },
  { label: 'Vase', src: publicAsset('demo-images/products/boutique-vase.jpg') },
  { label: 'Throw', src: publicAsset('demo-images/products/home-throw.jpg') },
  { label: 'Jacket', src: publicAsset('demo-images/products/sports-jacket.jpg') }
]

const template = computed(() => getTemplateById(store.value.template))
const storeProducts = computed(() => store.value.products || [])
const parsedCategories = computed(() => parseCategories(categoriesText.value))
const productDraftIsValid = computed(() => Boolean(productDraft.name && productDraft.category && Number(productDraft.price) >= 0))

onMounted(() => {
  platformStore.loadPlatform()
  syncDraft()
})

watch(store, syncDraft)

function syncDraft() {
  Object.assign(draft, {
    name: store.value.name,
    slug: store.value.slug,
    category: store.value.category,
    description: store.value.description,
    shippingMessage: store.value.shippingMessage,
    announcement: store.value.announcement,
    heroTitle: store.value.heroTitle,
    heroText: store.value.heroText,
    heroImage: store.value.heroImage || template.value.previewImage,
    brandColor: store.value.brandColor,
    logoText: store.value.logoText
  })
  categoriesText.value = (store.value.categories || []).join(', ')
  if (!editingProductId.value) resetProductDraft()
}

async function saveDetails() {
  await runOnce(saving, () => {
    saveStorePatch('Store setup saved.')
  })
}

async function publishStore() {
  await runOnce(publishing, () => {
    const savedStore = saveStorePatch()
    platformStore.publishStore(savedStore?.slug || store.value.slug)
    showSuccess('Store published. The storefront is ready to preview.')
  })
}

function saveStorePatch(message) {
  const savedStore = platformStore.updateStore(store.value.slug, {
    ...draft,
    merchantName: draft.name,
    categories: parsedCategories.value,
    setup: {
      details: Boolean(draft.name && draft.slug),
      shipping: Boolean(draft.shippingMessage),
      products: storeProducts.value.length > 0,
      preview: true
    }
  })
  if (savedStore) platformStore.setCurrentStore(savedStore.slug)
  if (message) showSuccess(message)
  return savedStore
}

function editProduct(product) {
  editingProductId.value = product.id
  Object.assign(productDraft, {
    name: product.name,
    category: product.category,
    price: product.price,
    compareAtPrice: product.compareAtPrice || '',
    stockQuantity: product.stockQuantity,
    badges: (product.badges || []).join(', '),
    imageUrl: product.imageUrl || productImageOptions[0].src,
    description: product.description || ''
  })
}

function saveProductDraft() {
  if (!productDraftIsValid.value) return
  const wasEditing = Boolean(editingProductId.value)
  const nextProduct = buildProductFromDraft()
  const products = editingProductId.value
    ? storeProducts.value.map((product) => product.id === editingProductId.value ? nextProduct : product)
    : [nextProduct, ...storeProducts.value]
  const savedStore = platformStore.updateStore(store.value.slug, {
    products,
    categories: [...parsedCategories.value, nextProduct.category],
    setup: { products: true }
  })
  if (savedStore) platformStore.setCurrentStore(savedStore.slug)
  resetProductDraft()
  showSuccess(wasEditing ? 'Product updated.' : 'Product added.')
}

function deleteProduct() {
  if (!editingProductId.value) return
  const products = storeProducts.value.filter((product) => product.id !== editingProductId.value)
  const savedStore = platformStore.updateStore(store.value.slug, {
    products,
    setup: { products: products.length > 0 }
  })
  if (savedStore) platformStore.setCurrentStore(savedStore.slug)
  resetProductDraft()
  showSuccess('Product deleted.')
}

function buildProductFromDraft() {
  const existing = storeProducts.value.find((product) => product.id === editingProductId.value)
  return {
    ...existing,
    id: existing?.id || Date.now(),
    name: productDraft.name,
    category: productDraft.category,
    price: Number(productDraft.price) || 0,
    compareAtPrice: Number(productDraft.compareAtPrice) || null,
    stockQuantity: Math.max(0, Math.floor(Number(productDraft.stockQuantity) || 0)),
    imageUrl: productDraft.imageUrl || productImageOptions[0].src,
    imageGallery: [productDraft.imageUrl || productImageOptions[0].src],
    badges: parseBadges(productDraft.badges),
    description: productDraft.description
  }
}

function resetProductDraft() {
  editingProductId.value = null
  Object.assign(productDraft, {
    name: '',
    category: parsedCategories.value[0] || 'New Arrivals',
    price: 48,
    compareAtPrice: '',
    stockQuantity: 12,
    badges: 'New',
    imageUrl: productImageOptions[0].src,
    description: ''
  })
}

async function runOnce(flag, action) {
  if (flag.value) return
  flag.value = true
  try {
    await Promise.resolve(action())
  } finally {
    window.setTimeout(() => {
      flag.value = false
    }, 500)
  }
}

function parseCategories(value) {
  const seen = new Set()
  return String(value || '')
    .split(/[,\n]/)
    .map((item) => item.trim())
    .filter((item) => {
      const key = item.toLowerCase()
      if (!item || seen.has(key)) return false
      seen.add(key)
      return true
    })
    .slice(0, 8)
}

function parseBadges(value) {
  return parseCategories(value).slice(0, 3)
}

function formatMoney(value) {
  return `$${Number(value || 0).toFixed(0)}`
}

function showSuccess(message) {
  successMessage.value = message
  lastSavedAt.value = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  window.clearTimeout(successTimer)
  successTimer = window.setTimeout(() => {
    successMessage.value = ''
  }, 2600)
}
</script>
