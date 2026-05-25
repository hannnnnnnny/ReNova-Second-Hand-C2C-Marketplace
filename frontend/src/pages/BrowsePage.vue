<script setup>
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { listingApi, categoryApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import ListingCard from '../components/ListingCard.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const categories = ref([])
const listings = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(0)

const filters = ref({
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : '',
  minPrice: route.query.minPrice || '',
  maxPrice: route.query.maxPrice || '',
  condition: route.query.condition || '',
  location: route.query.location || '',
  sort: route.query.sort || 'newest'
})

const CONDITIONS = ['NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'FOR_PARTS']
const SORTS = ['newest', 'price_asc', 'price_desc', 'popular']

async function loadCategories() {
  try { categories.value = await categoryApi.list() } catch {}
}

async function search() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 24,
      keyword: filters.value.keyword || undefined,
      categoryId: filters.value.categoryId || undefined,
      minPrice: filters.value.minPrice || undefined,
      maxPrice: filters.value.maxPrice || undefined,
      condition: filters.value.condition || undefined,
      location: filters.value.location || undefined,
      sort: filters.value.sort
    }
    const result = await listingApi.search(params)
    listings.value = result.content || []
    total.value = result.totalElements || 0
  } catch (err) {
    toast.error(apiError(err))
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  page.value = 0
  router.replace({ query: cleanQuery() })
  search()
}

function reset() {
  filters.value = { keyword: '', categoryId: '', minPrice: '', maxPrice: '', condition: '', location: '', sort: 'newest' }
  applyFilters()
}

function cleanQuery() {
  const q = {}
  Object.entries(filters.value).forEach(([k, v]) => {
    if (v !== '' && v !== null && v !== undefined) q[k] = v
  })
  return q
}

watch(() => route.query, (next) => {
  filters.value.keyword = next.keyword || ''
  filters.value.categoryId = next.categoryId ? Number(next.categoryId) : ''
  page.value = 0
  search()
})

onMounted(async () => {
  await loadCategories()
  search()
})
</script>

<template>
  <main class="page">
    <div class="container">
      <h1 style="margin-bottom: 24px">{{ t('common.browse') }}</h1>
      <div class="two-col">
        <aside class="filters">
          <div class="card">
            <div class="field">
              <label class="label">{{ t('common.categories') }}</label>
              <select class="select" v-model="filters.categoryId">
                <option value="">{{ t('common.anywhere') }}</option>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
              </select>
            </div>
            <div class="field">
              <label class="label">{{ t('common.condition') }}</label>
              <select class="select" v-model="filters.condition">
                <option value="">{{ t('common.anyCondition') }}</option>
                <option v-for="c in CONDITIONS" :key="c" :value="c">{{ t(`condition.${c}`) }}</option>
              </select>
            </div>
            <div class="field">
              <label class="label">{{ t('common.price') }}</label>
              <div class="row">
                <input class="input" type="number" :placeholder="t('common.from')" v-model="filters.minPrice" />
                <input class="input" type="number" :placeholder="t('common.to')" v-model="filters.maxPrice" />
              </div>
            </div>
            <div class="field">
              <label class="label">{{ t('common.location') }}</label>
              <input class="input" v-model="filters.location" :placeholder="t('common.anywhere')" />
            </div>
            <div class="row" style="gap:8px">
              <button class="btn btn-primary grow" @click="applyFilters" type="button">{{ t('common.apply') }}</button>
              <button class="btn btn-ghost" @click="reset" type="button">{{ t('common.reset') }}</button>
            </div>
          </div>
        </aside>

        <section>
          <div class="between" style="margin-bottom: 16px">
            <div class="muted">{{ total }} {{ t('common.browse').toLowerCase() }}</div>
            <select class="select" style="max-width: 220px" v-model="filters.sort" @change="applyFilters">
              <option v-for="s in SORTS" :key="s" :value="s">{{ t(`sort.${s}`) }}</option>
            </select>
          </div>

          <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
          <div v-else-if="listings.length === 0" class="empty-state">{{ t('common.empty') }}</div>
          <div v-else class="grid grid-listings">
            <ListingCard v-for="l in listings" :key="l.id" :listing="l" />
          </div>
        </section>
      </div>
    </div>
  </main>
</template>
