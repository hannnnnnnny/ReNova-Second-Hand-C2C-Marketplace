<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Catalog"
      title="Collections"
      description="Create and maintain seasonal fashion edits, campaign collections, and sale stories."
    />
    <LoadingState v-if="loading" message="Loading collections..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else class="admin-split-layout">
      <form class="admin-form" @submit.prevent="saveCollection">
        <h2>{{ editingId ? 'Edit Collection' : 'New Collection' }}</h2>
        <label>Name<input v-model.trim="form.name" required maxlength="140" /></label>
        <label>Slug<input v-model.trim="form.slug" maxlength="160" placeholder="optional-slug" /></label>
        <label class="wide-field">Description<textarea v-model.trim="form.description" rows="4" maxlength="800"></textarea></label>
        <label>Hero Image URL<input v-model.trim="form.heroImageUrl" placeholder="/catalog/seasonal.svg" /></label>
        <label>Display Image URL<input v-model.trim="form.displayImageUrl" placeholder="/catalog/women.svg" /></label>
        <label>Status
          <select v-model="form.status">
            <option value="ACTIVE">Active</option>
            <option value="DRAFT">Draft</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </label>
        <label>Sort Order<input v-model.number="form.sortOrder" min="0" type="number" /></label>
        <label class="checkbox-field"><input v-model="form.featured" type="checkbox" /> Featured</label>
        <div class="form-actions">
          <button class="primary-button" type="submit" :disabled="saving || !form.name">{{ saving ? 'Saving...' : 'Save Collection' }}</button>
          <button v-if="editingId" class="secondary-button" type="button" @click="resetForm">Cancel Edit</button>
        </div>
        <div v-if="editingId" class="wide-field assignment-panel">
          <label>Assigned Products
            <select v-model="selectedProductIds" multiple size="8">
              <option v-for="product in products" :key="product.id" :value="product.id">
                {{ product.name }} / {{ product.category?.name }}
              </option>
            </select>
          </label>
          <button class="secondary-button" type="button" :disabled="assignmentSaving" @click="saveProductAssignment">
            {{ assignmentSaving ? 'Saving Assignment...' : 'Save Product Assignment' }}
          </button>
        </div>
      </form>
      <div class="collection-admin-panel">
        <div class="collection-card-grid">
          <article v-for="collection in collections" :key="collection.id" class="admin-collection-card">
            <img :src="collection.displayImageUrl || collection.heroImageUrl || '/catalog/seasonal.svg'" :alt="collection.name" />
            <div>
              <p class="eyebrow">{{ collection.featured ? 'Featured collection' : 'Collection' }}</p>
              <h2>{{ collection.name }}</h2>
              <p>{{ collection.description }}</p>
              <div class="status-pair">
                <StatusBadge :value="collection.status" />
                <span class="featured-marker">{{ collection.productCount }} products</span>
              </div>
              <div class="table-actions">
                <button class="text-button" type="button" @click="editCollection(collection)">Edit</button>
                <button class="text-button danger" type="button" @click="removeCollection(collection)">Delete</button>
              </div>
            </div>
          </article>
        </div>
        <div class="admin-table-wrap">
        <table class="admin-table compact-table">
          <thead><tr><th>Name</th><th>Status</th><th>Products</th><th>Featured</th><th>Actions</th></tr></thead>
          <tbody>
            <tr v-for="collection in collections" :key="collection.id">
              <td><strong>{{ collection.name }}</strong><span>{{ collection.slug }}</span></td>
              <td><StatusBadge :value="collection.status" /></td>
              <td>{{ collection.productCount }}</td>
              <td>{{ collection.featured ? 'Yes' : 'No' }}</td>
              <td>
                <div class="table-actions">
                  <button class="text-button" type="button" @click="editCollection(collection)">Edit</button>
                  <button class="text-button danger" type="button" @click="removeCollection(collection)">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </div>
    <ConfirmationDialog v-bind="confirmation" @confirm="confirm" @cancel="cancel" />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  createAdminCollection,
  deleteAdminCollection,
  fetchAdminCollections,
  fetchAdminProducts,
  updateAdminCollection,
  updateAdminProduct
} from '../../api/admin'
import { getApiError } from '../../api/client'
import ConfirmationDialog from '../../components/ConfirmationDialog.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { useConfirmDialog } from '../../composables/useConfirmDialog'

const loading = ref(true)
const { confirmation, requestConfirmation, confirm, cancel } = useConfirmDialog()
const saving = ref(false)
const error = ref('')
const collections = ref([])
const products = ref([])
const editingId = ref(null)
const selectedProductIds = ref([])
const assignmentSaving = ref(false)
const form = reactive(emptyForm())

onMounted(loadCollections)

async function loadCollections() {
  loading.value = true
  error.value = ''
  try {
    const [collectionData, productData] = await Promise.all([
      fetchAdminCollections(),
      fetchAdminProducts()
    ])
    collections.value = collectionData
    products.value = productData
    if (editingId.value) {
      selectProductsForCollection(editingId.value)
    }
  } catch (requestError) {
    error.value = getApiError(requestError, 'Collections could not be loaded.')
  } finally {
    loading.value = false
  }
}

async function saveCollection() {
  saving.value = true
  error.value = ''
  try {
    const payload = { ...form, slug: form.slug || null }
    if (editingId.value) {
      await updateAdminCollection(editingId.value, payload)
    } else {
      await createAdminCollection(payload)
    }
    resetForm()
    await loadCollections()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Collection could not be saved.')
  } finally {
    saving.value = false
  }
}

function editCollection(collection) {
  editingId.value = collection.id
  Object.assign(form, {
    name: collection.name,
    slug: collection.slug,
    description: collection.description || '',
    heroImageUrl: collection.heroImageUrl || '',
    displayImageUrl: collection.displayImageUrl || '',
    status: collection.status || 'ACTIVE',
    featured: Boolean(collection.featured),
    sortOrder: collection.sortOrder || 0
  })
  selectProductsForCollection(collection.id)
}

async function removeCollection(collection) {
  const confirmed = await requestConfirmation({
    eyebrow: 'Collection removal',
    title: `Delete ${collection.name}?`,
    message: 'Assigned products must be removed first. If deletion succeeds, campaign merchandising and promotion targets using this collection will no longer reference it.',
    confirmLabel: 'Delete collection',
    tone: 'danger'
  })
  if (!confirmed) return
  try {
    await deleteAdminCollection(collection.id)
    collections.value = collections.value.filter((entry) => entry.id !== collection.id)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Collection could not be deleted.')
  }
}

function resetForm() {
  editingId.value = null
  selectedProductIds.value = []
  Object.assign(form, emptyForm())
}

async function saveProductAssignment() {
  const collectionId = editingId.value
  if (!collectionId) return

  assignmentSaving.value = true
  error.value = ''
  const selected = new Set(selectedProductIds.value.map(Number))
  const affectedProducts = products.value.filter((product) => {
    return product.collection?.id === collectionId || selected.has(product.id)
  })

  try {
    await Promise.all(affectedProducts.map((product) => updateAdminProduct(
      product.id,
      productPayload(product, {
        collectionId: selected.has(product.id) ? collectionId : null
      })
    )))
    await loadCollections()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Product assignment could not be saved.')
  } finally {
    assignmentSaving.value = false
  }
}

function selectProductsForCollection(collectionId) {
  selectedProductIds.value = products.value
    .filter((product) => product.collection?.id === collectionId)
    .map((product) => product.id)
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

function emptyForm() {
  return {
    name: '',
    slug: '',
    description: '',
    heroImageUrl: '/catalog/seasonal.svg',
    displayImageUrl: '/catalog/seasonal.svg',
    status: 'ACTIVE',
    featured: true,
    sortOrder: 0
  }
}
</script>
