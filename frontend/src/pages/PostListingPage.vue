<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { listingApi, categoryApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import ListingImageUploader from '../components/ListingImageUploader.vue'

const { t } = useI18n()
const router = useRouter()
const toast = useToastStore()
const categories = ref([])
const submitting = ref(false)
const CONDITIONS = ['NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'FOR_PARTS']

const form = ref({
  title: '',
  description: '',
  price: '',
  originalPrice: '',
  condition: 'GOOD',
  categoryId: '',
  location: '',
  negotiable: true,
  shippingFee: '0',
  images: []
})

onMounted(async () => {
  try {
    categories.value = await categoryApi.list()
    if (categories.value.length) form.value.categoryId = categories.value[0].id
  } catch (err) { toast.error(apiError(err)) }
})

async function submit() {
  const mediaIds = form.value.images.filter((image) => image.id && !image.uploading).map((image) => image.id)
  if (!mediaIds.length || mediaIds.length !== form.value.images.length) {
    toast.error(t('post.validationImages'))
    return
  }
  submitting.value = true
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      price: Number(form.value.price),
      originalPrice: form.value.originalPrice ? Number(form.value.originalPrice) : null,
      condition: form.value.condition,
      categoryId: Number(form.value.categoryId),
      location: form.value.location,
      negotiable: form.value.negotiable,
      shippingFee: form.value.shippingFee ? Number(form.value.shippingFee) : 0,
      mediaIds
    }
    const created = await listingApi.create(payload)
    toast.success(t('post.created'))
    router.push({ name: 'listing-detail', params: { id: created.id } })
  } catch (err) { toast.error(apiError(err)) } finally { submitting.value = false }
}
</script>

<template>
  <main class="page">
    <div class="container" style="max-width: 820px">
      <h1 style="margin-bottom: 24px">{{ t('post.title') }}</h1>
      <form class="panel" @submit.prevent="submit">
        <div class="field">
          <label class="label">{{ t('post.itemTitle') }}</label>
          <input class="input" v-model="form.title" :placeholder="t('post.itemTitlePlaceholder')" required maxlength="140" />
        </div>

        <div class="form-grid">
          <div class="field">
            <label class="label">{{ t('post.categoryLabel') }}</label>
            <select class="select" v-model="form.categoryId" required>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
            </select>
          </div>
          <div class="field">
            <label class="label">{{ t('post.conditionLabel') }}</label>
            <select class="select" v-model="form.condition" required>
              <option v-for="c in CONDITIONS" :key="c" :value="c">{{ t(`condition.${c}`) }}</option>
            </select>
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label class="label">{{ t('post.priceLabel') }} (USD)</label>
            <input class="input" type="number" v-model="form.price" min="0.01" step="0.01" required />
          </div>
          <div class="field">
            <label class="label">{{ t('post.originalPriceLabel') }}</label>
            <input class="input" type="number" v-model="form.originalPrice" min="0" step="0.01" />
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label class="label">{{ t('post.locationLabel') }}</label>
            <input class="input" v-model="form.location" maxlength="120" />
          </div>
          <div class="field">
            <label class="label">{{ t('post.shippingFeeLabel') }} (USD)</label>
            <input class="input" type="number" v-model="form.shippingFee" min="0" step="0.01" />
            <span class="help">{{ t('post.shippingFreeHint') }}</span>
          </div>
        </div>

        <div class="field">
          <label class="checkbox"><input type="checkbox" v-model="form.negotiable" /> {{ t('post.negotiableLabel') }}</label>
        </div>

        <div class="field">
          <label class="label">{{ t('post.descriptionLabel') }}</label>
          <textarea class="textarea" v-model="form.description" :placeholder="t('post.descriptionPlaceholder')" required maxlength="4000"></textarea>
        </div>

        <div class="field">
          <label class="label">{{ t('post.imagesLabel') }}</label>
          <ListingImageUploader v-model="form.images" />
        </div>

        <div class="row" style="justify-content: flex-end">
          <button class="btn btn-primary btn-lg" :disabled="submitting" type="submit">{{ submitting ? t('common.saving') : t('post.submit') }}</button>
        </div>
      </form>
    </div>
  </main>
</template>
