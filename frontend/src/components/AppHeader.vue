<template>
  <header class="app-header retail-header">
    <RouterLink class="brand retail-brand" to="/" aria-label="NovaCart home">
      <span class="brand-mark" aria-hidden="true">N</span>
      <span>NovaCart</span>
    </RouterLink>

    <nav class="store-nav retail-category-nav" aria-label="Store categories">
      <RouterLink v-for="item in categoryLinks" :key="item.label" :to="item.to">
        {{ item.label }}
      </RouterLink>
    </nav>

    <div class="header-actions">
      <form class="header-search" role="search" @submit.prevent="submitSearch">
        <SearchInput
          v-model="searchTerm"
          label="Search NovaCart"
          placeholder="Search styles"
          @submit="submitSearch"
        />
      </form>
      <RouterLink class="icon-link" to="/admin/dashboard" aria-label="Admin account">
        <UserRound aria-hidden="true" />
        <span>Admin</span>
      </RouterLink>
      <RouterLink class="icon-link cart-link" to="/cart" aria-label="Shopping cart">
        <ShoppingBag aria-hidden="true" />
        <span class="cart-count" aria-label="Cart item count">{{ cartStore.itemCount }}</span>
      </RouterLink>
      <button class="icon-button mobile-menu-button" type="button" :aria-expanded="menuOpen" @click="menuOpen = !menuOpen">
        <X v-if="menuOpen" aria-hidden="true" />
        <Menu v-else aria-hidden="true" />
        <span class="sr-only">{{ menuOpen ? 'Close menu' : 'Open menu' }}</span>
      </button>
    </div>

    <nav v-if="menuOpen" class="mobile-store-menu" aria-label="Mobile store navigation">
      <RouterLink v-for="item in categoryLinks" :key="item.label" :to="item.to" @click="menuOpen = false">
        {{ item.label }}
      </RouterLink>
      <RouterLink to="/support" @click="menuOpen = false">Support</RouterLink>
      <RouterLink to="/refund-request" @click="menuOpen = false">Refunds</RouterLink>
      <RouterLink to="/admin/dashboard" @click="menuOpen = false">Merchant Admin</RouterLink>
    </nav>
  </header>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, ShoppingBag, UserRound, X } from 'lucide-vue-next'
import SearchInput from './SearchInput.vue'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const searchTerm = ref('')
const menuOpen = ref(false)
const categoryLinks = [
  { label: 'New Arrivals', to: { name: 'products', query: { tag: 'new-arrival', sort: 'newest' } } },
  { label: 'Sale', to: { name: 'products', query: { sale: 'true', sort: 'discount' } } },
  { label: "Women's Fashion", to: { name: 'products', query: { search: 'women' } } },
  { label: "Men's Fashion", to: { name: 'products', query: { search: 'men' } } },
  { label: 'Household', to: { name: 'products', query: { search: 'home' } } },
  { label: 'Kids Collection', to: { name: 'products', query: { search: 'kids' } } },
  { label: 'Vintage', to: { name: 'products', query: { search: 'vintage' } } },
  { label: 'Sports', to: { name: 'products', query: { search: 'sportswear' } } },
  { label: 'Games & Puzzles', to: { name: 'products', query: { search: 'games puzzles' } } },
  { label: 'Special Interest', to: { name: 'products', query: { search: 'collectible' } } }
]

onMounted(() => {
  cartStore.loadCart()
})

function submitSearch() {
  const search = searchTerm.value.trim()
  if (!search) return
  menuOpen.value = false
  router.push({ name: 'products', query: { search } })
}
</script>
