<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { formatPrice } from '../utils/format'

const props = defineProps({
  listing: { type: Object, required: true },
  showFav: { type: Boolean, default: false },
  favorited: { type: Boolean, default: false }
})
const emit = defineEmits(['favorite'])
const { t } = useI18n()
const router = useRouter()

const cover = computed(() => props.listing.coverImageUrl || (props.listing.imageUrls?.[0]))
const status = computed(() => props.listing.status)

// Deterministic per-listing photo ratio so cards stagger into a waterfall
// regardless of the source image's real dimensions (the demo assets are all
// square). Mix of square and taller crops; stable per id, so no reflow.
const PHOTO_RATIOS = ['1 / 1', '4 / 5', '5 / 6', '1 / 1', '5 / 4', '3 / 4', '4 / 5', '1 / 1']
const photoRatio = computed(() => {
  const id = Number(props.listing.id) || 0
  return PHOTO_RATIOS[id % PHOTO_RATIOS.length]
})

function goDetail() {
  router.push({ name: 'listing-detail', params: { id: props.listing.id } })
}
function toggleFav(e) {
  e.stopPropagation()
  emit('favorite', props.listing)
}
</script>

<template>
  <article class="listing-card" @click="goDetail">
    <div class="photo" :style="{ aspectRatio: photoRatio }">
      <img v-if="cover" class="photo-img" :src="cover" :alt="listing.title" loading="lazy" />
      <div v-else class="photo-ph"></div>
      <span v-if="status && status !== 'ACTIVE'" class="status-pill">{{ t(`listingStatus.${status}`) }}</span>
      <button v-if="showFav" class="fav" :class="{ 'is-on': favorited }" @click="toggleFav" aria-label="favorite">
        {{ favorited ? '♥' : '♡' }}
      </button>
    </div>
    <div class="body">
      <div class="title">{{ listing.title }}</div>
      <div class="price-row">
        <span class="price">{{ formatPrice(listing.price) }}</span>
        <span v-if="listing.originalPrice && Number(listing.originalPrice) > Number(listing.price)" class="price-orig">{{ formatPrice(listing.originalPrice) }}</span>
      </div>
      <div class="meta">
        <span>{{ t(`condition.${listing.condition}`) }}</span>
        <span v-if="listing.location">·</span>
        <span v-if="listing.location">{{ listing.location }}</span>
      </div>
    </div>
  </article>
</template>
