<template>
  <section class="admin-page">
    <AdminPageHeader
      eyebrow="Merchant onboarding"
      :title="`${store.name} setup`"
      description="Track the core steps required to turn this merchant workspace into a published storefront."
    >
      <template #actions>
        <RouterLink class="secondary-button" :to="`/store/${store.slug}`">Preview store</RouterLink>
        <button class="primary-button" type="button" :disabled="!draft.name || !draft.slug" @click="publishStore">Publish store</button>
      </template>
    </AdminPageHeader>
    <div class="store-setup-layout">
      <SetupChecklist :store="store" />
      <StoreTemplatePreview :store="store" />
      <article class="dashboard-section">
        <h2>Store details</h2>
        <div v-if="successMessage" class="success-message" role="status">{{ successMessage }}</div>
        <div class="settings-grid compact-settings-grid">
          <label>Store name<input v-model.trim="draft.name" /></label>
          <label>Slug<input v-model.trim="draft.slug" /></label>
          <label>Category<input v-model.trim="draft.category" /></label>
          <label class="wide-field">Description<textarea v-model.trim="draft.description" rows="3"></textarea></label>
          <label class="wide-field">Shipping message<input v-model.trim="draft.shippingMessage" /></label>
        </div>
        <div class="form-actions">
          <button class="primary-button" type="button" @click="saveDetails">Save setup details</button>
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
import { usePlatformStore } from '../../stores/platform'

const platformStore = usePlatformStore()
const store = computed(() => platformStore.currentStore)
const successMessage = ref('')
const draft = reactive({
  name: '',
  slug: '',
  category: '',
  description: '',
  shippingMessage: ''
})

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
    shippingMessage: store.value.shippingMessage
  })
}

function saveDetails() {
  platformStore.updateStore(store.value.slug, {
    ...draft,
    setup: {
      details: Boolean(draft.name && draft.slug),
      shipping: Boolean(draft.shippingMessage)
    }
  })
  showSuccess('Store setup details saved.')
}

function publishStore() {
  platformStore.publishStore(store.value.slug)
  showSuccess('Store published. The generated storefront is available to preview.')
}

function showSuccess(message) {
  successMessage.value = message
  window.setTimeout(() => {
    successMessage.value = ''
  }, 2600)
}
</script>
