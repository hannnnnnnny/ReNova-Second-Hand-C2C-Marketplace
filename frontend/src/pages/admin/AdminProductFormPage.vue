<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Catalog editor"
      :title="isEditing ? 'Edit Product' : 'New Product'"
      description="Keep fashion product details complete, clear, and ready for customers to scan."
    >
      <template #actions>
        <RouterLink class="secondary-button" to="/admin/products">Back to Products</RouterLink>
      </template>
    </PageHeader>
    <LoadingState v-if="loading" message="Loading product form..." />
    <form v-else class="product-editor-layout" @submit.prevent="submitProduct">
      <div class="admin-form">
        <ErrorMessage v-if="error" :message="error" />
        <label>
          Name
          <input v-model.trim="form.name" required maxlength="180" placeholder="Product name" />
        </label>
        <label>
          Slug
          <input v-model.trim="form.slug" maxlength="200" placeholder="optional-product-slug" />
        </label>
        <label>
          SKU
          <input v-model.trim="form.sku" maxlength="80" placeholder="NC-FASHION-SKU" />
        </label>
        <label>
          Brand
          <input v-model.trim="form.brand" maxlength="120" placeholder="Aster Row" />
        </label>
        <label>
          Category
          <select v-model.number="form.categoryId" required>
            <option disabled value="">Select a category</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </label>
        <label>
          Price
          <input v-model.number="form.price" required min="0.01" step="0.01" type="number" />
        </label>
        <label>
          Compare-at Price
          <input v-model.number="form.compareAtPrice" min="0.01" step="0.01" type="number" />
        </label>
        <label>
          Stock Quantity
          <input v-model.number="form.stockQuantity" required min="0" step="1" type="number" />
        </label>
        <label>
          Low-stock Threshold
          <input v-model.number="form.lowStockThreshold" required min="0" step="1" type="number" />
        </label>
        <label>
          Image URL
          <input v-model.trim="form.imageUrl" required placeholder="/catalog/women.svg or https://example.com/product.jpg" />
        </label>
        <label>
          Gallery URLs
          <textarea v-model.trim="form.imageGalleryText" rows="3" placeholder="Add one image URL per line."></textarea>
        </label>
        <label>
          Tags
          <input v-model.trim="form.tagsText" placeholder="linen, capsule, workwear" />
        </label>
        <label class="wide-field">
          Description
          <textarea v-model.trim="form.description" required maxlength="2000" rows="6" placeholder="Describe the product clearly."></textarea>
        </label>
      </div>
      <aside class="summary-panel product-publish-panel">
        <h2>Publishing</h2>
        <p class="muted">Only active products with an Active status appear in the public storefront.</p>
        <label>
          Product Status
          <select v-model="form.status">
            <option value="ACTIVE">Active</option>
            <option value="DRAFT">Draft</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </label>
        <label class="checkbox-field">
          <input v-model="form.active" type="checkbox" />
          Active in storefront
        </label>
        <label class="checkbox-field">
          <input v-model="form.featured" type="checkbox" />
          Feature on storefront
        </label>
        <div class="summary-line">
          <span>Slug preview</span>
          <strong>{{ slugPreview }}</strong>
        </div>
        <div class="summary-line">
          <span>Stock status</span>
          <StatusBadge :value="stockStatus" :label="stockLabel" />
        </div>
        <button class="primary-button" type="submit" :disabled="submitting || !formIsValid">
          {{ submitting ? 'Saving Product...' : 'Save Product' }}
        </button>
      </aside>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createAdminProduct,
  fetchAdminCategories,
  fetchAdminProduct,
  updateAdminProduct
} from '../../api/admin'
import { getApiError } from '../../api/client'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const categories = ref([])
const isEditing = computed(() => Boolean(route.params.id))
const form = reactive({
  name: '',
  slug: '',
  sku: '',
  brand: '',
  description: '',
  price: 0,
  compareAtPrice: '',
  stockQuantity: 0,
  lowStockThreshold: 5,
  imageUrl: '',
  imageGalleryText: '',
  tagsText: '',
  featured: false,
  status: 'ACTIVE',
  active: true,
  categoryId: ''
})
const formIsValid = computed(() => {
  return form.name
    && form.description
    && Number(form.price) > 0
    && (!form.compareAtPrice || Number(form.compareAtPrice) > Number(form.price))
    && Number(form.stockQuantity) >= 0
    && Number(form.lowStockThreshold) >= 0
    && form.imageUrl
    && form.categoryId
})
const slugPreview = computed(() => {
  const source = form.slug || form.name || 'new-product'
  return source
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
})
const stockStatus = computed(() => {
  if (Number(form.stockQuantity) < 1) return 'out of stock'
  if (Number(form.stockQuantity) <= 5) return 'low stock'
  return 'in stock'
})
const stockLabel = computed(() => {
  if (Number(form.stockQuantity) < 1) return 'Out of stock'
  if (Number(form.stockQuantity) <= 5) return 'Low stock'
  return 'In stock'
})

onMounted(async () => {
  try {
    categories.value = await fetchAdminCategories()
    if (isEditing.value) {
      const product = await fetchAdminProduct(route.params.id)
      form.name = product.name
      form.slug = product.slug
      form.sku = product.sku || ''
      form.brand = product.brand || ''
      form.description = product.description
      form.price = Number(product.price)
      form.compareAtPrice = product.compareAtPrice ? Number(product.compareAtPrice) : ''
      form.stockQuantity = product.stockQuantity
      form.lowStockThreshold = product.lowStockThreshold ?? 5
      form.imageUrl = product.imageUrl
      form.imageGalleryText = (product.imageGallery || []).join('\n')
      form.tagsText = (product.tags || []).join(', ')
      form.featured = Boolean(product.featured)
      form.status = product.status || 'ACTIVE'
      form.active = product.active
      form.categoryId = product.category.id
    }
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product form could not be loaded.')
  } finally {
    loading.value = false
  }
})

async function submitProduct() {
  error.value = ''
  submitting.value = true

  const payload = {
    ...form,
    price: Number(form.price),
    compareAtPrice: form.compareAtPrice ? Number(form.compareAtPrice) : null,
    stockQuantity: Number(form.stockQuantity),
    lowStockThreshold: Number(form.lowStockThreshold),
    slug: form.slug || null,
    sku: form.sku || null,
    brand: form.brand || null,
    imageGallery: splitLines(form.imageGalleryText),
    tags: splitTags(form.tagsText)
  }
  delete payload.imageGalleryText
  delete payload.tagsText

  try {
    if (isEditing.value) {
      await updateAdminProduct(route.params.id, payload)
    } else {
      await createAdminProduct(payload)
    }
    router.push('/admin/products')
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product could not be saved.')
  } finally {
    submitting.value = false
  }
}

function splitLines(value) {
  return String(value || '')
    .split('\n')
    .map((entry) => entry.trim())
    .filter(Boolean)
}

function splitTags(value) {
  return String(value || '')
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean)
}
</script>
