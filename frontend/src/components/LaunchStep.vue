<template>
  <section class="onboarding-step">
    <p class="eyebrow">Step 5</p>
    <h2>Finish setup</h2>
    <p>Review the store summary before opening the merchant content workspace.</p>
    <div class="launch-summary-card">
      <span class="store-logo-preview" :style="{ background: model.brandColor }">{{ model.logoText || 'NS' }}</span>
      <div>
        <h3>{{ model.name || 'New Store' }}</h3>
        <p>{{ model.description || 'A NovaCart merchant storefront.' }}</p>
        <span>{{ selectedTemplate?.name }} / {{ model.products.length }} products / {{ model.currency }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  model: {
    type: Object,
    required: true
  },
  templates: {
    type: Array,
    required: true
  }
})

const selectedTemplate = computed(() => props.templates.find((template) => template.id === props.model.template))
</script>
