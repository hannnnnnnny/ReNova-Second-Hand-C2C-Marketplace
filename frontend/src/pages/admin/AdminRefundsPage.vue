<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Customer Care"
      title="Refund Requests"
      description="Review refund requests, approve or reject them, and keep payment/refund status synchronized."
    />
    <section class="care-queue-summary" aria-label="Refund queue summary">
      <article v-for="item in refundSummary" :key="item.label">
        <span>{{ item.label }}</span>
        <strong>{{ item.count }}</strong>
      </article>
    </section>
    <div class="admin-toolbar">
      <label>Status
        <select v-model="statusFilter" @change="loadRefunds">
          <option value="">All statuses</option>
          <option value="REQUESTED">Requested</option>
          <option value="UNDER_REVIEW">Under review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="REFUNDED">Refunded</option>
        </select>
      </label>
      <button class="secondary-button" type="button" @click="loadRefunds">Refresh</button>
    </div>
    <LoadingState v-if="loading" message="Loading refund requests..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState v-else-if="!refunds.length" title="No refund requests" message="Eligible customer refund requests will appear here." />
    <div v-else class="admin-table-wrap">
      <table class="admin-table">
        <thead><tr><th>Order</th><th>Customer</th><th>Reason</th><th>Status</th><th>Internal Notes</th><th>Update</th></tr></thead>
        <tbody>
          <tr v-for="refund in refunds" :key="refund.id">
            <td><RouterLink class="text-link" :to="`/admin/orders/${refund.orderId}`">{{ refund.orderNumber }}</RouterLink></td>
            <td><strong>{{ refund.customerName }}</strong><span>{{ refund.email }}</span></td>
            <td>{{ refund.reason }}</td>
            <td><StatusBadge :value="refund.status" /></td>
            <td class="table-notes-field">
              <textarea v-model.trim="refund.internalNotesDraft" rows="3" maxlength="1200" placeholder="Add merchant-only refund review notes."></textarea>
            </td>
            <td>
              <div class="table-actions">
                <select v-model="refund.nextStatus">
                  <option value="REQUESTED">Requested</option>
                  <option value="UNDER_REVIEW">Under review</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="REFUNDED">Refunded</option>
                </select>
                <button class="secondary-button compact-button" type="button" :disabled="savingId === refund.id || !refundChanged(refund)" @click="updateRefund(refund)">
                  {{ savingId === refund.id ? 'Saving...' : 'Save' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <ToastMessage :message="toastMessage" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAdminRefunds, updateAdminRefund } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useAuthStore } from '../../stores/auth'
import { usePlatformStore } from '../../stores/platform'
import { isLocalDemoAdminSession, localRefundsForStore, shouldUseLocalDemoFallback, updateLocalRefund } from '../../utils/demoAdmin'

const platformStore = usePlatformStore()
const authStore = useAuthStore()
const loading = ref(true)
const error = ref('')
const statusFilter = ref('')
const refunds = ref([])
const savingId = ref(null)
const toastMessage = ref('')
let toastTimer
const refundSummary = computed(() => {
  const statuses = ['REQUESTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'REFUNDED']
  return statuses.map((status) => ({
    label: status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()),
    count: refunds.value.filter((refund) => refund.status === status).length
  }))
})

onMounted(loadRefunds)

async function loadRefunds() {
  platformStore.loadPlatform()
  loading.value = true
  error.value = ''
  if (isLocalDemoAdminSession(authStore)) {
    refunds.value = localRefundsForStore(platformStore.currentStore.slug, statusFilter.value).map(prepareRefund)
    loading.value = false
    return
  }
  try {
    const params = statusFilter.value ? { status: statusFilter.value } : {}
    refunds.value = (await fetchAdminRefunds(params)).map(prepareRefund)
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) {
      error.value = getApiError(requestError, 'Refund requests could not be loaded.')
    } else {
      refunds.value = localRefundsForStore(platformStore.currentStore.slug, statusFilter.value).map(prepareRefund)
      error.value = ''
    }
  } finally {
    loading.value = false
  }
}

function prepareRefund(refund) {
  return {
    ...refund,
    nextStatus: refund.status,
    internalNotesDraft: refund.internalNotes || ''
  }
}

async function updateRefund(refund) {
  savingId.value = refund.id
  try {
    const updated = refund.localDemo
      ? updateLocalRefund(refund.id, {
        status: refund.nextStatus,
        internalNotes: refund.internalNotesDraft || null
      })
      : await updateAdminRefund(refund.id, {
        status: refund.nextStatus,
        internalNotes: refund.internalNotesDraft || null
      })
    if (!updated) throw new Error('Refund request could not be updated.')
    Object.assign(refund, updated, {
      nextStatus: updated.status,
      internalNotesDraft: updated.internalNotes || ''
    })
    showToast('Refund request updated.')
  } catch (requestError) {
    error.value = getApiError(requestError, 'Refund request could not be updated.')
  } finally {
    savingId.value = null
  }
}

function refundChanged(refund) {
  return refund.nextStatus !== refund.status || (refund.internalNotesDraft || '') !== (refund.internalNotes || '')
}

function showToast(message) {
  toastMessage.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
