<template>
  <section class="storefront-hero" aria-label="NovaCart storefront hero">
    <div class="storefront-hero-copy">
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
      <div class="hero-actions">
        <RouterLink class="primary-button" :to="ctaTo">{{ ctaLabel }}</RouterLink>
        <RouterLink class="secondary-button" :to="{ name: 'products', query: { sale: 'true', sort: 'discount' } }">
          Browse Sale
        </RouterLink>
      </div>
    </div>
    <div class="storefront-hero-visual" aria-hidden="true">
      <img src="/catalog/seasonal.svg" alt="" />
      <div class="hero-visual-note">
        <span>Curated edit</span>
        <strong>Timeless layers, rare finds, everyday polish.</strong>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'NovaCart Finds'
  },
  title: {
    type: String,
    default: 'Classic, elegant, uniquely yours'
  },
  description: {
    type: String,
    default: 'Shop timeless fashion and one-of-a-kind finds.'
  },
  ctaLabel: {
    type: String,
    default: 'Shop Now'
  },
  ctaTo: {
    type: Object,
    default: () => ({ name: 'products', query: { tag: 'new-arrival', sort: 'newest' } })
  }
})
</script>
