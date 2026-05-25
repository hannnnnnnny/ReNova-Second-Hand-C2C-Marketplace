<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { listingApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import ListingCard from '../components/ListingCard.vue'

const { t } = useI18n()
const toast = useToastStore()
const listings = ref([])
const loading = ref(true)

async function refresh() {
  loading.value = true
  try {
    const result = await listingApi.favorites({ page: 0, size: 48 })
    listings.value = result.content || []
  } catch (err) { toast.error(apiError(err)) } finally { loading.value = false }
}

async function unfav(listing) {
  try {
    await listingApi.toggleFavorite(listing.id)
    listings.value = listings.value.filter((l) => l.id !== listing.id)
  } catch (err) { toast.error(apiError(err)) }
}

onMounted(refresh)
</script>

<template>
  <main class="page">
    <div class="container">
      <h1 style="margin-bottom: 24px">{{ t('common.favorites') }}</h1>
      <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
      <div v-else-if="listings.length === 0" class="empty-state">{{ t('common.empty') }}</div>
      <div v-else class="grid grid-listings">
        <ListingCard v-for="l in listings" :key="l.id" :listing="l" :show-fav="true" :favorited="true" @favorite="unfav" />
      </div>
    </div>
  </main>
</template>
