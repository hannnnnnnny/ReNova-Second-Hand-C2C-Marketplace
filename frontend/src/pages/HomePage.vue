<script setup>
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowUpRight, ArrowRight, PackageOpen, RefreshCw } from 'lucide-vue-next'
import ListingCard from '../components/ListingCard.vue'
import { useHomeFeed } from '../composables/useHomeFeed'
import { categoryLabel } from '../i18n/marketplace-ui'

const { t, te } = useI18n()
const { listings, listingsLoading, listingsError, categories, categoriesLoading, categoriesError,
  loadListings, loadCategories, load } = useHomeFeed()
const heroImage = `${import.meta.env.BASE_URL}demo-images/products/boutique-vase.jpg`
const steps = ['discover', 'connect', 'exchange']
onMounted(load)
</script>

<template>
  <main class="page home-page">
    <div class="container">
      <section class="home-intro" aria-labelledby="home-title">
        <div class="intro-copy">
          <p class="eyebrow">{{ t('marketplaceUi.eyebrow') }}</p>
          <h1 id="home-title">{{ t('marketplaceUi.heroTitle') }}</h1>
          <p class="intro-body">{{ t('marketplaceUi.heroBody') }}</p>
          <div class="intro-actions">
            <RouterLink :to="{ name: 'browse' }" class="btn btn-primary btn-lg">{{ t('marketplaceUi.browse') }}</RouterLink>
            <RouterLink :to="{ name: 'post-listing' }" class="text-link">{{ t('marketplaceUi.sell') }}<ArrowRight :size="16" aria-hidden="true" /></RouterLink>
          </div>
        </div>
        <figure class="intro-visual">
          <img :src="heroImage" :alt="t('marketplaceUi.photoAlt')" width="900" height="900" fetchpriority="high" />
          <figcaption>{{ t('marketplaceUi.photoCaption') }}</figcaption>
        </figure>
      </section>

      <section class="home-categories" :aria-label="t('marketplaceUi.categories')">
        <div v-if="categoriesLoading" class="category-skeletons" role="status" :aria-label="t('common.loading')">
          <span v-for="n in 6" :key="n" class="skeleton category-skeleton" aria-hidden="true"></span>
        </div>
        <div v-else-if="categoriesError" class="category-error" role="alert">
          <span>{{ t('marketplaceUi.categoryError') }}</span>
          <button type="button" class="text-link" @click="loadCategories">{{ t('common.retry') }}</button>
        </div>
        <nav v-else class="category-strip" :aria-label="t('marketplaceUi.categories')">
          <RouterLink :to="{ name: 'browse' }" class="category-chip category-chip-all">{{ t('marketplaceUi.allCategories') }}<ArrowUpRight :size="15" aria-hidden="true" /></RouterLink>
          <RouterLink v-for="category in categories" :key="category.id" :to="{ name: 'browse', query: { categoryId: category.id } }" class="category-chip">
            {{ categoryLabel(category, t, te) }}
          </RouterLink>
        </nav>
      </section>

      <section class="home-listings" aria-labelledby="latest-title" :aria-busy="listingsLoading">
        <div class="section-heading">
          <div><h2 id="latest-title">{{ t('marketplaceUi.latest') }}</h2><p>{{ t('marketplaceUi.latestBody') }}</p></div>
          <RouterLink :to="{ name: 'browse' }" class="text-link">{{ t('common.seeAll') }}<ArrowRight :size="17" aria-hidden="true" /></RouterLink>
        </div>
        <div v-if="listingsLoading" class="grid grid-listings" role="status" :aria-label="t('common.loading')">
          <div v-for="n in 4" :key="n" class="listing-skeleton" aria-hidden="true">
            <div class="skeleton skeleton-photo"></div><div class="skeleton skeleton-title"></div><div class="skeleton skeleton-price"></div>
          </div>
        </div>
        <div v-else-if="listingsError" class="home-feed-state" role="alert">
          <RefreshCw :size="28" stroke-width="1.5" aria-hidden="true" />
          <h3>{{ t('marketplaceUi.loadError') }}</h3><p>{{ t('marketplaceUi.loadErrorBody') }}</p>
          <button class="btn btn-outline" type="button" @click="loadListings">{{ t('common.retry') }}</button>
        </div>
        <div v-else-if="!listings.length" class="home-feed-state">
          <PackageOpen :size="34" stroke-width="1.4" aria-hidden="true" />
          <h3>{{ t('marketplaceUi.emptyTitle') }}</h3><p>{{ t('marketplaceUi.emptyBody') }}</p>
          <RouterLink :to="{ name: 'post-listing' }" class="btn btn-primary">{{ t('marketplaceUi.sell') }}<ArrowUpRight :size="17" aria-hidden="true" /></RouterLink>
        </div>
        <div v-else class="grid grid-listings"><ListingCard v-for="listing in listings" :key="listing.id" :listing="listing" /></div>
      </section>

      <section class="home-how" aria-labelledby="how-title">
        <div class="how-heading"><p class="eyebrow">{{ t('marketplaceUi.workflowLabel') }}</p><h2 id="how-title">{{ t('marketplaceUi.workflowTitle') }}</h2></div>
        <ol class="how-steps">
          <li v-for="(step, index) in steps" :key="step">
            <span class="how-number" aria-hidden="true">0{{ index + 1 }}</span>
            <div><h3>{{ t(`marketplaceUi.steps.${step}.title`) }}</h3><p>{{ t(`marketplaceUi.steps.${step}.body`) }}</p></div>
          </li>
        </ol>
      </section>
    </div>
  </main>
</template>

<style scoped>
.home-page { padding-top: 24px; padding-bottom: 56px; min-height: auto; }
.home-intro { display: grid; grid-template-columns: 1fr; justify-items: center; text-align: center; overflow: hidden; border-radius: 24px; background: var(--bg-elevated); min-height: 328px; }
.intro-copy { align-self: center; padding: 80px 32px 24px; display: flex; flex-direction: column; align-items: center; }
.eyebrow { color: var(--text-muted); font-size: 17px; font-weight: 600; letter-spacing: -.01em; margin: 0 0 12px; }
.intro-copy h1 { max-width: 760px; font-size: clamp(44px, 7vw, 80px); line-height: 1.05; letter-spacing: -.03em; text-wrap: balance; }
.intro-body { margin: 20px auto 32px; max-width: 560px; color: var(--text-muted); font-size: 21px; line-height: 1.45; }
.intro-actions .text-link { font-size: 17px; }
.intro-actions { display: flex; align-items: center; justify-content: center; gap: 28px; flex-wrap: wrap; }
.intro-visual { margin: 0; position: relative; background: transparent; min-width: 0; width: 100%; max-width: 520px; }
.intro-visual img { width: 100%; height: auto; max-height: 420px; object-fit: contain; mix-blend-mode: multiply; padding: 0 32px 64px; }
.intro-visual figcaption { position: absolute; left: 0; right: 0; bottom: 28px; font-size: 12px; color: var(--text-muted); }
.home-categories { padding: 40px 0 56px; }
.category-strip, .category-skeletons { display: flex; gap: 10px; overflow-x: auto; padding: 4px 2px 10px; scrollbar-width: thin; scrollbar-color: var(--border) transparent; }
.category-chip { flex-shrink: 0; display: inline-flex; align-items: center; gap: 10px; border: 1px solid var(--border); background: var(--bg-elevated); padding: 10px 17px; border-radius: 30px; font-size: 13px; font-weight: 500; transition: background .15s ease, border-color .15s ease; }
.category-chip:hover { background: var(--primary-soft); border-color: #b4c8b5; }
.category-chip-all { background: var(--primary-soft); border-color: transparent; color: var(--primary-strong); }
.category-error { display: flex; gap: 14px; flex-wrap: wrap; color: var(--text-muted); font-size: 13px; }
.section-heading { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 23px; }
.section-heading h2 { font-size: 27px; letter-spacing: -.025em; }
.section-heading p { font-size: 14px; color: var(--text-muted); margin: 6px 0 0; }
.section-heading > .text-link { flex-shrink: 0; }
.home-feed-state { min-height: 240px; padding: 30px 24px; display: flex; align-items: center; flex-direction: column; text-align: center; border-radius: 16px; border: 1px solid var(--border); background: var(--bg-elevated); }
.home-feed-state > svg { color: var(--primary); margin-bottom: 14px; }
.home-feed-state h3 { font-size: 21px; margin-bottom: 8px; }
.home-feed-state p { max-width: 440px; color: var(--text-muted); font-size: 14px; margin-bottom: 20px; }
.skeleton { background: var(--bg-muted); animation: breathe 1.5s ease-in-out infinite alternate; border-radius: 8px; }
.skeleton-photo { aspect-ratio: 4/5; border-radius: 15px; }
.skeleton-title { width: 80%; height: 13px; margin-top: 16px; }
.skeleton-price { width: 35%; height: 19px; margin-top: 12px; }
.category-skeleton { width: 125px; height: 40px; flex-shrink: 0; border-radius: 30px; }
.home-how { display: grid; grid-template-columns: 1fr 1.25fr; gap: 70px; border-top: 1px solid var(--border); margin-top: 64px; padding-top: 45px; }
.how-heading h2 { font-size: 31px; max-width: 340px; letter-spacing: -.025em; text-wrap: balance; }
.how-steps { list-style: none; margin: 0; padding: 0; display: grid; gap: 24px; }
.how-steps li { display: flex; gap: 22px; }
.how-number { font-size: 12px; font-variant-numeric: tabular-nums; color: var(--primary); padding-top: 3px; }
.how-steps h3 { font-family: var(--font-body); font-size: 15px; margin-bottom: 6px; }
.how-steps p { font-size: 13px; color: var(--text-muted); line-height: 1.7; margin: 0; }
@keyframes breathe { to { opacity: .45; } }
@media (max-width: 760px) {
  .home-page { padding-top: 16px; }
  .home-intro { grid-template-columns: 1fr; min-height: auto; border-radius: 17px; }
  .intro-copy { padding: 25px 24px 29px; }
  .intro-copy h1 { max-width: 330px; font-size: 34px; }
  .intro-body { font-size: 14px; margin-bottom: 20px; }
  .intro-visual { display: none; }
  .intro-actions { gap: 18px; }
  .home-categories { padding: 14px 0 22px; }
  .section-heading { align-items: flex-start; margin-bottom: 18px; }
  .section-heading h2 { font-size: 24px; }
  .section-heading p { font-size: 12px; }
  .section-heading > .text-link { margin-top: 5px; font-size: 12px; }
  .home-feed-state { padding: 28px 20px; }
  .home-how { grid-template-columns: 1fr; gap: 28px; margin-top: 40px; padding-top: 30px; }
  .how-heading h2 { font-size: 27px; }
}
@media (prefers-reduced-motion: reduce) { .skeleton { animation: none; } }
</style>
