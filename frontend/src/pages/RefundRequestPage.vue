<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Customer Service"
      title="Refund Request"
      description="Submit an eligible paid order for merchant review within the demo 30-day refund window."
    />
    <section class="care-hero-panel refund-care-panel">
      <div>
        <p class="eyebrow">Refund review</p>
        <h2>Connect the order number and email so the merchant can verify the payment and fulfillment state.</h2>
      </div>
      <p>Approved requests can update refund status and keep the order record visible to admin teams.</p>
    </section>
    <form class="support-layout" @submit.prevent="submitRefund">
      <div class="admin-form">
        <ErrorMessage v-if="error" :message="error" />
        <label>
          Order Number
          <input v-model.trim="form.orderNumber" required maxlength="32" placeholder="NC-20260516-0001" />
        </label>
        <label>
          Email
          <input v-model.trim="form.email" required type="email" maxlength="180" placeholder="morgan@example.com" />
        </label>
        <label class="wide-field">
          Refund Reason
          <textarea v-model.trim="form.reason" required maxlength="1200" rows="7" placeholder="Share the item issue, fit concern, shipping problem, or other reason for review."></textarea>
        </label>
      </div>
      <aside class="summary-panel order-summary-card">
        <h2>Refund Window</h2>
        <p class="muted">Refund requests are stored with Requested status. Admin users can move them under review, approve, reject, or mark refunded.</p>
        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? 'Submitting...' : 'Submit Refund Request' }}
        </button>
        <RouterLink class="text-link" :to="{ name: 'support', query: { orderNumber: form.orderNumber, email: form.email } }">
          Contact support instead
        </RouterLink>
        <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
      </aside>
    </form>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createRefundRequest } from '../api/orders'
import { getApiError } from '../api/client'
import ErrorMessage from '../components/ErrorMessage.vue'
import PageHeader from '../components/PageHeader.vue'

const route = useRoute()
const submitting = ref(false)
const error = ref('')
const successMessage = ref('')
const form = reactive({
  orderNumber: route.query.orderNumber ? String(route.query.orderNumber) : '',
  email: route.query.email ? String(route.query.email) : '',
  reason: ''
})
const formIsValid = computed(() => {
  return form.orderNumber && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) && form.reason
})

async function submitRefund() {
  if (submitting.value) return
  error.value = ''
  successMessage.value = ''
  if (!formIsValid.value) {
    error.value = 'Complete the refund request before submitting.'
    return
  }
  submitting.value = true
  try {
    const refund = await createRefundRequest(form)
    successMessage.value = `Refund request #${refund.id} is now ${refund.status}.`
    form.reason = ''
  } catch (requestError) {
    error.value = getApiError(requestError, 'Refund request could not be submitted.')
  } finally {
    submitting.value = false
  }
}
</script>
