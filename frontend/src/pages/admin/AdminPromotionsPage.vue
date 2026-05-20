<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Pricing"
      title="Promotions"
      description="Build percentage or fixed discounts for selected products, categories, collections, seasons, or tags."
    />
    <LoadingState v-if="loading" message="Loading promotions..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <div v-else class="admin-split-layout">
      <form class="admin-form" @submit.prevent="savePromotion">
        <h2>{{ editingId ? 'Edit Promotion' : 'New Promotion' }}</h2>
        <label>Name<input v-model.trim="form.name" required maxlength="140" /></label>
        <label class="wide-field">Description<textarea v-model.trim="form.description" rows="3" maxlength="800"></textarea></label>
        <label>Discount Type
          <select v-model="form.discountType">
            <option value="PERCENTAGE">Percentage</option>
            <option value="FIXED_AMOUNT">Fixed amount</option>
          </select>
        </label>
        <label>Discount Value<input v-model.number="form.discountValue" required min="0.01" step="0.01" type="number" /></label>
        <label>Target Type
          <select v-model="form.targetType" @change="resetTargetsForType">
            <option value="SELECTED_PRODUCTS">Selected products</option>
            <option value="CATEGORY">Category</option>
            <option value="COLLECTION">Collection</option>
            <option value="SEASON">Season</option>
            <option value="TAGS">Tags</option>
          </select>
        </label>
        <label v-if="form.targetType === 'TAGS'" class="wide-field">
          Target Tags
          <textarea v-model.trim="form.targetValuesText" rows="3" placeholder="sale, last-season, active-weekend"></textarea>
        </label>
        <label v-else class="wide-field">
          Promotion Targets
          <select v-model="form.targetValues" multiple size="8">
            <option v-for="option in targetOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </label>
        <p class="muted wide-field">{{ targetHelpText }}</p>
        <section class="affected-products-panel wide-field" aria-label="Affected products preview">
          <div>
            <strong>{{ affectedProducts.length }} affected products</strong>
            <span>{{ affectedProductsDescription }}</span>
          </div>
          <ul v-if="affectedProducts.length">
            <li v-for="product in affectedProducts.slice(0, 8)" :key="product.id">
              {{ product.name }} / {{ product.category?.name }} / {{ product.collection?.name || 'No collection' }}
            </li>
          </ul>
        </section>
        <label>Start Date<input v-model="form.startDate" type="date" /></label>
        <label>End Date<input v-model="form.endDate" type="date" /></label>
        <label class="checkbox-field"><input v-model="form.active" type="checkbox" /> Active</label>
        <div class="form-actions">
          <button class="primary-button" type="submit" :disabled="saving || !form.name || !form.discountValue || !targetValuesForPayload.length">{{ saving ? 'Saving...' : 'Save Promotion' }}</button>
          <button v-if="editingId" class="secondary-button" type="button" @click="resetForm">Cancel Edit</button>
        </div>
      </form>
      <div class="promotion-admin-panel">
        <div class="promotion-card-grid">
          <article v-for="promotion in promotions" :key="promotion.id" class="promotion-card">
            <div>
              <p class="eyebrow">{{ formatStatus(promotion.targetType) }}</p>
              <h2>{{ promotion.name }}</h2>
              <p>{{ promotion.description || 'Promotion without public description.' }}</p>
            </div>
            <strong>{{ promotion.discountType === 'PERCENTAGE' ? `${promotion.discountValue}%` : formatCurrency(promotion.discountValue) }}</strong>
            <div class="status-pair">
              <StatusBadge :value="promotion.active ? 'ACTIVE' : 'INACTIVE'" />
              <span class="featured-marker">{{ formatTargetValues(promotion) }}</span>
            </div>
            <div class="table-actions">
              <button class="text-button" type="button" @click="editPromotion(promotion)">Edit</button>
              <button class="text-button danger" type="button" @click="removePromotion(promotion)">Delete</button>
            </div>
          </article>
        </div>
        <div class="admin-table-wrap">
        <table class="admin-table compact-table">
          <thead><tr><th>Name</th><th>Discount</th><th>Target</th><th>Active</th><th>Actions</th></tr></thead>
          <tbody>
            <tr v-for="promotion in promotions" :key="promotion.id">
              <td><strong>{{ promotion.name }}</strong><span>{{ promotion.description }}</span></td>
              <td>{{ promotion.discountType === 'PERCENTAGE' ? `${promotion.discountValue}%` : formatCurrency(promotion.discountValue) }}</td>
              <td>{{ formatStatus(promotion.targetType) }}: {{ formatTargetValues(promotion) }}</td>
              <td><StatusBadge :value="promotion.active ? 'ACTIVE' : 'INACTIVE'" /></td>
              <td>
                <div class="table-actions">
                  <button class="text-button" type="button" @click="editPromotion(promotion)">Edit</button>
                  <button class="text-button danger" type="button" @click="removePromotion(promotion)">Delete</button>
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
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createAdminPromotion,
  deleteAdminPromotion,
  fetchAdminCategories,
  fetchAdminCollections,
  fetchAdminProducts,
  fetchAdminPromotions,
  updateAdminPromotion
} from '../../api/admin'
import { getApiError } from '../../api/client'
import ConfirmationDialog from '../../components/ConfirmationDialog.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { useConfirmDialog } from '../../composables/useConfirmDialog'
import { formatCurrency, formatStatus } from '../../utils/format'

const loading = ref(true)
const { confirmation, requestConfirmation, confirm, cancel } = useConfirmDialog()
const saving = ref(false)
const error = ref('')
const promotions = ref([])
const products = ref([])
const categories = ref([])
const collections = ref([])
const editingId = ref(null)
const form = reactive(emptyForm())
const seasonOptions = ['Spring 2026', 'Summer 2026', 'Fall Winter 2026', 'Active Weekend', 'Evening Edit', 'Last Season']
const targetOptions = computed(() => {
  if (form.targetType === 'SELECTED_PRODUCTS') {
    return products.value.map((product) => ({
      value: String(product.id),
      label: `${product.name} / ${product.sku || product.slug}`
    }))
  }
  if (form.targetType === 'CATEGORY') {
    return categories.value.map((category) => ({
      value: String(category.id),
      label: category.name
    }))
  }
  if (form.targetType === 'COLLECTION') {
    return collections.value.map((collection) => ({
      value: String(collection.id),
      label: collection.name
    }))
  }
  if (form.targetType === 'SEASON') {
    return seasonOptions.map((season) => ({ value: season, label: season }))
  }
  return []
})
const targetValuesForPayload = computed(() => {
  return form.targetType === 'TAGS'
    ? splitValues(form.targetValuesText)
    : form.targetValues.map(String).filter(Boolean)
})
const targetHelpText = computed(() => {
  const labels = {
    SELECTED_PRODUCTS: 'Choose one or more products that should receive this discount.',
    CATEGORY: 'Choose one or more categories. Current and future products in those categories can qualify.',
    COLLECTION: 'Choose one or more seasonal collections.',
    SEASON: 'Choose one or more season labels used by products.',
    TAGS: 'Enter one or more comma-separated tags such as sale, linen, or active-weekend.'
  }
  return labels[form.targetType] || ''
})
const affectedProducts = computed(() => {
  const targets = targetValuesForPayload.value.map((target) => target.toLowerCase())
  if (!targets.length) return []

  return products.value.filter((product) => {
    if (form.targetType === 'SELECTED_PRODUCTS') {
      return targets.includes(String(product.id).toLowerCase())
    }
    if (form.targetType === 'CATEGORY') {
      return targets.includes(String(product.category?.id).toLowerCase())
        || targets.includes(String(product.category?.slug || '').toLowerCase())
        || targets.includes(String(product.category?.name || '').toLowerCase())
    }
    if (form.targetType === 'COLLECTION') {
      return targets.includes(String(product.collection?.id).toLowerCase())
        || targets.includes(String(product.collection?.slug || '').toLowerCase())
        || targets.includes(String(product.collection?.name || '').toLowerCase())
    }
    if (form.targetType === 'SEASON') {
      return targets.includes(String(product.season || '').toLowerCase())
    }
    if (form.targetType === 'TAGS') {
      return product.tags?.some((tag) => targets.includes(String(tag).toLowerCase()))
    }
    return false
  })
})
const affectedProductsDescription = computed(() => {
  if (!targetValuesForPayload.value.length) return 'Choose targets to preview the promotion scope.'
  if (!affectedProducts.value.length) return 'No current products match these targets yet.'
  return 'Preview is based on the currently loaded merchant catalog.'
})

onMounted(loadPromotions)

async function loadPromotions() {
  loading.value = true
  error.value = ''
  try {
    const [promotionData, productData, categoryData, collectionData] = await Promise.all([
      fetchAdminPromotions(),
      products.value.length ? Promise.resolve(products.value) : fetchAdminProducts(),
      categories.value.length ? Promise.resolve(categories.value) : fetchAdminCategories(),
      collections.value.length ? Promise.resolve(collections.value) : fetchAdminCollections()
    ])
    promotions.value = promotionData
    products.value = productData
    categories.value = categoryData
    collections.value = collectionData
  } catch (requestError) {
    error.value = getApiError(requestError, 'Promotions could not be loaded.')
  } finally {
    loading.value = false
  }
}

async function savePromotion() {
  saving.value = true
  error.value = ''
  try {
    const payload = {
      ...form,
      discountValue: Number(form.discountValue),
      startDate: form.startDate || null,
      endDate: form.endDate || null,
      targetValues: targetValuesForPayload.value
    }
    delete payload.targetValuesText
    if (editingId.value) {
      await updateAdminPromotion(editingId.value, payload)
    } else {
      await createAdminPromotion(payload)
    }
    resetForm()
    await loadPromotions()
  } catch (requestError) {
    error.value = getApiError(requestError, 'Promotion could not be saved.')
  } finally {
    saving.value = false
  }
}

function editPromotion(promotion) {
  editingId.value = promotion.id
  Object.assign(form, {
    name: promotion.name,
    description: promotion.description || '',
    discountType: promotion.discountType,
    discountValue: Number(promotion.discountValue),
    targetType: promotion.targetType,
    targetValuesText: promotion.targetValues.join(', '),
    targetValues: promotion.targetValues.map(String),
    startDate: promotion.startDate || '',
    endDate: promotion.endDate || '',
    active: Boolean(promotion.active)
  })
}

async function removePromotion(promotion) {
  const confirmed = await requestConfirmation({
    eyebrow: 'Promotion removal',
    title: `Delete ${promotion.name}?`,
    message: 'This promotion will stop applying to future carts and will be removed from the merchant promotion list.',
    confirmLabel: 'Delete promotion',
    tone: 'danger'
  })
  if (!confirmed) return
  try {
    await deleteAdminPromotion(promotion.id)
    promotions.value = promotions.value.filter((entry) => entry.id !== promotion.id)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Promotion could not be deleted.')
  }
}

function resetForm() {
  editingId.value = null
  Object.assign(form, emptyForm())
}

function resetTargetsForType() {
  form.targetValues = []
  form.targetValuesText = form.targetType === 'TAGS' ? 'sale' : ''
}

function splitValues(value) {
  return String(value || '')
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean)
}

function emptyForm() {
  return {
    name: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: 10,
    targetType: 'TAGS',
    targetValuesText: 'sale',
    targetValues: [],
    startDate: '',
    endDate: '',
    active: true
  }
}

function formatTargetValues(promotion) {
  const values = promotion.targetValues || []
  if (!values.length) return 'No targets'
  return values.map((value) => targetLabel(promotion.targetType, value)).join(', ')
}

function targetLabel(targetType, value) {
  if (targetType === 'SELECTED_PRODUCTS') {
    return products.value.find((product) => String(product.id) === String(value))?.name || value
  }
  if (targetType === 'CATEGORY') {
    return categories.value.find((category) => String(category.id) === String(value))?.name || value
  }
  if (targetType === 'COLLECTION') {
    return collections.value.find((collection) => String(collection.id) === String(value))?.name || value
  }
  return value
}
</script>
