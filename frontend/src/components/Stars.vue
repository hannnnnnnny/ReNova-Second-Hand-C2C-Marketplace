<script setup>
defineProps({
  rating: { type: Number, default: 0 },
  size: { type: Number, default: 16 }
})
</script>

<template>
  <span class="stars" :style="{ fontSize: size + 'px' }">
    <span v-for="i in 5" :key="i">{{ i <= Math.round(rating) ? '★' : '☆' }}</span>
  </span>
</template>
