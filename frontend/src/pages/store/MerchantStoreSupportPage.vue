<template>
  <section class="page-section merchant-care-page">
    <PageHeader
      eyebrow="Customer care"
      :title="`${store.name} support and refund requests`"
      description="Send a support message or open a demo refund request for a recent order from this merchant storefront."
    />

    <div class="merchant-care-layout">
      <form class="merchant-care-form" @submit.prevent="submitCareRequest">
        <div class="care-mode-tabs" role="tablist" aria-label="Care request type">
          <button type="button" :class="{ active: mode === 'support' }" @click="mode = 'support'">Support ticket</button>
          <button type="button" :class="{ active: mode === 'refund' }" @click="mode = 'refund'">Refund request</button>
        </div>

        <ErrorMessage v-if="error" :message="error" />
        <div v-if="successMessage" class="success-message" role="status">{{ successMessage }}</div>

        <section class="form-section">
          <h2>Order details</h2>
          <div class="form-grid">
            <label>Order number<input v-model.trim="form.orderNumber" placeholder="Order number" /></label>
            <label>Email<input v-model.trim="form.email" type="email" placeholder="customer@example.com" /></label>
            <label>Full name<input v-model.trim="form.customerName" placeholder="Customer name" /></label>
          </div>
        </section>

        <section class="form-section">
          <h2>{{ mode === 'refund' ? 'Refund reason' : 'Support message' }}</h2>
          <label v-if="mode === 'support'">
            Issue type
            <select v-model="form.issueType">
              <option value="SHIPPING_ISSUE">Shipping issue</option>
              <option value="PRODUCT_ISSUE">Product issue</option>
              <option value="PAYMENT_ISSUE">Payment issue</option>
              <option value="EXCHANGE_REQUEST">Exchange request</option>
              <option value="OTHER">Other</option>
            </select>
          </label>
          <label v-else>
            Reason
            <select v-model="form.refundReason">
              <option value="Size or fit issue">Size or fit issue</option>
              <option value="Product arrived damaged">Product arrived damaged</option>
              <option value="Changed my mind">Changed my mind</option>
              <option value="Wrong item received">Wrong item received</option>
              <option value="Other refund reason">Other refund reason</option>
            </select>
          </label>
          <label>
            Message
            <textarea v-model.trim="form.message" rows="6" maxlength="1200" placeholder="Share the details the merchant needs to help you."></textarea>
          </label>
        </section>

        <div class="care-action-row">
          <button class="primary-button" type="submit" :disabled="submitting">
            {{ submitting ? 'Submitting...' : mode === 'refund' ? 'Submit refund request' : 'Submit support ticket' }}
          </button>
          <RouterLink class="text-link" :to="`/store/${store.slug}`">Back to store</RouterLink>
        </div>
      </form>

      <aside class="merchant-care-policy">
        <p class="eyebrow">Care policy</p>
        <h2>What happens next</h2>
        <ul>
          <li>Support tickets are routed to the merchant care queue when the API is available.</li>
          <li>Generated local demo orders can still save refund requests in this browser.</li>
          <li>The demo refund window is 30 days from order creation.</li>
        </ul>
        <div v-if="order" class="summary-panel">
          <div class="summary-line"><span>Order</span><strong>{{ order.id }}</strong></div>
          <div class="summary-line"><span>Payment</span><strong>{{ order.paymentStatus }}</strong></div>
          <div class="summary-line"><span>Total</span><strong>{{ formatCurrency(order.total) }}</strong></div>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getApiError } from '../../api/client'
import { createRefundRequest, createSupportTicket } from '../../api/orders'
import ErrorMessage from '../../components/ErrorMessage.vue'
import PageHeader from '../../components/PageHeader.vue'
import { shouldUseLocalDemoFallback } from '../../utils/demoAdmin'
import { formatCurrency } from '../../utils/format'
import { saveLocalCareRequest } from '../../utils/orderTracking'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const mode = ref('support')
const submitting = ref(false)
const error = ref('')
const successMessage = ref('')
const form = reactive({
  orderNumber: '',
  email: '',
  customerName: '',
  issueType: 'SHIPPING_ISSUE',
  refundReason: 'Size or fit issue',
  message: ''
})
const order = computed(() => {
  const orderId = route.query.order || form.orderNumber
  if (!orderId) return null
  const rawOrder = localStorage.getItem(`novacart_order_${orderId}`)
  if (!rawOrder) return null
  try {
    return JSON.parse(rawOrder)
  } catch {
    return null
  }
})

watch(
  () => route.query.mode,
  (value) => {
    if (value === 'refund') mode.value = 'refund'
    if (value === 'support') mode.value = 'support'
  },
  { immediate: true }
)

watch(
  order,
  (value) => {
    if (!value) return
    form.orderNumber = value.id
    form.email = value.customer?.email || form.email
    form.customerName = value.customer?.name || form.customerName
  },
  { immediate: true }
)

async function submitCareRequest() {
  error.value = ''
  successMessage.value = ''
  if (!form.email || !form.customerName || !form.message) {
    error.value = 'Enter your name, email, and message before submitting.'
    return
  }
  if (mode.value === 'refund' && !form.orderNumber) {
    error.value = 'Enter an order number before requesting a refund.'
    return
  }
  submitting.value = true
  try {
    if (mode.value === 'refund') {
      await submitRefundRequest()
    } else {
      await submitSupportTicket()
    }
    form.message = ''
  } catch (requestError) {
    error.value = getApiError(requestError, 'The care request could not be submitted.')
  } finally {
    submitting.value = false
  }
}

async function submitSupportTicket() {
  const payload = {
    issueType: form.issueType,
    orderNumber: form.orderNumber || null,
    email: form.email,
    customerName: form.customerName,
    message: careMessage()
  }
  if (String(form.orderNumber).startsWith('local-')) {
    saveLocalCareRequest('support', {
      storeSlug: props.store.slug,
      status: 'OPEN',
      ...payload
    })
    successMessage.value = 'Demo support ticket saved for this generated storefront preview.'
    return
  }
  try {
    await createSupportTicket(payload)
    successMessage.value = 'Support ticket submitted to the merchant care queue.'
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) throw requestError
    saveLocalCareRequest('support', {
      storeSlug: props.store.slug,
      status: 'OPEN',
      ...payload
    })
    successMessage.value = 'Backend is offline, so this demo support ticket was saved in this browser.'
  }
}

async function submitRefundRequest() {
  if (String(form.orderNumber).startsWith('local-')) {
    saveLocalCareRequest('refund', {
      storeSlug: props.store.slug,
      orderNumber: form.orderNumber,
      email: form.email,
      customerName: form.customerName,
      reason: `${form.refundReason}: ${form.message}`
    })
    successMessage.value = 'Demo refund request saved for this generated storefront preview.'
    return
  }
  try {
    await createRefundRequest({
      orderNumber: form.orderNumber,
      email: form.email,
      reason: `${form.refundReason}: ${form.message}`
    })
    successMessage.value = 'Refund request submitted for merchant review.'
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) throw requestError
    saveLocalCareRequest('refund', {
      storeSlug: props.store.slug,
      orderNumber: form.orderNumber,
      email: form.email,
      customerName: form.customerName,
      reason: `${form.refundReason}: ${form.message}`
    })
    successMessage.value = 'Backend is offline, so this demo refund request was saved in this browser.'
  }
}

function careMessage() {
  return `[${props.store.name}] ${form.message}`
}
</script>
