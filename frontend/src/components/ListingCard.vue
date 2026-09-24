<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Heart, ImageOff } from 'lucide-vue-next'
import { formatPrice } from '../utils/format'

const props = defineProps({
  listing: { type: Object, required: true },
  showFav: { type: Boolean, default: false },
  favorited: { type: Boolean, default: false }
})
const emit = defineEmits(['favorite'])
const { t } = useI18n()

const cover = computed(() => props.listing.coverImageUrl || props.listing.imageUrls?.[0])
const imageAvailable = ref(true)
const showImage = computed(() => Boolean(cover.value) && imageAvailable.value)
const status = computed(() => props.listing.status)
const detailTarget = computed(() => ({ name: 'listing-detail', params: { id: props.listing.id } }))
const favoriteLabel = computed(() => t(
  props.favorited ? 'cardUi.removeFavorite' : 'cardUi.addFavorite',
  { title: props.listing.title }
))

watch(cover, () => {
  imageAvailable.value = true
})

function handleImageError() {
  imageAvailable.value = false
}

function toggleFavorite() {
  emit('favorite', props.listing)
}
</script>

<template>
  <article class="rn-product-card">
    <RouterLink class="rn-product-link" :to="detailTarget">
      <div class="rn-product-media">
        <img v-if="showImage" class="rn-product-image" :src="cover" :alt="listing.title" loading="lazy" @error="handleImageError" />
        <div v-else class="rn-product-placeholder" role="img" :aria-label="t('cardUi.imageUnavailable')">
          <ImageOff :size="30" :stroke-width="1.6" aria-hidden="true" />
          <span>{{ t('cardUi.imageUnavailable') }}</span>
        </div>
        <span v-if="status && status !== 'ACTIVE'" class="rn-product-status">{{ t(`listingStatus.${status}`) }}</span>
      </div>

      <div class="rn-product-body">
        <h3 class="rn-product-title">{{ listing.title }}</h3>
        <div class="rn-product-price-row">
          <span class="rn-product-price">{{ formatPrice(listing.price) }}</span>
          <span v-if="listing.originalPrice && Number(listing.originalPrice) > Number(listing.price)" class="rn-product-original-price">
            {{ formatPrice(listing.originalPrice) }}
          </span>
        </div>
        <div class="rn-product-meta">
          <span class="rn-product-condition">{{ t(`condition.${listing.condition}`) }}</span>
          <span v-if="listing.location" class="rn-product-location">{{ listing.location }}</span>
        </div>
      </div>
    </RouterLink>

    <button
      v-if="showFav"
      class="rn-product-favorite"
      :class="{ 'is-active': favorited }"
      type="button"
      :aria-label="favoriteLabel"
      :aria-pressed="favorited"
      @click="toggleFavorite"
    >
      <Heart :size="19" :stroke-width="2" :fill="favorited ? 'currentColor' : 'none'" aria-hidden="true" />
    </button>
  </article>
</template>

<style scoped>
.rn-product-card {
  position: relative;
  min-width: 0;
  overflow: hidden;
  border: 0;
  border-radius: var(--radius-lg);
  background: var(--bg-elevated);
  box-shadow: var(--shadow-sm);
  transition: transform var(--dur-fast) var(--ease), box-shadow var(--dur) var(--ease);
}

.rn-product-card:hover { box-shadow: var(--shadow-lg); }
.rn-product-card:active { transform: scale(0.98); }

.rn-product-link { display: block; color: inherit; }
.rn-product-link:hover { color: inherit; }
.rn-product-link:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: -2px;
  border-radius: inherit;
}

.rn-product-media {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 5;
  overflow: hidden;
  background: var(--bg);
}

.rn-product-image { width: 100%; height: 100%; padding: 8px; object-fit: contain; transition: transform var(--dur) var(--ease); }
.rn-product-card:hover .rn-product-image { transform: scale(1.05); }
.rn-product-placeholder {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.3;
  text-align: center;
}

.rn-product-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 9px;
  border-radius: 999px;
  background: rgba(29, 29, 31, 0.85);
  color: white;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
}

.rn-product-body { display: flex; flex-direction: column; gap: 6px; padding: 16px 16px 18px; }
.rn-product-title {
  display: -webkit-box;
  min-height: 38px;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 500;
  line-height: 1.33;
}

.rn-product-price-row { display: flex; min-width: 0; align-items: baseline; gap: 7px; }
.rn-product-price {
  color: var(--text);
  font-family: var(--font-display);
  font-size: 19px;
  font-weight: 600;
  letter-spacing: -.01em;
  line-height: 1.1;
}

.rn-product-original-price {
  overflow: hidden;
  color: var(--text-soft);
  font-size: 13px;
  text-decoration: line-through;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rn-product-meta { display: flex; min-width: 0; align-items: center; gap: 8px; font-size: 12px; line-height: 1.3; }
.rn-product-condition {
  flex: none;
  padding: 3px 8px;
  border-radius: 999px;
  background: var(--bg-muted);
  color: var(--text);
  font-weight: 500;
}

.rn-product-location {
  overflow: hidden;
  color: var(--text-muted);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rn-product-favorite {
  position: absolute;
  z-index: 2;
  top: 9px;
  right: 9px;
  display: inline-flex;
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: var(--text-muted);
  box-shadow: var(--shadow-sm);
  transition: transform var(--dur-fast) var(--ease), color var(--dur-fast) var(--ease);
}
.rn-product-favorite:active { transform: scale(0.9); }

.rn-product-favorite:hover,
.rn-product-favorite.is-active { color: var(--accent); }
.rn-product-favorite:focus-visible {
  outline: 3px solid color-mix(in srgb, var(--primary) 62%, white);
  outline-offset: 2px;
}

@media (max-width: 480px) {
  .rn-product-body { gap: 6px; padding: 10px 10px 12px; }
  .rn-product-title { min-height: 35px; font-size: 13px; }
  .rn-product-price { font-size: 17px; }
}

@media (prefers-reduced-motion: reduce) {
  .rn-product-card { transition: none; }
}
</style>
