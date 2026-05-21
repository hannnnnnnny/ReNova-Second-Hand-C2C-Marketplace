<template>
  <section class="admin-page">
    <PageHeader
      eyebrow="Customer Care"
      title="Support Tickets"
      description="Review customer service issues and move tickets through the support workflow."
    />
    <section class="care-queue-summary" aria-label="Support queue summary">
      <article v-for="item in ticketSummary" :key="item.label">
        <span>{{ item.label }}</span>
        <strong>{{ item.count }}</strong>
      </article>
    </section>
    <div class="admin-toolbar">
      <label>Status
        <select v-model="statusFilter">
          <option value="">All statuses</option>
          <option value="OPEN">Open</option>
          <option value="IN_REVIEW">In review</option>
          <option value="WAITING_FOR_CUSTOMER">Waiting for customer</option>
          <option value="RESOLVED">Resolved</option>
          <option value="CLOSED">Closed</option>
        </select>
      </label>
      <button class="secondary-button" type="button" @click="loadTickets">Refresh</button>
    </div>
    <LoadingState v-if="loading" message="Loading support tickets..." />
    <ErrorMessage v-else-if="error" :message="error" />
    <EmptyState v-else-if="!filteredTickets.length" title="No support tickets" message="Customer support requests will appear here, or adjust the current status filter." />
    <div v-else class="admin-table-wrap">
      <table class="admin-table">
        <thead><tr><th>Customer</th><th>Issue</th><th>Message</th><th>Status</th><th>Internal Notes</th><th>Update</th></tr></thead>
        <tbody>
          <tr v-for="ticket in filteredTickets" :key="ticket.id">
            <td><strong>{{ ticket.customerName }}</strong><span>{{ ticket.email }} {{ ticket.orderNumber || '' }}</span></td>
            <td>{{ formatStatus(ticket.issueType) }}</td>
            <td>{{ ticket.message }}</td>
            <td><StatusBadge :value="ticket.status" /></td>
            <td class="table-notes-field">
              <textarea v-model.trim="ticket.internalNotesDraft" rows="3" maxlength="1200" placeholder="Add merchant-only resolution notes."></textarea>
            </td>
            <td>
              <div class="table-actions">
                <select v-model="ticket.nextStatus">
                  <option value="OPEN">Open</option>
                  <option value="IN_REVIEW">In review</option>
                  <option value="WAITING_FOR_CUSTOMER">Waiting for customer</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="CLOSED">Closed</option>
                </select>
                <button class="secondary-button compact-button" type="button" :disabled="savingId === ticket.id || !ticketChanged(ticket)" @click="updateTicket(ticket)">
                  {{ savingId === ticket.id ? 'Saving...' : 'Save' }}
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
import { fetchAdminSupportTickets, updateAdminSupportTicket } from '../../api/admin'
import { getApiError } from '../../api/client'
import EmptyState from '../../components/EmptyState.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import ToastMessage from '../../components/ToastMessage.vue'
import { useAuthStore } from '../../stores/auth'
import { usePlatformStore } from '../../stores/platform'
import { isLocalDemoAdminSession, localSupportTicketsForStore, shouldUseLocalDemoFallback, updateLocalSupportTicket } from '../../utils/demoAdmin'
import { formatStatus } from '../../utils/format'

const platformStore = usePlatformStore()
const authStore = useAuthStore()
const loading = ref(true)
const error = ref('')
const tickets = ref([])
const statusFilter = ref('')
const savingId = ref(null)
const toastMessage = ref('')
let toastTimer
const filteredTickets = computed(() => {
  return statusFilter.value
    ? tickets.value.filter((ticket) => ticket.status === statusFilter.value)
    : tickets.value
})
const ticketSummary = computed(() => {
  const statuses = ['OPEN', 'IN_REVIEW', 'WAITING_FOR_CUSTOMER', 'RESOLVED', 'CLOSED']
  return statuses.map((status) => ({
    label: formatStatus(status),
    count: tickets.value.filter((ticket) => ticket.status === status).length
  }))
})

onMounted(loadTickets)

async function loadTickets() {
  platformStore.loadPlatform()
  loading.value = true
  error.value = ''
  if (isLocalDemoAdminSession(authStore)) {
    tickets.value = localSupportTicketsForStore(platformStore.currentStore.slug).map(prepareTicket)
    loading.value = false
    return
  }
  try {
    tickets.value = (await fetchAdminSupportTickets()).map(prepareTicket)
  } catch (requestError) {
    if (!shouldUseLocalDemoFallback(requestError)) {
      error.value = getApiError(requestError, 'Support tickets could not be loaded.')
    } else {
      tickets.value = localSupportTicketsForStore(platformStore.currentStore.slug).map(prepareTicket)
      error.value = ''
    }
  } finally {
    loading.value = false
  }
}

function prepareTicket(ticket) {
  return {
    ...ticket,
    nextStatus: ticket.status,
    internalNotesDraft: ticket.internalNotes || ''
  }
}

async function updateTicket(ticket) {
  savingId.value = ticket.id
  try {
    const updated = ticket.localDemo
      ? updateLocalSupportTicket(ticket.id, {
        status: ticket.nextStatus,
        internalNotes: ticket.internalNotesDraft || null
      })
      : await updateAdminSupportTicket(ticket.id, {
        status: ticket.nextStatus,
        internalNotes: ticket.internalNotesDraft || null
      })
    if (!updated) throw new Error('Support ticket could not be updated.')
    Object.assign(ticket, updated, {
      nextStatus: updated.status,
      internalNotesDraft: updated.internalNotes || ''
    })
    showToast('Support ticket updated.')
  } catch (requestError) {
    error.value = getApiError(requestError, 'Support ticket could not be updated.')
  } finally {
    savingId.value = null
  }
}

function ticketChanged(ticket) {
  return ticket.nextStatus !== ticket.status || (ticket.internalNotesDraft || '') !== (ticket.internalNotes || '')
}

function showToast(message) {
  toastMessage.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 2400)
}
</script>
