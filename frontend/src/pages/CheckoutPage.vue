<template>
  <section class="page-section">
    <header class="section-heading">
      <p class="eyebrow">Checkout</p>
      <h1>Shipping Details</h1>
    </header>
    <EmptyState v-if="!cartStore.items.length" title="Your cart is empty" message="Add products before checkout.">
      <RouterLink class="primary-button" to="/products">Browse Products</RouterLink>
    </EmptyState>
    <form v-else class="form-grid" @submit.prevent="submitOrder">
      <ErrorMessage v-if="error" :message="error" />
      <label>Full Name<input v-model="form.customerName" required /></label>
      <label>Email<input v-model="form.customerEmail" required type="email" /></label>
      <label>Address<input v-model="form.shippingAddress" required /></label>
      <label>City<input v-model="form.city" required /></label>
      <label>Postal Code<input v-model="form.postalCode" required /></label>
      <label>Country<input v-model="form.country" required /></label>
      <aside class="summary-panel">
        <h2>Order Summary</h2>
        <p>{{ cartStore.itemCount }} items</p>
        <p>Subtotal: {{ formatCurrency(cartStore.subtotal) }}</p>
      </aside>
      <button class="primary-button" type="submit" :disabled="submitting">
        {{ submitting ? 'Placing Order...' : 'Place Order' }}
      </button>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '../api/orders'
import { getApiError } from '../api/client'
import EmptyState from '../components/EmptyState.vue'
import ErrorMessage from '../components/ErrorMessage.vue'
import { useCartStore } from '../stores/cart'
import { formatCurrency } from '../utils/format'

const router = useRouter()
const cartStore = useCartStore()
const submitting = ref(false)
const error = ref('')
const form = reactive({
  customerName: '',
  customerEmail: '',
  shippingAddress: '',
  city: '',
  postalCode: '',
  country: ''
})

onMounted(() => {
  cartStore.loadCart()
})

async function submitOrder() {
  error.value = ''
  const hasInvalidQuantity = cartStore.items.some((item) => item.quantity > item.stockQuantity)
  if (hasInvalidQuantity) {
    error.value = 'Adjust cart quantities before checkout.'
    return
  }

  submitting.value = true
  try {
    const order = await createOrder({
      ...form,
      items: cartStore.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    })
    cartStore.clearCart()
    router.push(`/order-success/${order.id}`)
  } catch (requestError) {
    error.value = getApiError(requestError, 'Order could not be placed.')
  } finally {
    submitting.value = false
  }
}
</script>
