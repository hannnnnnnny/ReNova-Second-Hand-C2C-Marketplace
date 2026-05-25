<script setup>
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { userApi, reviewApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatDate, formatRelative } from '../utils/format'
import Avatar from '../components/Avatar.vue'
import Stars from '../components/Stars.vue'
import ListingCard from '../components/ListingCard.vue'

const { t, locale } = useI18n()
const route = useRoute()
const toast = useToastStore()
const profile = ref(null)
const listings = ref([])
const reviews = ref([])
const loading = ref(true)
const tab = ref('listings')

async function load() {
  loading.value = true
  try {
    const id = route.params.id
    const [p, l, r] = await Promise.all([
      userApi.publicProfile(id),
      userApi.publicListings(id, { page: 0, size: 24 }),
      reviewApi.forUser(id)
    ])
    profile.value = p
    listings.value = l.content || []
    reviews.value = r
  } catch (err) { toast.error(apiError(err)) } finally { loading.value = false }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <main class="page">
    <div class="container" v-if="profile">
      <div class="panel" style="margin-bottom: 24px">
        <div class="row" style="gap: 20px">
          <Avatar :user="profile" size="lg" />
          <div class="grow">
            <h1 style="margin-bottom: 6px">{{ profile.displayName }}</h1>
            <div class="row-wrap">
              <Stars :rating="profile.averageRating" :size="16" />
              <span class="muted">{{ profile.averageRating?.toFixed(1) || '—' }} ({{ profile.ratingCount }} {{ t('common.reviews').toLowerCase() }})</span>
              <span v-if="profile.location" class="badge badge-muted">📍 {{ profile.location }}</span>
              <span class="badge badge-info">{{ t('common.member') }} {{ formatDate(profile.memberSince, locale) }}</span>
            </div>
            <p v-if="profile.bio" style="margin-top: 12px">{{ profile.bio }}</p>
          </div>
        </div>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ 'is-active': tab === 'listings' }" @click="tab = 'listings'" type="button">{{ t('profile.listings') }} ({{ listings.length }})</button>
        <button class="tab" :class="{ 'is-active': tab === 'reviews' }" @click="tab = 'reviews'" type="button">{{ t('profile.reviewsReceived') }} ({{ reviews.length }})</button>
      </div>

      <div v-if="loading" class="muted">{{ t('common.loading') }}</div>

      <template v-else>
        <div v-if="tab === 'listings'">
          <div v-if="listings.length === 0" class="empty-state">{{ t('profile.noListings') }}</div>
          <div v-else class="grid grid-listings">
            <ListingCard v-for="l in listings" :key="l.id" :listing="l" />
          </div>
        </div>
        <div v-else>
          <div v-if="reviews.length === 0" class="empty-state">{{ t('common.noReviews') }}</div>
          <div v-else class="stack">
            <div v-for="r in reviews" :key="r.id" class="card">
              <div class="row" style="justify-content:space-between; margin-bottom: 8px">
                <div class="row">
                  <Avatar :user="r.reviewer" />
                  <div>
                    <div class="bold">{{ r.reviewer.displayName }}</div>
                    <Stars :rating="r.rating" :size="14" />
                  </div>
                </div>
                <div class="soft">{{ formatRelative(r.createdAt, locale) }}</div>
              </div>
              <p style="margin: 0">{{ r.comment || '—' }}</p>
              <div class="soft" style="margin-top: 6px">{{ t('offer.forListing') }}: <RouterLink :to="{ name: 'order-detail', params: { id: r.orderId } }">{{ r.listingTitle }}</RouterLink></div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </main>
</template>
