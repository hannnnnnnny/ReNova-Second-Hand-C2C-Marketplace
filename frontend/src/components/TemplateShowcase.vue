<template>
  <section class="platform-section">
    <div class="platform-section-heading">
      <p class="eyebrow">Store templates</p>
      <h2>Start with a storefront that fits the merchant</h2>
      <p>Templates are original NovaCart layouts for different merchant categories, not copied brand identities.</p>
    </div>
    <div class="template-showcase-grid">
      <article
        v-for="template in templates"
        :key="template.id"
        class="template-card"
        :style="{ '--template-accent': template.accentColor }"
      >
        <figure>
        <img :src="template.previewImage" :alt="`${template.name} preview`" loading="lazy" decoding="async" />
          <figcaption>{{ template.fontStyle }}</figcaption>
        </figure>
        <div>
          <h3>{{ template.name }}</h3>
          <p>{{ template.description }}</p>
          <span>Best for: {{ template.bestFor }}</span>
        </div>
        <div class="template-actions">
          <RouterLink class="secondary-button" :to="previewPath(template.id)">Preview pages</RouterLink>
          <RouterLink class="primary-button" :to="{ path: '/onboarding', query: { template: template.id } }">Select</RouterLink>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { demoStores } from '../data/platform'

defineProps({
  templates: {
    type: Array,
    required: true
  }
})

function previewPath(templateId) {
  const store = demoStores.find((entry) => entry.template === templateId)
  return store ? `/templates/${store.slug}` : '/templates'
}
</script>
