<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { listingApi, offerApi, userApi } from '../api/endpoints'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatPrice, formatRelative } from '../utils/format'
import Avatar from '../components/Avatar.vue'
import Stars from '../components/Stars.vue'
import ListingCard from '../components/ListingCard.vue'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const listing = ref(null)
const loading = ref(true)
const activeImage = ref(0)
const showOfferModal = ref(false)
const offerAmount = ref('')
const offerMessage = ref('')
const sellerListings = ref([])

async function load() {
  loading.value = true
  try {
    listing.value = await listingApi.get(route.params.id)
    activeImage.value = 0
    if (listing.value?.seller?.id) {
      await loadSellerListings(listing.value.seller.id)
    } else {
      sellerListings.value = []
    }
  } catch (err) {
    toast.error(apiError(err))
    router.replace({ name: 'browse' })
  } finally {
    loading.value = false
  }
}

async function loadSellerListings(sellerId) {
  try {
    const more = await userApi.publicListings(sellerId, { page: 0, size: 4 })
    sellerListings.value = (more.content || [])
      .filter((l) => l.id !== listing.value.id)
      .slice(0, 3)
  } catch (err) {
    sellerListings.value = []
    toast.error(apiError(err, 'More listings from this seller could not be loaded.'))
  }
}

watch(() => route.params.id, load, { immediate: false })
onMounted(load)

const cover = computed(() => listing.value?.imageUrls?.[activeImage.value] || '')
const isOwner = computed(() => auth.user?.id === listing.value?.seller?.id)
const canBuy = computed(() => listing.value?.status === 'ACTIVE' && !isOwner.value)

async function toggleFav() {
  if (!auth.isAuthenticated) { router.push({ name: 'login' }); return }
  try {
    await listingApi.toggleFavorite(listing.value.id)
    listing.value.favorited = !listing.value.favorited
    listing.value.favoriteCount += listing.value.favorited ? 1 : -1
  } catch (err) { toast.error(apiError(err)) }
}

function openOffer() {
  if (!auth.isAuthenticated) { router.push({ name: 'login' }); return }
  offerAmount.value = ''
  offerMessage.value = ''
  showOfferModal.value = true
}

async function submitOffer() {
  if (!offerAmount.value) return
  try {
    await offerApi.create({ listingId: listing.value.id, amount: Number(offerAmount.value), message: offerMessage.value })
    showOfferModal.value = false
    toast.success(t('offer.sent'))
  } catch (err) { toast.error(apiError(err)) }
}

function openMessage() {
  if (!auth.isAuthenticated) { router.push({ name: 'login' }); return }
  // Route to the dedicated compose page. It loads the listing again,
  // pulls the seller's other items, and lets the buyer pick which one
  // to actually ask about before sending — replaces the old inline modal.
  router.push({ name: 'compose-message', query: { listingId: listing.value.id } })
}

function checkout() {
  if (!auth.isAuthenticated) { router.push({ name: 'login' }); return }
  router.push({ name: 'checkout', params: { listingId: listing.value.id } })
}
</script>

<template>
  <main class="page">
    <div class="container">
      <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
      <div v-else-if="listing" class="listing-detail">
        <div>
          <div class="gallery">
            <div class="main" :style="{ backgroundImage: `url('${cover}')` }"></div>
            <div class="thumbs">
              <div v-for="(src, i) in listing.imageUrls" :key="i" class="thumb" :class="{ 'is-active': i === activeImage }" :style="{ backgroundImage: `url('${src}')` }" @click="activeImage = i"></div>
            </div>
          </div>

          <div class="panel" style="margin-top: 24px">
            <h2 style="margin-bottom: 12px">{{ t('listing.description') }}</h2>
            <p style="white-space: pre-wrap">{{ listing.description }}</p>
            <div class="divider"></div>
            <div class="row-wrap">
              <span class="badge">{{ listing.category?.icon }} {{ listing.category?.name }}</span>
              <span class="badge badge-info">{{ t(`condition.${listing.condition}`) }}</span>
              <span v-if="listing.negotiable" class="badge badge-accent">{{ t('common.negotiable') }}</span>
              <span v-if="listing.location" class="badge badge-muted">📍 {{ listing.location }}</span>
            </div>
          </div>

          <div v-if="sellerListings.length" class="section" style="margin-top: 40px">
            <h3 style="margin-bottom: 16px">{{ t('listing.moreFromSeller') }}</h3>
            <div class="grid grid-listings">
              <ListingCard v-for="l in sellerListings" :key="l.id" :listing="l" />
            </div>
          </div>
        </div>

        <aside>
          <div class="panel" style="position: sticky; top: calc(var(--topbar-h) + 24px)">
            <div class="row-wrap" style="margin-bottom: 6px">
              <span v-if="listing.status !== 'ACTIVE'" class="badge badge-warning">{{ t(`listingStatus.${listing.status}`) }}</span>
            </div>
            <h1 style="margin-bottom: 14px">{{ listing.title }}</h1>
            <div class="row" style="gap: 14px; align-items: baseline; margin-bottom: 8px">
              <span style="font-family: var(--font-display); font-size: 36px; font-weight: 700;">{{ formatPrice(listing.price) }}</span>
              <span v-if="listing.originalPrice && Number(listing.originalPrice) > Number(listing.price)" class="muted" style="text-decoration: line-through">{{ formatPrice(listing.originalPrice) }}</span>
            </div>
            <div class="soft" style="margin-bottom: 24px">
              <span>{{ t('listing.postedOn') }} {{ formatRelative(listing.createdAt, locale) }}</span>
              <span> · 👁 {{ listing.viewCount }} · ♥ {{ listing.favoriteCount }}</span>
            </div>

            <div class="actions" v-if="!isOwner">
              <button v-if="canBuy" class="btn btn-primary btn-lg" @click="checkout" type="button">{{ t('listing.buyNow') }}</button>
              <button v-if="canBuy && listing.negotiable" class="btn btn-accent" @click="openOffer" type="button">{{ t('listing.makeOffer') }}</button>
              <button class="btn btn-outline" @click="openMessage" type="button" v-if="auth.user?.id !== listing.seller?.id">{{ t('listing.messageSeller') }}</button>
              <button class="btn btn-ghost" @click="toggleFav" type="button">
                {{ listing.favorited ? '♥ ' + t('listing.removeFavorite') : '♡ ' + t('listing.addFavorite') }}
              </button>
              <div v-if="listing.status === 'RESERVED'" class="badge badge-warning text-center" style="justify-content:center">{{ t('listing.reservedForBuyer') }}</div>
              <div v-if="listing.status === 'SOLD'" class="badge badge-danger text-center" style="justify-content:center">{{ t('listing.soldNotice') }}</div>
            </div>
            <div v-else class="actions">
              <RouterLink :to="{ name: 'edit-listing', params: { id: listing.id } }" class="btn btn-primary">{{ t('common.edit') }}</RouterLink>
            </div>

            <div class="divider"></div>

            <h3 style="margin-bottom: 12px">{{ t('listing.meetSeller') }}</h3>
            <RouterLink :to="{ name: 'profile', params: { id: listing.seller.id } }" class="row" style="text-decoration: none">
              <Avatar :user="listing.seller" size="lg" />
              <div>
                <div class="bold">{{ listing.seller.displayName }}</div>
                <div class="soft">
                  <Stars :rating="listing.seller.averageRating" :size="14" />
                  <span style="margin-left: 6px">{{ listing.seller.averageRating?.toFixed(1) || '—' }} ({{ listing.seller.ratingCount }})</span>
                </div>
                <div class="soft">{{ listing.seller.location || '' }}</div>
              </div>
            </RouterLink>
          </div>
        </aside>
      </div>
    </div>

    <div v-if="showOfferModal" class="modal-overlay" @click.self="showOfferModal = false">
      <div class="modal">
        <h3 style="margin-bottom: 16px">{{ t('offer.makeOfferTitle') }}</h3>
        <div class="muted" style="margin-bottom: 16px">{{ listing.title }} — {{ formatPrice(listing.price) }}</div>
        <div class="field">
          <label class="label">{{ t('offer.amount') }}</label>
          <input class="input" type="number" v-model="offerAmount" step="0.01" min="0.01" />
        </div>
        <div class="field">
          <label class="label">{{ t('offer.message') }}</label>
          <textarea class="textarea" v-model="offerMessage" maxlength="500"></textarea>
        </div>
        <div class="row" style="justify-content: flex-end">
          <button class="btn btn-ghost" @click="showOfferModal = false" type="button">{{ t('common.cancel') }}</button>
          <button class="btn btn-primary" @click="submitOffer" type="button">{{ t('offer.submit') }}</button>
        </div>
      </div>
    </div>

  </main>
</template>
