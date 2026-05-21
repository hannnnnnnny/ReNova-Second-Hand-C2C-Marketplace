<template>
  <section class="onboarding-step">
    <p class="eyebrow">Step 3</p>
    <h2>Add first products</h2>
    <p>Start with a few products so the merchant can preview a real storefront immediately.</p>
    <div class="first-products-list">
      <article v-for="(product, index) in model.products" :key="index" class="first-product-row">
        <label>Product name<input v-model.trim="product.name" placeholder="Signature tote" /></label>
        <label>Price<input v-model.number="product.price" min="0" step="0.01" type="number" /></label>
        <label>Category<input v-model.trim="product.category" placeholder="Bags" /></label>
        <button class="text-button danger" type="button" @click="removeProduct(index)">Remove</button>
      </article>
    </div>
    <button class="secondary-button" type="button" @click="addProduct">Add another product</button>
  </section>
</template>

<script setup>
import { publicAsset } from '../utils/publicPath'

const props = defineProps({
  model: {
    type: Object,
    required: true
  }
})

function addProduct() {
  props.model.products.push({ name: '', price: 48, category: 'New Arrivals', imageUrl: publicAsset('demo-images/products/boutique-shirt.jpg') })
}

function removeProduct(index) {
  if (props.model.products.length <= 1) return
  props.model.products.splice(index, 1)
}
</script>
