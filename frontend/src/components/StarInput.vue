<script setup>
const props = defineProps({
  modelValue: { type: Number, default: 0 }
})
const emit = defineEmits(['update:modelValue'])
function pick(n) { emit('update:modelValue', n) }
</script>

<template>
  <span class="star-input">
    <button v-for="i in 5" :key="i" type="button" :class="{ 'is-on': i <= props.modelValue }" @click="pick(i)">★</button>
  </span>
</template>
