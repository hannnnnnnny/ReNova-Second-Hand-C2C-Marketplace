<template>
  <RouterLink class="category-tile" :to="to">
    <span class="category-tile-image">
      <img :src="image" :alt="`${title} category preview`" />
    </span>
    <span>
      <strong>{{ title }}</strong>
      <small>{{ description }}</small>
    </span>
  </RouterLink>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  image: {
    type: String,
    default: '/catalog/seasonal.svg'
  },
  to: {
    type: [String, Object],
    required: true
  }
})
</script>
