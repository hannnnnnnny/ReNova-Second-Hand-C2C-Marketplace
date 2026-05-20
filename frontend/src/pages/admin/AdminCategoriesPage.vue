<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Catalog"
      title="Categories"
      description="Keep fashion category names, descriptions, and storefront visibility clear for customers."
    >
      <template #actions>
        <button class="secondary-button" type="button" @click="resetForm">New Category</button>
      </template>
    </PageHeader>
    <div class="split-admin-layout">
      <form class="admin-form single-column" @submit.prevent="submitCategory">
        <h2>{{ editingId ? 'Edit Category' : 'New Category' }}</h2>
        <ErrorMessage v-if="formError" :message="formError" />
        <label>
          Name
          <input v-model.trim="form.name" required maxlength="120" placeholder="Category name" />
        </label>
        <label>
          Slug
          <input
            v-model.trim="form.slug"
            :aria-invalid="Boolean(form.slug && !slugIsValid)"
            maxlength="140"
            placeholder="optional-category-slug"
          />
          <small v-if="form.slug && !slugIsValid" class="field-error">
            Use lowercase letters, numbers, and hyphens only.
          </small>
        </label>
        <label>
          Description
          <textarea v-model.trim="form.description" maxlength="500" rows="4" placeholder="Describe the category."></textarea>
        </label>
        <label>
          Display Image URL
          <input v-model.trim="form.imageUrl" maxlength="600" placeholder="/catalog/women.svg" />
        </label>
        <label>
          Sort Order
          <input v-model.number="form.sortOrder" min="0" type="number" />
        </label>
        <label class="checkbox-field">
          <input v-model="form.active" type="checkbox" />
          Active in storefront
        </label>
        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? 'Saving Category...' : 'Save Category' }}
        </button>
      </form>

      <div>
        <label class="search-field category-search">
          Search categories
          <input v-model.trim="searchTerm" type="search" placeholder="Search by name or slug" />
        </label>
        <LoadingState v-if="loading" message="Loading categories..." />
        <ErrorMessage v-else-if="listError" :message="listError" />
        <EmptyState
          v-else-if="!filteredCategories.length"
          title="No categories yet"
          message="Create categories to organize storefront products."
        />
        <div v-else class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Slug</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="category in filteredCategories" :key="category.id">
                <td>
                  <strong>{{ category.name }}</strong>
                  <span>{{ category.description || 'No description provided.' }}</span>
                  <span v-if="category.imageUrl">{{ category.imageUrl }}</span>
                </td>
                <td>{{ category.slug }}</td>
                <td>
                  <StatusBadge :value="category.active ? 'active' : 'inactive'" :label="category.active ? 'Active' : 'Inactive'" />
                </td>
                <td>
                  <div class="table-actions">
                    <button class="text-button" type="button" @click="editCategory(category)">Edit</button>
                    <button class="text-button danger" type="button" @click="removeCategory(category)">Delete</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <ConfirmationDialog v-bind="confirmation" @confirm="confirm" @cancel="cancel" />
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createAdminCategory,
  deleteAdminCategory,
  fetchAdminCategories,
  updateAdminCategory
} from '../../api/admin'
import { getApiError } from '../../api/client'
import ConfirmationDialog from '../../components/ConfirmationDialog.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useConfirmDialog } from '../../composables/useConfirmDialog'

const categories = ref([])
const { confirmation, requestConfirmation, confirm, cancel } = useConfirmDialog()
const loading = ref(true)
const submitting = ref(false)
const listError = ref('')
const formError = ref('')
const editingId = ref(null)
const searchTerm = ref('')
const toastMessage = ref('')
let toastTimer
const form = reactive({
  name: '',
  slug: '',
  description: '',
  imageUrl: '',
  sortOrder: 0,
  active: true
})
const filteredCategories = computed(() => {
  const query = searchTerm.value.toLowerCase()
  if (!query) return categories.value
  return categories.value.filter((category) => {
    return `${category.name} ${category.slug} ${category.description || ''}`.toLowerCase().includes(query)
  })
})
const slugIsValid = computed(() => {
  return !form.slug || /^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(form.slug)
})

onMounted(loadCategories)

async function loadCategories() {
  loading.value = true
  listError.value = ''
  try {
    categories.value = await fetchAdminCategories()
  } catch (requestError) {
    listError.value = getApiError(requestError, 'Categories could not be loaded.')
  } finally {
    loading.value = false
  }
}

function editCategory(category) {
  editingId.value = category.id
  form.name = category.name
  form.slug = category.slug
  form.description = category.description || ''
  form.imageUrl = category.imageUrl || ''
  form.sortOrder = category.sortOrder || 0
  form.active = category.active
  formError.value = ''
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.slug = ''
  form.description = ''
  form.imageUrl = ''
  form.sortOrder = 0
  form.active = true
  formError.value = ''
}

async function submitCategory() {
  formError.value = ''
  if (!form.name) {
    formError.value = 'Category name is required.'
    return
  }
  if (!slugIsValid.value) {
    formError.value = 'Category slug must use lowercase letters, numbers, and hyphens.'
    return
  }

  submitting.value = true
  const payload = {
    ...form,
    slug: form.slug || null,
    description: form.description || null,
    imageUrl: form.imageUrl || null,
    sortOrder: Number(form.sortOrder) || 0
  }

  try {
    if (editingId.value) {
      await updateAdminCategory(editingId.value, payload)
    } else {
      await createAdminCategory(payload)
    }
    resetForm()
    await loadCategories()
    showToast('Category saved successfully.')
  } catch (requestError) {
    formError.value = getApiError(requestError, 'Category could not be saved.')
  } finally {
    submitting.value = false
  }
}

async function removeCategory(category) {
  const confirmed = await requestConfirmation({
    eyebrow: 'Category removal',
    title: `Delete ${category.name}?`,
    message: 'Products assigned to this category will prevent deletion. If deletion succeeds, the category will disappear from storefront organization.',
    confirmLabel: 'Delete category',
    tone: 'danger'
  })
  if (!confirmed) return

  try {
    await deleteAdminCategory(category.id)
    categories.value = categories.value.filter((entry) => entry.id !== category.id)
    if (editingId.value === category.id) {
      resetForm()
    }
    showToast(`${category.name} deleted.`)
  } catch (requestError) {
    listError.value = getApiError(requestError, 'Category could not be deleted.')
  }
}

function showToast(message) {
  toastMessage.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
