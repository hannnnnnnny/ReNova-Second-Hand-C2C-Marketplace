<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Store cart"
      :title="`${store.name} cart`"
      description="Review products from this merchant before continuing to demo checkout."
    />
    <EmptyState v-if="!items.length" title="Your cart is empty" :message="`Browse ${store.name} and add products to checkout.`">
      <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Browse products</RouterLink>
    </EmptyState>
    <div v-else class="cart-layout">
      <div class="cart-items">
        <article v-for="item in items" :key="item.itemId || item.productId" class="cart-item fashion-cart-item">
          <img :src="item.imageUrl" :alt="item.name" loading="lazy" decoding="async" />
          <div class="cart-item-body">
            <h2>{{ item.name }}</h2>
            <p v-if="selectedOptionsLabel(item)" class="cart-variant-line">{{ selectedOptionsLabel(item) }}</p>
            <div class="price-stack">
              <strong>{{ formatCurrency(item.price) }}</strong>
              <span v-if="item.compareAtPrice">{{ formatCurrency(item.compareAtPrice) }}</span>
            </div>
            <div class="cart-item-controls">
              <QuantityStepper :model-value="item.quantity" :max="Math.max(item.stockQuantity, 1)" @update:model-value="cartStore.updateQuantity(store.slug, item.itemId || item.productId, $event)" />
              <button class="text-button danger" type="button" @click="cartStore.removeItem(store.slug, item.itemId || item.productId)">Remove</button>
            </div>
          </div>
        </article>
      </div>
      <CartSummary
        :item-count="itemCount"
        :subtotal="subtotal"
        :discount-total="discountTotal"
        :shipping="shipping"
      >
        <RouterLink class="primary-button" :to="`/store/${store.slug}/checkout`">Continue to checkout</RouterLink>
        <RouterLink class="text-link" :to="`/store/${store.slug}/products`">Keep shopping</RouterLink>
      </CartSummary>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import CartSummary from '../../components/CartSummary.vue'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import QuantityStepper from '../../components/QuantityStepper.vue'
import { useStorefrontCartStore } from '../../stores/storefrontCart'
import { formatCurrency } from '../../utils/format'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const cartStore = useStorefrontCartStore()
const items = computed(() => cartStore.itemsForStore(props.store.slug))
const itemCount = computed(() => cartStore.itemCountForStore(props.store.slug))
const subtotal = computed(() => cartStore.subtotalForStore(props.store.slug))
const discountTotal = computed(() => cartStore.discountTotalForStore(props.store.slug))
const shipping = computed(() => (items.value.length ? 6 : 0))

function selectedOptionsLabel(item) {
  return [item.options?.size ? `Size ${item.options.size}` : '', item.options?.color ? `Color ${item.options.color}` : '']
    .filter(Boolean)
    .join(' / ')
}
</script>
