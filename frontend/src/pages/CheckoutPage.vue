<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Checkout"
      title="Secure Demo Checkout"
      description="Confirm contact details, shipping, and demo-safe payment before placing the fashion order."
    />
    <ol class="checkout-progress" aria-label="Checkout progress">
      <li class="active">Cart</li>
      <li class="active">Shipping</li>
      <li class="active">Payment</li>
      <li>Confirmation</li>
    </ol>
    <EmptyState v-if="!cartStore.items.length" title="Your cart is empty" message="Add products before checkout.">
      <RouterLink class="primary-button" to="/products">Browse Fashion</RouterLink>
    </EmptyState>
    <form v-else class="checkout-layout" novalidate @submit.prevent="submitOrder">
      <div class="checkout-form-panel">
        <ErrorMessage v-if="error" :message="error" />
        <section class="form-section">
          <h2>Customer Information</h2>
          <div class="form-grid">
            <label :class="{ 'has-field-error': submitted && formErrors.customerName }">
              Full Name
              <input v-model.trim="form.customerName" :aria-invalid="Boolean(submitted && formErrors.customerName)" placeholder="Morgan Lee" />
              <small v-if="submitted && formErrors.customerName" class="field-error">{{ formErrors.customerName }}</small>
            </label>
            <label :class="{ 'has-field-error': submitted && formErrors.customerEmail }">
              Email
              <input v-model.trim="form.customerEmail" :aria-invalid="Boolean(submitted && formErrors.customerEmail)" type="email" placeholder="morgan@example.com" />
              <small v-if="submitted && formErrors.customerEmail" class="field-error">{{ formErrors.customerEmail }}</small>
            </label>
          </div>
        </section>
        <section class="form-section">
          <h2>Shipping Address</h2>
          <div class="form-grid">
            <label class="wide-field" :class="{ 'has-field-error': submitted && formErrors.shippingAddress }">
              Address
              <input v-model.trim="form.shippingAddress" :aria-invalid="Boolean(submitted && formErrors.shippingAddress)" placeholder="12 Market Street" />
              <small v-if="submitted && formErrors.shippingAddress" class="field-error">{{ formErrors.shippingAddress }}</small>
            </label>
            <label :class="{ 'has-field-error': submitted && formErrors.city }">
              City
              <input v-model.trim="form.city" :aria-invalid="Boolean(submitted && formErrors.city)" placeholder="Auckland" />
              <small v-if="submitted && formErrors.city" class="field-error">{{ formErrors.city }}</small>
            </label>
            <label :class="{ 'has-field-error': submitted && formErrors.postalCode }">
              Postal Code
              <input v-model.trim="form.postalCode" :aria-invalid="Boolean(submitted && formErrors.postalCode)" placeholder="1010" />
              <small v-if="submitted && formErrors.postalCode" class="field-error">{{ formErrors.postalCode }}</small>
            </label>
            <label :class="{ 'has-field-error': submitted && formErrors.country }">
              Country
              <input v-model.trim="form.country" :aria-invalid="Boolean(submitted && formErrors.country)" placeholder="New Zealand" />
              <small v-if="submitted && formErrors.country" class="field-error">{{ formErrors.country }}</small>
            </label>
          </div>
        </section>
        <section class="form-section">
          <h2>Delivery Method</h2>
          <div class="option-grid">
            <label v-for="option in shippingOptions" :key="option.value" class="choice-card">
              <input v-model="form.shippingMethod" type="radio" :value="option.value" />
              <span>
                <strong>{{ option.label }}</strong>
                <small>{{ option.description }}</small>
              </span>
              <strong>{{ formatCurrency(option.price) }}</strong>
            </label>
          </div>
        </section>
        <section class="form-section">
          <h2>Demo Payment</h2>
          <div class="option-grid">
            <label class="choice-card">
              <input v-model="form.paymentMethod" type="radio" value="Demo Card Approved" />
              <span>
                <strong>Demo Card Approved</strong>
                <small>Creates a paid demo order without contacting a real provider.</small>
              </span>
            </label>
            <label class="choice-card">
              <input v-model="form.paymentMethod" type="radio" value="Demo Card Declined" />
              <span>
                <strong>Demo Card Declined</strong>
                <small>Use this option to preview checkout failure handling.</small>
              </span>
            </label>
          </div>
          <p class="demo-payment-note">No real card data is collected or processed in NovaCart.</p>
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
          <span>Subtotal</span>
          <strong>{{ formatCurrency(cartStore.subtotal) }}</strong>
        </div>
        <div class="summary-line">
          <span>Shipping</span>
          <strong>{{ formatCurrency(shippingAmount) }}</strong>
        </div>
        <div class="summary-line">
          <span>Estimated Tax</span>
          <strong>{{ formatCurrency(taxAmount) }}</strong>
        </div>
        <div class="summary-line total-line">
          <span>Total</span>
          <strong>{{ formatCurrency(orderTotal) }}</strong>
        </div>
        <div class="checkout-note">
          <strong>Stock is checked when the order is placed.</strong>
          <span>Demo payment can be approved or declined without using real card data.</span>
        </div>
        <button class="primary-button" type="submit" :disabled="submitting">
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
const submitted = ref(false)
const error = ref('')
const form = reactive({
  customerName: '',
  customerEmail: '',
  shippingAddress: '',
  city: '',
  postalCode: '',
  country: '',
  shippingMethod: 'STANDARD',
  paymentMethod: 'Demo Card Approved'
})
const shippingOptions = [
  {
    value: 'STANDARD',
    label: 'Standard Delivery',
    description: 'Reliable delivery for most fashion orders.',
    price: 6
  },
  {
    value: 'EXPRESS',
    label: 'Express Delivery',
    description: 'Priority handling for event, travel, or campaign timing.',
    price: 14
  },
  {
    value: 'PICKUP',
    label: 'Store Pickup',
    description: 'No shipping fee for showroom pickup orders.',
    price: 0
  }
]
const formIsComplete = computed(() => {
  return Object.values(form).every((value) => String(value).trim().length > 0)
})
const emailIsValid = computed(() => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.customerEmail)
})
const hasInvalidCartQuantity = computed(() => {
  return cartStore.items.some((item) => item.quantity > item.stockQuantity)
})
const formErrors = computed(() => ({
  customerName: form.customerName ? '' : 'Full name is required.',
  customerEmail: !form.customerEmail
    ? 'Email address is required.'
    : emailIsValid.value
      ? ''
      : 'Enter a valid email address.',
  shippingAddress: form.shippingAddress ? '' : 'Shipping address is required.',
  city: form.city ? '' : 'City is required.',
  postalCode: form.postalCode ? '' : 'Postal code is required.',
  country: form.country ? '' : 'Country is required.'
}))
const formIsValid = computed(() => {
  return formIsComplete.value && emailIsValid.value
})
const selectedShippingOption = computed(() => {
  return shippingOptions.find((option) => option.value === form.shippingMethod) || shippingOptions[0]
})
const shippingAmount = computed(() => selectedShippingOption.value.price)
const taxAmount = computed(() => Number((cartStore.subtotal * 0.08).toFixed(2)))
const orderTotal = computed(() => cartStore.subtotal + shippingAmount.value + taxAmount.value)

onMounted(() => {
  cartStore.loadCart()
})

async function submitOrder() {
  submitted.value = true
  error.value = ''
  if (!formIsValid.value) {
    error.value = 'Review the highlighted checkout fields before placing the order.'
    return
  }

  if (hasInvalidCartQuantity.value) {
    error.value = 'Adjust cart quantities before checkout.'
    return
  }

  submitting.value = true
  try {
    const order = await createOrder({
      ...form,
      idempotencyKey: createIdempotencyKey(),
      simulatePaymentFailure: form.paymentMethod === 'Demo Card Declined',
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

function createIdempotencyKey() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID()
  }
  return `checkout-${Date.now()}-${Math.random().toString(16).slice(2)}`
}
</script>
