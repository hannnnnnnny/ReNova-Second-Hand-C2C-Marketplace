<template>
  <section class="onboarding-step">
    <p class="eyebrow">Step 2</p>
    <h2>Choose a template</h2>
    <p>Each template changes the generated storefront styling and merchandising tone.</p>
    <div class="template-select-grid">
      <button
        v-for="template in templates"
        :key="template.id"
        class="template-select-card"
        type="button"
        :class="{ active: model.template === template.id }"
        @click="model.template = template.id"
      >
        <img :src="template.previewImage" :alt="`${template.name} preview`" />
        <strong>{{ template.name }}</strong>
        <span>{{ template.layoutName }}</span>
        <small>{{ template.primaryGoal }}</small>
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  model: {
    type: Object,
    required: true
  },
  templates: {
    type: Array,
    required: true
  }
})
</script>
