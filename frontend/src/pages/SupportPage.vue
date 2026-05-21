<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Customer Service"
      title="Support Request"
      description="Send a refund, exchange, shipping, product, payment, or general service issue to the merchant team."
    />
    <section class="care-hero-panel">
      <div>
        <p class="eyebrow">Fashion care desk</p>
        <h2>Ask about fit, delivery, payment, refund eligibility, or product condition.</h2>
      </div>
      <p>Submitted tickets appear in the merchant support queue with status controls and internal notes.</p>
    </section>
    <form class="support-layout" @submit.prevent="submitTicket">
      <div class="admin-form">
        <ErrorMessage v-if="error" :message="error" />
        <label>
          Issue Type
          <select v-model="form.issueType" required>
            <option value="REFUND_REQUEST">Refund request</option>
            <option value="EXCHANGE_REQUEST">Exchange request</option>
            <option value="SHIPPING_ISSUE">Shipping issue</option>
            <option value="PRODUCT_ISSUE">Product issue</option>
            <option value="PAYMENT_ISSUE">Payment issue</option>
            <option value="OTHER">Other</option>
          </select>
        </label>
        <label>
          Order Number
          <input v-model.trim="form.orderNumber" maxlength="32" placeholder="NC-20260516-0001" />
        </label>
        <label>
          Name
          <input v-model.trim="form.customerName" required maxlength="140" placeholder="Morgan Lee" />
        </label>
        <label>
          Email
          <input v-model.trim="form.email" required type="email" maxlength="180" placeholder="morgan@example.com" />
        </label>
        <label class="wide-field">
          Message
          <textarea v-model.trim="form.message" required maxlength="2000" rows="7" placeholder="Tell the merchant team what happened and what outcome you need."></textarea>
        </label>
      </div>
      <aside class="summary-panel order-summary-card">
        <h2>Support Status</h2>
        <p class="muted">Submitted tickets enter Open status and can be reviewed from the admin support queue.</p>
        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? 'Submitting...' : 'Submit Ticket' }}
        </button>
        <RouterLink class="text-link" :to="{ name: 'refund-request', query: { orderNumber: form.orderNumber, email: form.email } }">
          Start a refund request
        </RouterLink>
        <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
      </aside>
    </form>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createSupportTicket } from '../api/orders'
import { getApiError } from '../api/client'
import ErrorMessage from '../components/ErrorMessage.vue'
import PageHeader from '../components/PageHeader.vue'

const route = useRoute()
const submitting = ref(false)
const error = ref('')
const successMessage = ref('')
const form = reactive({
  issueType: 'OTHER',
  orderNumber: route.query.orderNumber ? String(route.query.orderNumber) : '',
  customerName: '',
  email: route.query.email ? String(route.query.email) : '',
  message: ''
})
const formIsValid = computed(() => {
  return form.issueType && form.customerName && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) && form.message
})

async function submitTicket() {
  if (submitting.value) return
  error.value = ''
  successMessage.value = ''
  if (!formIsValid.value) {
    error.value = 'Complete the support form before submitting.'
    return
  }
  submitting.value = true
  try {
    const ticket = await createSupportTicket({ ...form, orderNumber: form.orderNumber || null })
    successMessage.value = `Support ticket #${ticket.id} was submitted.`
    form.message = ''
  } catch (requestError) {
    error.value = getApiError(requestError, 'Support ticket could not be submitted.')
  } finally {
    submitting.value = false
  }
}
</script>
