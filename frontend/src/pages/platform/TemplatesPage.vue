<template>
  <div>
    <section class="platform-page-hero template-gallery-hero">
      <p class="eyebrow">Templates</p>
      <h1>Preview each store like a real launch</h1>
      <p>Open a homepage preview first, then inspect the product, cart, checkout, order, support, and merchant operations screens behind that template.</p>
    </section>
    <section class="platform-section template-preview-index">
      <div class="platform-section-heading template-preview-heading">
        <p class="eyebrow">Homepage previews</p>
        <h2>Choose a storefront, then review every page state</h2>
        <p>Each card opens a focused preview gallery so the project reads like a multi-page ecommerce builder, not a set of static theme thumbnails.</p>
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
