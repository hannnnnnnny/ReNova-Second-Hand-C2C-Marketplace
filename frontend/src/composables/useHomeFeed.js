import { ref } from 'vue'
import { categoryApi, listingApi } from '../api/endpoints'

function resource(fetchData) {
  const items = ref([])
  const loading = ref(false)
  const error = ref(false)

  async function load() {
    if (loading.value) return
    loading.value = true
    error.value = false
    try {
      items.value = await fetchData()
    } catch {
      // Keep a distinct failure state; a failed request is not empty inventory.
      error.value = true
    } finally {
      loading.value = false
    }
  }
  return { items, loading, error, load }
}

export function useHomeFeed() {
  const listings = resource(async () => (await listingApi.search({ page: 0, size: 8, sort: 'newest' })).content || [])
  const categories = resource(async () => (await categoryApi.list()) || [])
  return {
    listings: listings.items, listingsLoading: listings.loading, listingsError: listings.error,
    categories: categories.items, categoriesLoading: categories.loading, categoriesError: categories.error,
    loadListings: listings.load, loadCategories: categories.load,
    load: () => Promise.all([listings.load(), categories.load()])
  }
}
