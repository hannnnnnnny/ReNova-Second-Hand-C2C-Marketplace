<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { listingApi, categoryApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import ListingCard from '../components/ListingCard.vue'

const { t } = useI18n()
const router = useRouter()
const toast = useToastStore()
const featured = ref([])
const categories = ref([])
const loading = ref(true)

const heroPhotos = [
  'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600',
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600',
  'https://images.unsplash.com/photo-1518155317743-a8ff43ea6a5f?w=600',
  'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600'
]

onMounted(async () => {
  try {
    const [page, cats] = await Promise.all([
      listingApi.search({ page: 0, size: 12, sort: 'newest' }),
      categoryApi.list()
    ])
    featured.value = page.content || []
    categories.value = cats || []
  } catch (err) {
    toast.error(apiError(err))
  } finally {
    loading.value = false
  }
})

function browseCategory(category) {
  router.push({ name: 'browse', query: { categoryId: category.id } })
}
</script>

<template>
  <main class="page">
    <div class="container">
      <section class="hero">
        <div>
          <h1 style="margin-bottom: 16px">{{ t('home.heroTitle') }}</h1>
          <p style="font-size: 18px; color: var(--text-muted); max-width: 480px">{{ t('home.heroSubtitle') }}</p>
          <div class="row" style="margin-top: 24px; gap: 12px">
            <RouterLink :to="{ name: 'browse' }" class="btn btn-primary btn-lg">{{ t('home.ctaStart') }}</RouterLink>
            <RouterLink :to="{ name: 'post-listing' }" class="btn btn-outline btn-lg">{{ t('home.ctaSell') }}</RouterLink>
          </div>
        </div>
        <div class="hero-art">
          <div v-for="src in heroPhotos" :key="src" class="photo" :style="{ backgroundImage: `url('${src}')` }"></div>
        </div>
      </section>

      <section class="section">
        <h2 style="margin-bottom: 18px">{{ t('home.categoriesTitle') }}</h2>
        <div class="grid grid-categories">
          <button v-for="c in categories" :key="c.id" class="category-tile" @click="browseCategory(c)" type="button">
            <span class="emoji">{{ c.icon }}</span>
            <span>{{ c.name }}</span>
          </button>
        </div>
      </section>

      <section class="section">
        <div class="between" style="margin-bottom: 18px">
          <h2>{{ t('home.featured') }}</h2>
          <RouterLink :to="{ name: 'browse' }" class="btn btn-ghost btn-sm">{{ t('common.seeAll') }}</RouterLink>
        </div>
        <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
        <div v-else-if="featured.length === 0" class="empty-state">{{ t('common.empty') }}</div>
        <div v-else class="grid grid-listings">
          <ListingCard v-for="l in featured" :key="l.id" :listing="l" />
        </div>
      </section>

      <section class="section">
        <h2 style="margin-bottom: 24px" class="text-center">{{ t('home.howItWorks') }}</h2>
        <div class="grid grid-3">
          <div class="step-card">
            <div class="step-number">1</div>
            <h3>{{ t('home.step1Title') }}</h3>
            <p class="muted">{{ t('home.step1Body') }}</p>
          </div>
          <div class="step-card">
            <div class="step-number">2</div>
            <h3>{{ t('home.step2Title') }}</h3>
            <p class="muted">{{ t('home.step2Body') }}</p>
          </div>
          <div class="step-card">
            <div class="step-number">3</div>
            <h3>{{ t('home.step3Title') }}</h3>
            <p class="muted">{{ t('home.step3Body') }}</p>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
