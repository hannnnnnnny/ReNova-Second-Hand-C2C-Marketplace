<template>
  <div>
    <section class="platform-page-hero template-gallery-hero">
      <p class="eyebrow">Templates</p>
      <h1>Choose a real storefront shape, not a thumbnail</h1>
      <p>Each template now has a distinct layout, merchandising focus, editable homepage image, product structure, and operations flow behind it.</p>
    </section>
    <section class="template-difference-strip" aria-label="Template builder strengths">
      <article>
        <span>1</span>
        <strong>Different layouts</strong>
        <p>Fashion, thrift, sports, home, and minimal stores lead with different page structures.</p>
      </article>
      <article>
        <span>2</span>
        <strong>Easy editing</strong>
        <p>Store name, hero image, copy, categories, and products can be changed from one simple screen.</p>
      </article>
      <article>
        <span>3</span>
        <strong>Safer forms</strong>
        <p>Public and merchant forms use submit locks and idempotent order handling for traffic spikes.</p>
      </article>
    </section>
    <section class="platform-section template-preview-index">
      <div class="platform-section-heading template-preview-heading">
        <p class="eyebrow">Homepage previews</p>
        <h2>Choose a storefront, then review every page state</h2>
        <p>Open a card to inspect the homepage, catalog, product detail, cart, checkout, order tracking, support, and merchant setup screens for that exact template.</p>
      </div>
      <div class="generated-store-grid homepage-preview-grid">
        <StoreTemplatePreview v-for="store in demoStores" :key="store.slug" :store="store" />
      </div>
    </section>
  </div>
</template>

<script setup>
import StoreTemplatePreview from '../../components/StoreTemplatePreview.vue'
import { demoStores } from '../../data/platform'
</script>
