<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Checkout"
      title="Shipping Details"
      description="Confirm your contact information and shipping address before placing the order."
    />
    <ol class="checkout-progress" aria-label="Checkout progress">
      <li class="active">Cart</li>
      <li class="active">Shipping</li>
      <li>Confirmation</li>
    </ol>
    <EmptyState v-if="!cartStore.items.length" title="Your cart is empty" message="Add products before checkout.">
      <RouterLink class="primary-button" to="/products">Browse Products</RouterLink>
    </EmptyState>
    <form v-else class="checkout-layout" @submit.prevent="submitOrder">
      <div class="checkout-form-panel">
        <ErrorMessage v-if="error" :message="error" />
        <section class="form-section">
          <h2>Customer Information</h2>
          <div class="form-grid">
            <label>Full Name<input v-model.trim="form.customerName" required placeholder="Morgan Lee" /></label>
            <label>Email<input v-model.trim="form.customerEmail" required type="email" placeholder="morgan@example.com" /></label>
          </div>
        </section>
        <section class="form-section">
          <h2>Shipping Address</h2>
          <div class="form-grid">
            <label class="wide-field">Address<input v-model.trim="form.shippingAddress" required placeholder="12 Market Street" /></label>
            <label>City<input v-model.trim="form.city" required placeholder="Auckland" /></label>
            <label>Postal Code<input v-model.trim="form.postalCode" required placeholder="1010" /></label>
            <label>Country<input v-model.trim="form.country" required placeholder="New Zealand" /></label>
          </div>
        </section>
      </div>
      <aside class="summary-panel order-summary-card">
        <h2>Order Summary</h2>
        <div class="checkout-items">
          <div v-for="item in cartStore.items" :key="item.productId" class="checkout-item">
            <span>{{ item.name }} x {{ item.quantity }}</span>
            <strong>{{ formatCurrency(item.price * item.quantity) }}</strong>
          </div>
        </div>
        <div class="summary-line">
          <span>Items</span>
          <strong>{{ cartStore.itemCount }}</strong>
        </div>
        <div class="summary-line">
          <span>Total</span>
          <strong>{{ formatCurrency(cartStore.subtotal) }}</strong>
        </div>
        <p class="muted">You can update fulfillment status from the admin workspace after checkout.</p>
        <button class="primary-button" type="submit" :disabled="submitting || !formIsComplete">
          {{ submitting ? 'Placing Order...' : 'Place Order' }}
        </button>
        <RouterLink class="text-link" to="/cart">Return to Cart</RouterLink>
      </aside>
    </form>
  </section>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '../api/orders'
import { getApiError } from '../api/client'
import EmptyState from '../components/EmptyState.vue'
import ErrorMessage from '../components/ErrorMessage.vue'
import PageHeader from '../components/PageHeader.vue'
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
const formIsComplete = computed(() => {
  return Object.values(form).every((value) => String(value).trim().length > 0)
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
