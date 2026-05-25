<script setup>
import { useToastStore } from '../stores/toast'
const toast = useToastStore()
</script>

<template>
  <div class="toast-wrap">
    <div v-for="t in toast.items" :key="t.id" class="toast" :class="t.type">{{ t.message }}</div>
  </div>
</template>
