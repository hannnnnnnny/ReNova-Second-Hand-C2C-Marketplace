<template>
  <div class="announcement-bar" role="status">
    <span>Free shipping on orders over $75</span>
    <RouterLink :to="{ name: 'products', query: { sale: 'true', sort: 'discount' } }">
      Shop current offers
    </RouterLink>
  </div>
</template>
