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

onMounted(async () => {
  try {
    const result = await listingApi.mine({ page: 0, size: 48 })
    listings.value = result.content || []
  } catch (err) { toast.error(apiError(err)) } finally { loading.value = false }
})
</script>

<template>
  <main class="page">
    <div class="container">
      <div class="between" style="margin-bottom: 24px">
        <h1>{{ t('post.title') }}</h1>
        <RouterLink :to="{ name: 'post-listing' }" class="btn btn-accent">{{ t('common.sell') }}</RouterLink>
      </div>
      <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
      <div v-else-if="listings.length === 0" class="empty-state">{{ t('profile.noListings') }}</div>
      <div v-else class="grid grid-listings">
        <ListingCard v-for="l in listings" :key="l.id" :listing="l" />
      </div>
    </div>
  </main>
</template>
