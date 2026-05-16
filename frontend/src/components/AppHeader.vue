<template>
  <header class="app-header">
    <RouterLink class="brand" to="/" aria-label="NovaCart home">
      <span class="brand-mark" aria-hidden="true">N</span>
      <span>NovaCart</span>
    </RouterLink>

    <nav class="store-nav" aria-label="Storefront navigation">
      <RouterLink to="/products">Fashion</RouterLink>
      <RouterLink class="cart-link" to="/cart">
        <span>Cart</span>
        <span class="cart-count" aria-label="Cart item count">{{ cartStore.itemCount }}</span>
      </RouterLink>
      <RouterLink to="/admin/dashboard">Admin</RouterLink>
    </nav>
  </header>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../stores/cart'

const cartStore = useCartStore()

onMounted(() => {
  cartStore.loadCart()
})
</script>
