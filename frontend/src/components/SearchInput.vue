<template>
  <label class="search-input">
    <Search aria-hidden="true" />
    <span class="sr-only">{{ label }}</span>
    <input
      :value="modelValue"
      type="search"
      :placeholder="placeholder"
      @input="$emit('update:modelValue', $event.target.value)"
      @keydown.enter="$emit('submit')"
    />
  </label>
</template>

<script setup>
import { Search } from 'lucide-vue-next'

defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: 'Search'
  },
  placeholder: {
    type: String,
    default: 'Search products'
  }
})

defineEmits(['update:modelValue', 'submit'])
</script>
