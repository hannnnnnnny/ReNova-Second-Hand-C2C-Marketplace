<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Demo checkout"
      :title="`${store.name} checkout`"
      description="This generated storefront uses a safe local demo order flow for multi-merchant preview."
    />
    <EmptyState v-if="!items.length" title="Cart is empty" message="Add products before checkout.">
      <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Browse products</RouterLink>
    </EmptyState>
    <form v-else class="checkout-layout" @submit.prevent="submitOrder">
      <div class="checkout-form-panel">
        <ErrorMessage v-if="error" :message="error" />
        <section class="form-section">
          <h2>Customer information</h2>
          <div class="form-grid">
            <label>Full name<input v-model.trim="form.name" autocomplete="name" required placeholder="Morgan Lee" /></label>
            <label>Email<input v-model.trim="form.email" autocomplete="email" type="email" required placeholder="morgan@example.com" /></label>
            <label>Phone<input v-model.trim="form.phone" placeholder="+64 20 0000 0000" /></label>
          </div>
        </section>
        <section class="form-section">
          <h2>Shipping</h2>
          <div class="form-grid">
            <label class="wide-field">Address<input v-model.trim="form.address" autocomplete="shipping street-address" required placeholder="12 Market Street" /></label>
            <label>City<input v-model.trim="form.city" autocomplete="shipping address-level2" required placeholder="Auckland" /></label>
            <label>Region<input v-model.trim="form.region" placeholder="Auckland" /></label>
            <label>Postal code<input v-model.trim="form.postalCode" autocomplete="shipping postal-code" required placeholder="1010" /></label>
            <label>Country<input v-model.trim="form.country" autocomplete="shipping country-name" required placeholder="New Zealand" /></label>
          </div>
        </section>
        <section class="form-section">
          <h2>Delivery method</h2>
          <div class="delivery-choice-grid">
            <label class="choice-card"><input v-model="form.deliveryMethod" type="radio" value="STANDARD" /><span><strong>Standard delivery</strong><small>3-6 business days / $6.00</small></span></label>
            <label class="choice-card"><input v-model="form.deliveryMethod" type="radio" value="EXPRESS" /><span><strong>Express delivery</strong><small>1-3 business days / $14.00</small></span></label>
            <label class="choice-card"><input v-model="form.deliveryMethod" type="radio" value="PICKUP" /><span><strong>Merchant pickup</strong><small>Local demo pickup / free</small></span></label>
          </div>
        </section>
        <section class="form-section">
          <h2>Demo payment</h2>
          <label class="choice-card"><input v-model="form.payment" type="radio" value="PAID" /><span><strong>Mock card approved</strong><small>No real payment is processed.</small></span></label>
          <label class="choice-card"><input v-model="form.payment" type="radio" value="UNPAID" /><span><strong>Manual test payment</strong><small>Creates an unpaid local demo order.</small></span></label>
          <label class="checkbox-field"><input v-model="form.refundPolicy" type="checkbox" /> I understand this store has a 30-day refund request window.</label>
        </section>
      </div>
      <CartSummary :item-count="itemCount" :subtotal="subtotal" :discount-total="discountTotal" :shipping="shipping">
        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? 'Placing order...' : 'Place demo order' }}
        </button>
        <RouterLink class="text-link" :to="`/store/${store.slug}/cart`">Return to cart</RouterLink>
      </CartSummary>
    </form>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import CartSummary from '../../components/CartSummary.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import PageHeader from '../../components/PageHeader.vue'
import { useStorefrontCartStore } from '../../stores/storefrontCart'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const cartStore = useStorefrontCartStore()
const error = ref('')
const form = reactive({
  name: '',
  email: '',
  phone: '',
  address: '',
  city: '',
  region: '',
  postalCode: '',
  country: '',
  deliveryMethod: 'STANDARD',
  payment: 'PAID',
  refundPolicy: false
})
const items = computed(() => cartStore.itemsForStore(props.store.slug))
const itemCount = computed(() => cartStore.itemCountForStore(props.store.slug))
const subtotal = computed(() => cartStore.subtotalForStore(props.store.slug))
const discountTotal = computed(() => cartStore.discountTotalForStore(props.store.slug))
const shipping = computed(() => {
  if (!items.value.length) return 0
  if (form.deliveryMethod === 'EXPRESS') return 14
  if (form.deliveryMethod === 'PICKUP') return 0
  return 6
})
const submitting = ref(false)

function submitOrder() {
  if (submitting.value) return
  if (!form.name || !form.email || !form.address || !form.city || !form.country || !form.refundPolicy) {
    error.value = 'Complete customer, shipping, and refund acknowledgement fields before placing the order.'
    return
  }
  if (!form.postalCode) {
    error.value = 'Enter a postal code before placing the order.'
    return
  }
  if (!isValidEmail(form.email)) {
    error.value = 'Enter a valid email address before placing the order.'
    return
  }
  submitting.value = true
  const orderId = `local-${props.store.slug}-${Date.now()}`
  const order = {
    id: orderId,
    storeSlug: props.store.slug,
    storeName: props.store.name,
    customer: { ...form },
    items: items.value,
    subtotal: subtotal.value,
    discountTotal: discountTotal.value,
    shipping: shipping.value,
    total: subtotal.value + shipping.value,
    paymentStatus: form.payment,
    deliveryMethod: form.deliveryMethod,
    createdAt: new Date().toISOString()
  }
  localStorage.setItem(`novacart_order_${orderId}`, JSON.stringify(order))
  cartStore.clearStoreCart(props.store.slug)
  router.push(`/store/${props.store.slug}/order-success?order=${orderId}`)
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value).trim())
}
</script>
