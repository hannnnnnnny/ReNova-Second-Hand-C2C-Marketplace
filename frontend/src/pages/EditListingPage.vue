<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { listingApi, categoryApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const categories = ref([])
const submitting = ref(false)
const loading = ref(true)
const CONDITIONS = ['NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'FOR_PARTS']

const form = ref({
  title: '', description: '', price: '', originalPrice: '',
  condition: 'GOOD', categoryId: '', location: '',
  negotiable: true, shippingFee: '0', imagesText: '', status: 'ACTIVE'
})

onMounted(async () => {
  try {
    const [cats, listing] = await Promise.all([
      categoryApi.list(),
      listingApi.get(route.params.id)
    ])
    categories.value = cats
    Object.assign(form.value, {
      title: listing.title,
      description: listing.description,
      price: listing.price,
      originalPrice: listing.originalPrice || '',
      condition: listing.condition,
      categoryId: listing.category.id,
      location: listing.location || '',
      negotiable: listing.negotiable,
      shippingFee: listing.shippingFee ?? 0,
      imagesText: (listing.imageUrls || []).join('\n'),
      status: listing.status === 'SOLD' ? 'ACTIVE' : listing.status
    })
  } catch (err) {
    toast.error(apiError(err))
    router.replace({ name: 'my-listings' })
  } finally { loading.value = false }
})

async function submit() {
  const imageUrls = form.value.imagesText.split(/\r?\n/).map((s) => s.trim()).filter(Boolean)
  submitting.value = true
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      price: form.value.price === '' ? null : Number(form.value.price),
      originalPrice: form.value.originalPrice === '' ? null : Number(form.value.originalPrice),
      condition: form.value.condition,
      categoryId: Number(form.value.categoryId),
      location: form.value.location,
      negotiable: form.value.negotiable,
      shippingFee: form.value.shippingFee === '' ? null : Number(form.value.shippingFee),
      imageUrls: imageUrls.length ? imageUrls : null,
      status: form.value.status
    }
    const updated = await listingApi.update(route.params.id, payload)
    toast.success(t('common.save'))
    router.push({ name: 'listing-detail', params: { id: updated.id } })
  } catch (err) { toast.error(apiError(err)) } finally { submitting.value = false }
}

async function archive() {
  if (!confirm('Remove this listing?')) return
  try {
    await listingApi.remove(route.params.id)
    toast.success(t('common.save'))
    router.push({ name: 'my-listings' })
  } catch (err) { toast.error(apiError(err)) }
}
</script>

<template>
  <main class="page">
    <div class="container" style="max-width: 820px">
      <h1 style="margin-bottom: 24px">{{ t('common.edit') }}</h1>
      <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
      <form v-else class="panel" @submit.prevent="submit">
        <div class="field">
          <label class="label">{{ t('post.itemTitle') }}</label>
          <input class="input" v-model="form.title" required maxlength="140" />
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
            <label class="label">{{ t('post.priceLabel') }}</label>
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
            <label class="label">{{ t('post.shippingFeeLabel') }}</label>
            <input class="input" type="number" v-model="form.shippingFee" min="0" step="0.01" />
          </div>
        </div>
        <div class="field">
          <label class="checkbox"><input type="checkbox" v-model="form.negotiable" /> {{ t('post.negotiableLabel') }}</label>
        </div>
        <div class="field">
          <label class="label">{{ t('post.descriptionLabel') }}</label>
          <textarea class="textarea" v-model="form.description" required maxlength="4000"></textarea>
        </div>
        <div class="field">
          <label class="label">{{ t('post.imagesLabel') }}</label>
          <textarea class="textarea" v-model="form.imagesText"></textarea>
        </div>
        <div class="row" style="justify-content: space-between">
          <button class="btn btn-danger" type="button" @click="archive">{{ t('common.delete') }}</button>
          <button class="btn btn-primary btn-lg" :disabled="submitting" type="submit">{{ submitting ? t('common.saving') : t('common.save') }}</button>
        </div>
      </form>
    </div>
  </main>
</template>
