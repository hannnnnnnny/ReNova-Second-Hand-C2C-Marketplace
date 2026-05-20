<template>
  <div class="category-tile-grid">
    <RouterLink v-for="category in uniqueCategories" :key="category" class="category-tile compact-category-tile" :to="{ path: productsPath, query: { category } }">
      <span>{{ category }}</span>
      <small>Shop {{ category.toLowerCase() }}</small>
    </RouterLink>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  categories: {
    type: Array,
    required: true
  },
  productsPath: {
    type: String,
    required: true
  }
})

const uniqueCategories = computed(() => {
  const seen = new Set()
  return props.categories.filter((category) => {
    const key = String(category || '').trim().toLowerCase()
    if (!key || seen.has(key)) return false
    seen.add(key)
    return true
  })
})
</script>
