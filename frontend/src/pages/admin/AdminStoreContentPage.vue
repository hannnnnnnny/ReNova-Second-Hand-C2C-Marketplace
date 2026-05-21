<template>
  <section class="admin-page store-content-page">
    <AdminPageHeader
      eyebrow="Website content"
      :title="`${store.name} storefront editor`"
      description="Update the live customer-facing website for the selected merchant store."
    >
      <template #actions>
        <RouterLink class="secondary-button" :to="`/store/${store.slug}`">Open storefront</RouterLink>
        <button class="primary-button" type="button" :disabled="saving || !draft.name || !draft.slug" @click="publishContent">
          {{ saving ? 'Saving...' : 'Save and publish' }}
        </button>
      </template>
    </AdminPageHeader>

    <div v-if="successMessage" class="success-message content-save-status" role="status">{{ successMessage }}</div>

    <div class="content-editor-shell">
      <div class="content-control-panel">
        <article class="dashboard-section content-form-card">
          <div class="content-card-heading">
            <span>01</span>
            <div>
              <h2>Homepage</h2>
              <p>The main message, background image, and first shopping buttons.</p>
            </div>
          </div>
          <div class="settings-grid compact-settings-grid">
            <label>Store name<input v-model.trim="draft.name" autocomplete="organization" /></label>
            <label>Public link<input v-model.trim="draft.slug" autocomplete="off" /></label>
            <label class="wide-field">Announcement bar<input v-model.trim="draft.announcement" /></label>
            <label class="wide-field">Hero headline<input v-model.trim="draft.heroTitle" /></label>
            <label class="wide-field">Hero description<textarea v-model.trim="draft.heroText" rows="3"></textarea></label>
            <label>Primary button<input v-model.trim="draft.heroButtonLabel" /></label>
            <label>Secondary button<input v-model.trim="draft.secondaryButtonLabel" /></label>
          </div>
          <div class="content-image-picker">
            <p class="eyebrow">Homepage image</p>
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
        </article>

        <article class="dashboard-section content-form-card">
          <div class="content-card-heading">
            <span>02</span>
            <div>
              <h2>Story and navigation</h2>
              <p>Store story, public promise, footer copy, and menu categories.</p>
            </div>
          </div>
          <div class="settings-grid compact-settings-grid">
            <label class="wide-field">Story title<input v-model.trim="draft.aboutTitle" /></label>
            <label class="wide-field">Story text<textarea v-model.trim="draft.aboutText" rows="4"></textarea></label>
            <label class="wide-field">Customer promise<input v-model.trim="draft.customerPromise" /></label>
            <label class="wide-field">Footer text<input v-model.trim="draft.footerText" /></label>
            <label class="wide-field">Menu categories<textarea v-model="categoriesText" rows="4"></textarea></label>
          </div>
          <div class="category-preview-chips">
            <span v-for="category in parsedCategories" :key="category">{{ category }}</span>
          </div>
        </article>

        <article class="dashboard-section content-form-card">
          <div class="content-card-heading">
            <span>03</span>
            <div>
              <h2>Homepage products</h2>
              <p>Pick the products that should appear first on the storefront homepage.</p>
            </div>
          </div>
          <div class="content-product-picks">
            <label v-for="product in store.products" :key="product.id" :class="{ active: draft.featuredProductIds.includes(product.id) }">
              <input v-model="draft.featuredProductIds" :value="product.id" type="checkbox" />
              <img :src="product.imageUrl" :alt="product.name" loading="lazy" decoding="async" />
              <span>
                <strong>{{ product.name }}</strong>
                <small>{{ product.category }} / {{ formatCurrency(product.price) }}</small>
              </span>
            </label>
          </div>
        </article>

        <article class="dashboard-section content-form-card">
          <div class="content-card-heading">
            <span>04</span>
            <div>
              <h2>SEO preview</h2>
              <p>Search title and description used for this storefront route.</p>
            </div>
          </div>
          <div class="settings-grid">
            <label class="wide-field">SEO title<input v-model.trim="draft.seoTitle" maxlength="70" /></label>
            <label class="wide-field">SEO description<textarea v-model.trim="draft.seoDescription" rows="3" maxlength="160"></textarea></label>
          </div>
        </article>
      </div>

      <aside class="content-preview-panel">
        <article class="website-live-preview" :style="{ '--store-accent': draft.brandColor || store.brandColor }">
          <div class="preview-announcement">{{ draft.announcement }}</div>
          <div class="preview-header-row">
            <strong>{{ draft.logoText || store.logoText }}</strong>
            <span>{{ draft.name }}</span>
          </div>
          <div class="preview-hero-block">
            <img :src="draft.heroImage || template.previewImage" :alt="`${draft.name} homepage image`" />
            <div>
              <p class="eyebrow">{{ template.layoutName }}</p>
              <h2>{{ draft.heroTitle }}</h2>
              <p>{{ draft.heroText }}</p>
              <div class="preview-button-row">
                <span>{{ draft.heroButtonLabel }}</span>
                <span>{{ draft.secondaryButtonLabel }}</span>
              </div>
            </div>
          </div>
          <div class="preview-category-row">
            <span v-for="category in parsedCategories.slice(0, 5)" :key="category">{{ category }}</span>
          </div>
          <div class="preview-story-block">
            <h3>{{ draft.aboutTitle }}</h3>
            <p>{{ draft.aboutText }}</p>
            <strong>{{ draft.customerPromise }}</strong>
          </div>
          <div class="preview-product-strip">
            <article v-for="product in selectedFeaturedProducts.slice(0, 3)" :key="product.id">
              <img :src="product.imageUrl" :alt="product.name" loading="lazy" decoding="async" />
              <strong>{{ product.name }}</strong>
            </article>
          </div>
        </article>
        <StoreTemplatePreview :store="previewStore" />
        <div class="content-publish-actions">
          <button class="secondary-button" type="button" :disabled="saving" @click="saveContent">
            {{ saving ? 'Saving...' : 'Save draft' }}
          </button>
          <button class="primary-button" type="button" :disabled="saving || !draft.name || !draft.slug" @click="publishContent">
            {{ saving ? 'Saving...' : 'Publish changes' }}
          </button>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AdminPageHeader from '../../components/AdminPageHeader.vue'
import StoreTemplatePreview from '../../components/StoreTemplatePreview.vue'
import { getTemplateById } from '../../data/platform'
import { usePlatformStore } from '../../stores/platform'
import { formatCurrency } from '../../utils/format'
import { publicAsset } from '../../utils/publicPath'

const platformStore = usePlatformStore()
const store = computed(() => platformStore.currentStore)
const template = computed(() => getTemplateById(store.value.template))
const successMessage = ref('')
const saving = ref(false)
const categoriesText = ref('')
let successTimer

const draft = reactive({
  name: '',
  slug: '',
  announcement: '',
  heroTitle: '',
  heroText: '',
  heroImage: '',
  heroButtonLabel: '',
  secondaryButtonLabel: '',
  aboutTitle: '',
  aboutText: '',
  customerPromise: '',
  footerText: '',
  seoTitle: '',
  seoDescription: '',
  brandColor: '',
  logoText: '',
  featuredProductIds: []
})

const heroOptions = [
  { label: 'Fashion', src: publicAsset('demo-images/heroes/fashion.jpg') },
  { label: 'Vintage', src: publicAsset('demo-images/heroes/boutique.jpg') },
  { label: 'Sports', src: publicAsset('demo-images/heroes/sports.jpg') },
  { label: 'Home', src: publicAsset('demo-images/heroes/home.jpg') }
]

const parsedCategories = computed(() => parseCategories(categoriesText.value))
const selectedFeaturedProducts = computed(() => {
  const selected = new Set(draft.featuredProductIds.map(Number))
  const products = (store.value.products || []).filter((product) => selected.has(Number(product.id)))
  return products.length ? products : (store.value.products || []).slice(0, 3)
})
const previewStore = computed(() => ({
  ...store.value,
  ...draft,
  name: draft.name || store.value.name,
  slug: draft.slug || store.value.slug,
  categories: parsedCategories.value,
  products: store.value.products || []
}))

onMounted(() => {
  platformStore.loadPlatform()
  syncDraft()
})

watch(store, syncDraft)

function syncDraft() {
  const currentStore = store.value
  Object.assign(draft, {
    name: currentStore.name,
    slug: currentStore.slug,
    announcement: currentStore.announcement || currentStore.shippingMessage,
    heroTitle: currentStore.heroTitle,
    heroText: currentStore.heroText,
    heroImage: currentStore.heroImage || template.value.previewImage,
    heroButtonLabel: currentStore.heroButtonLabel || 'Shop products',
    secondaryButtonLabel: currentStore.secondaryButtonLabel || 'Browse new arrivals',
    aboutTitle: currentStore.aboutTitle || `${currentStore.name} story`,
    aboutText: currentStore.aboutText || currentStore.description,
    customerPromise: currentStore.customerPromise || 'Clear product details, safe demo checkout, and responsive merchant support.',
    footerText: currentStore.footerText || currentStore.description,
    seoTitle: currentStore.seoTitle || `${currentStore.name} | NovaCart Storefront`,
    seoDescription: currentStore.seoDescription || currentStore.description,
    brandColor: currentStore.brandColor,
    logoText: currentStore.logoText,
    featuredProductIds: Array.isArray(currentStore.featuredProductIds) && currentStore.featuredProductIds.length
      ? currentStore.featuredProductIds.slice(0, 6)
      : (currentStore.products || []).slice(0, 6).map((product) => product.id)
  })
  categoriesText.value = (currentStore.categories || []).join(', ')
}

async function saveContent() {
  await saveStoreContent(false)
}

async function publishContent() {
  await saveStoreContent(true)
}

async function saveStoreContent(shouldPublish) {
  if (saving.value) return
  saving.value = true
  try {
    const savedStore = platformStore.updateStore(store.value.slug, {
      ...draft,
      merchantName: draft.name,
      description: draft.aboutText || store.value.description,
      categories: parsedCategories.value,
      featuredProductIds: draft.featuredProductIds,
      setup: { details: true, preview: true }
    })
    if (savedStore) platformStore.setCurrentStore(savedStore.slug)
    if (shouldPublish) {
      platformStore.publishStore(savedStore?.slug || store.value.slug)
    }
    showSuccess(shouldPublish ? 'Website content saved and published.' : 'Website content draft saved.')
  } finally {
    window.setTimeout(() => {
      saving.value = false
    }, 450)
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

function showSuccess(message) {
  successMessage.value = message
  window.clearTimeout(successTimer)
  successTimer = window.setTimeout(() => {
    successMessage.value = ''
  }, 2600)
}
</script>
