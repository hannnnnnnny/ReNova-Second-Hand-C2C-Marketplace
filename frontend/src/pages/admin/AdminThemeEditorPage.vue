<template>
  <section class="admin-page">
    <AdminPageHeader
      eyebrow="Store builder"
      title="Theme Editor"
      description="Customize the current merchant storefront and preview the generated template."
    >
      <template #actions>
        <RouterLink class="secondary-button" :to="`/store/${store.slug}`">Open storefront</RouterLink>
        <button class="primary-button" type="button" @click="saveTheme">Save changes</button>
      </template>
    </AdminPageHeader>
    <div class="theme-editor-layout">
      <form class="admin-form theme-editor-form" @submit.prevent="saveTheme">
        <div v-if="successMessage" class="success-message" role="status">{{ successMessage }}</div>
        <label>Logo text<input v-model.trim="draft.logoText" maxlength="3" /></label>
        <label>Brand color<input v-model="draft.brandColor" type="color" /></label>
        <label>Font style
          <select v-model="draft.fontStyle">
            <option>Editorial serif</option>
            <option>Classic sans</option>
            <option>Modern condensed</option>
            <option>Warm minimal</option>
            <option>System clean</option>
          </select>
        </label>
        <label class="wide-field">Homepage hero text<input v-model.trim="draft.heroTitle" /></label>
        <label class="wide-field">Hero supporting text<textarea v-model.trim="draft.heroText" rows="3"></textarea></label>
        <label class="wide-field">Announcement bar text<input v-model.trim="draft.announcement" /></label>
        <label>Button style
          <select v-model="draft.buttonStyle">
            <option>Rounded</option>
            <option>Sharp</option>
            <option>Soft</option>
          </select>
        </label>
      </form>
      <StoreTemplatePreview :store="previewStore" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AdminPageHeader from '../../components/AdminPageHeader.vue'
import StoreTemplatePreview from '../../components/StoreTemplatePreview.vue'
import { usePlatformStore } from '../../stores/platform'

const platformStore = usePlatformStore()
const store = computed(() => platformStore.currentStore)
const successMessage = ref('')
const draft = reactive({
  logoText: '',
  brandColor: '',
  fontStyle: 'System clean',
  heroTitle: '',
  heroText: '',
  announcement: '',
  buttonStyle: 'Rounded'
})

const previewStore = computed(() => ({
  ...store.value,
  ...draft
}))

onMounted(() => {
  platformStore.loadPlatform()
  syncDraft()
})

watch(store, syncDraft)

function syncDraft() {
  Object.assign(draft, {
    logoText: store.value.logoText,
    brandColor: store.value.brandColor,
    fontStyle: store.value.fontStyle || 'System clean',
    heroTitle: store.value.heroTitle,
    heroText: store.value.heroText,
    announcement: store.value.announcement,
    buttonStyle: store.value.buttonStyle || 'Rounded'
  })
}

function saveTheme() {
  platformStore.updateStore(store.value.slug, {
    ...draft,
    setup: { preview: true }
  })
  successMessage.value = 'Theme changes saved for the current merchant store.'
  window.setTimeout(() => {
    successMessage.value = ''
  }, 2600)
}
</script>
