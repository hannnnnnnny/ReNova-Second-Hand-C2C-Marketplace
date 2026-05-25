<script setup>
import { computed } from 'vue'
import { initials, avatarBackground } from '../utils/format'

const props = defineProps({
  user: { type: Object, required: false, default: null },
  size: { type: String, default: 'md' }
})

const name = computed(() => props.user?.displayName || '·')
const url = computed(() => props.user?.avatarUrl || '')
const bg = computed(() => avatarBackground(name.value))
const klass = computed(() => ['avatar', props.size === 'lg' ? 'avatar-lg' : ''])
</script>

<template>
  <span :class="klass" :style="{ background: bg }">
    <img v-if="url" :src="url" :alt="name" />
    <span v-else>{{ initials(name) }}</span>
  </span>
</template>
