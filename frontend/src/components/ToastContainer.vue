<script setup>
import { useToastStore } from '../stores/toast'
const toast = useToastStore()
</script>

<template>
  <TransitionGroup name="toast" tag="div" class="toast-wrap" aria-live="polite">
    <div v-for="t in toast.items" :key="t.id" class="toast" :class="t.type" :role="t.type === 'error' ? 'alert' : 'status'">{{ t.message }}</div>
  </TransitionGroup>
</template>
