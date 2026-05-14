<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Basket"
      title="Cart"
      description="Review quantities, stock limits, and order totals before checkout."
    />
    <EmptyState v-if="!cartStore.items.length" title="Your cart is empty" message="Add products to begin checkout.">
      <RouterLink class="primary-button" to="/products">Browse Products</RouterLink>
    </EmptyState>
    <div v-else class="cart-layout">
      <div class="cart-items">
        <article v-for="item in cartStore.items" :key="item.productId" class="cart-item">
          <img :src="item.imageUrl" :alt="item.name" />
          <div class="cart-item-body">
            <div>
              <h2>{{ item.name }}</h2>
              <p class="muted">{{ item.stockQuantity }} available</p>
              <strong>{{ formatCurrency(item.price) }}</strong>
            </div>
            <div class="cart-item-controls">
              <QuantityStepper
                :model-value="item.quantity"
                :max="Math.max(item.stockQuantity, 1)"
                :label="`Quantity for ${item.name}`"
                @update:model-value="updateQuantity(item.productId, $event)"
              />
              <button class="text-button danger" type="button" @click="removeItem(item)">Remove</button>
            </div>
            <p class="line-total">Line total: {{ formatCurrency(item.price * item.quantity) }}</p>
          </div>
        </article>
      </div>
      <aside class="summary-panel order-summary-card">
        <h2>Order Summary</h2>
        <div class="summary-line">
          <span>Items</span>
          <strong>{{ cartStore.itemCount }}</strong>
        </div>
        <div class="summary-line">
          <span>Subtotal</span>
          <strong>{{ formatCurrency(cartStore.subtotal) }}</strong>
        </div>
        <p class="muted">Taxes and shipping are finalized outside this sample checkout.</p>
        <RouterLink class="primary-button" to="/checkout">Continue to Checkout</RouterLink>
        <RouterLink class="text-link" to="/products">Keep Shopping</RouterLink>
      </aside>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import EmptyState from '../components/EmptyState.vue'
import PageHeader from '../components/PageHeader.vue'
import QuantityStepper from '../components/QuantityStepper.vue'
import ToastMessage from '../components/ToastMessage.vue'
import { useCartStore } from '../stores/cart'
import { formatCurrency } from '../utils/format'

const cartStore = useCartStore()
const toastMessage = ref('')
let toastTimer

onMounted(() => {
  cartStore.loadCart()
})

function updateQuantity(productId, quantity) {
  cartStore.updateQuantity(productId, quantity)
  showToast('Cart quantity updated.')
}

function removeItem(item) {
  cartStore.removeItem(item.productId)
  showToast(`${item.name} removed from cart.`)
}

function showToast(message) {
  toastMessage.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2200)
}
</script>
