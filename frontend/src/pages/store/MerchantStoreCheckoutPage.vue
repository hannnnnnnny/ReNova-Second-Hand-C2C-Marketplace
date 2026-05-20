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
            <label class="choice-card"><input v-model="form.deliveryMethod" type="radio" value="STANDARD" /><span><strong>Standard delivery</strong><small>3-6 business days / {{ standardDeliveryLabel }}</small></span></label>
            <label class="choice-card"><input v-model="form.deliveryMethod" type="radio" value="EXPRESS" /><span><strong>Express delivery</strong><small>1-3 business days / $14.00</small></span></label>
            <label class="choice-card"><input v-model="form.deliveryMethod" type="radio" value="PICKUP" /><span><strong>Merchant pickup</strong><small>Local demo pickup / free</small></span></label>
          </div>
        </section>
        <section class="form-section">
          <h2>Demo payment</h2>
          <p class="demo-payment-note">This checkout is demo-safe. It validates fields and creates a local test order, but never sends card details or processes real payments.</p>
          <div class="payment-method-grid">
            <label v-for="method in paymentMethods" :key="method.id" class="choice-card payment-choice-card">
              <input v-model="form.paymentMethod" type="radio" :value="method.id" />
              <span><strong>{{ method.label }}</strong><small>{{ method.description }}</small></span>
            </label>
          </div>
          <div v-if="form.paymentMethod === 'CARD'" class="demo-card-form" aria-label="Demo card details">
            <div class="form-grid">
              <label :class="{ 'has-field-error': paymentErrors.cardholderName }">
                Cardholder name
                <input v-model.trim="paymentDetails.cardholderName" autocomplete="cc-name" placeholder="Morgan Lee" />
                <small v-if="paymentErrors.cardholderName" class="field-error">{{ paymentErrors.cardholderName }}</small>
              </label>
              <label :class="{ 'has-field-error': paymentErrors.cardNumber }">
                Card number
                <input v-model.trim="paymentDetails.cardNumber" inputmode="numeric" autocomplete="cc-number" placeholder="4242 4242 4242 4242" />
                <small v-if="paymentErrors.cardNumber" class="field-error">{{ paymentErrors.cardNumber }}</small>
              </label>
              <label :class="{ 'has-field-error': paymentErrors.expiry }">
                Expiry date
                <input v-model.trim="paymentDetails.expiry" autocomplete="cc-exp" placeholder="12/28" />
                <small v-if="paymentErrors.expiry" class="field-error">{{ paymentErrors.expiry }}</small>
              </label>
              <label :class="{ 'has-field-error': paymentErrors.cvv }">
                CVV
                <input v-model.trim="paymentDetails.cvv" inputmode="numeric" autocomplete="cc-csc" placeholder="123" />
                <small v-if="paymentErrors.cvv" class="field-error">{{ paymentErrors.cvv }}</small>
              </label>
            </div>
            <label class="checkbox-field"><input v-model="paymentDetails.billingSameAsShipping" type="checkbox" /> Billing address is the same as shipping.</label>
            <label v-if="!paymentDetails.billingSameAsShipping" :class="['wide-field', { 'has-field-error': paymentErrors.billingAddress }]">
              Billing address
              <input v-model.trim="paymentDetails.billingAddress" autocomplete="billing street-address" placeholder="Billing address" />
              <small v-if="paymentErrors.billingAddress" class="field-error">{{ paymentErrors.billingAddress }}</small>
            </label>
          </div>
          <label class="checkbox-field"><input v-model="form.refundPolicy" type="checkbox" /> I understand this store has a 30-day refund request window.</label>
        </section>
      </div>
      <CartSummary :item-count="itemCount" :subtotal="subtotal" :discount-total="summaryDiscountTotal" :shipping="shipping">
        <div v-if="activePromotion" class="checkout-note compact-note">
          <strong>{{ activePromotion.code }} applied</strong>
          <span>{{ activePromotion.label }}</span>
        </div>
        <div class="checkout-note compact-note">
          <strong>{{ paymentMethodLabel(form.paymentMethod) }}</strong>
          <span>{{ paymentStatusForMethod(form.paymentMethod) === 'PAID' ? 'Demo payment confirmed at checkout.' : 'Payment will show as pending for the merchant.' }}</span>
        </div>
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
import {
  PAYMENT_METHODS,
  createDemoTracking,
  maskCardNumber,
  paymentMethodLabel,
  paymentStatusForMethod,
  saveStoreOrder
} from '../../utils/orderTracking'

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
  paymentMethod: 'CARD',
  refundPolicy: false
})
const paymentDetails = reactive({
  cardholderName: '',
  cardNumber: '',
  expiry: '',
  cvv: '',
  billingSameAsShipping: true,
  billingAddress: ''
})
const paymentErrors = reactive({})
const paymentMethods = PAYMENT_METHODS
const items = computed(() => cartStore.itemsForStore(props.store.slug))
const itemCount = computed(() => cartStore.itemCountForStore(props.store.slug))
const subtotal = computed(() => cartStore.subtotalForStore(props.store.slug))
const discountTotal = computed(() => cartStore.discountTotalForStore(props.store.slug))
const activePromotion = computed(() => cartStore.promotionForStore(props.store.slug))
const promotionDiscount = computed(() => cartStore.promotionDiscountForStore(props.store.slug, subtotal.value))
const summaryDiscountTotal = computed(() => discountTotal.value + promotionDiscount.value)
const subtotalAfterDiscounts = computed(() => Math.max(0, subtotal.value - summaryDiscountTotal.value))
const standardShipping = computed(() => {
  if (!items.value.length) return 0
  if (activePromotion.value?.type === 'free_shipping') return 0
  return subtotalAfterDiscounts.value >= 75 ? 0 : 6
})
const standardDeliveryLabel = computed(() => (standardShipping.value === 0 ? 'free' : '$6.00'))
const shipping = computed(() => {
  if (!items.value.length) return 0
  if (form.deliveryMethod === 'EXPRESS') return 14
  if (form.deliveryMethod === 'PICKUP') return 0
  return standardShipping.value
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
  if (!validatePayment()) {
    error.value = 'Review the highlighted demo payment fields before placing the order.'
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
    discountTotal: summaryDiscountTotal.value,
    shipping: shipping.value,
    total: subtotalAfterDiscounts.value + shipping.value,
    promotion: activePromotion.value,
    paymentMethod: form.paymentMethod,
    paymentMethodLabel: paymentMethodLabel(form.paymentMethod),
    paymentStatus: paymentStatusForMethod(form.paymentMethod),
    paymentDetails: paymentDetailsForOrder(),
    deliveryMethod: form.deliveryMethod,
    shippingStatus: 'Processing',
    fulfillmentStatus: 'Processing',
    createdAt: new Date().toISOString()
  }
  order.tracking = createDemoTracking(order, props.store)
  saveStoreOrder(order)
  cartStore.clearStoreCart(props.store.slug)
  router.push(`/store/${props.store.slug}/order-success?order=${orderId}`)
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value).trim())
}

function validatePayment() {
  Object.keys(paymentErrors).forEach((key) => delete paymentErrors[key])
  if (form.paymentMethod !== 'CARD') return true
  const digits = paymentDetails.cardNumber.replace(/\D/g, '')
  if (!paymentDetails.cardholderName) paymentErrors.cardholderName = 'Enter the cardholder name.'
  if (digits.length < 12 || digits.length > 19) paymentErrors.cardNumber = 'Enter a valid demo card number.'
  if (!/^(0[1-9]|1[0-2])\/?\d{2}$/.test(paymentDetails.expiry)) paymentErrors.expiry = 'Use MM/YY format.'
  if (!/^\d{3,4}$/.test(paymentDetails.cvv)) paymentErrors.cvv = 'Enter a 3 or 4 digit CVV.'
  if (!paymentDetails.billingSameAsShipping && !paymentDetails.billingAddress) {
    paymentErrors.billingAddress = 'Enter a billing address or use the shipping address.'
  }
  return Object.keys(paymentErrors).length === 0
}

function paymentDetailsForOrder() {
  if (form.paymentMethod !== 'CARD') return {}
  return {
    cardholderName: paymentDetails.cardholderName,
    cardLast4: maskCardNumber(paymentDetails.cardNumber),
    expiry: paymentDetails.expiry,
    billingSameAsShipping: paymentDetails.billingSameAsShipping,
    billingAddress: paymentDetails.billingSameAsShipping ? 'Same as shipping' : paymentDetails.billingAddress
  }
}
</script>
