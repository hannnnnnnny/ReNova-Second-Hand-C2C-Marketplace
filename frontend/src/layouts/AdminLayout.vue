<template>
  <div class="admin-shell">
    <a class="skip-link" href="#admin-content">Skip to admin content</a>
    <AdminSidebar />
    <div class="admin-workspace">
      <AdminTopbar :notification-count="notificationCount" />
      <main id="admin-content" class="admin-main" tabindex="-1">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAdminRefunds, fetchAdminSupportTickets } from '../api/admin'
import AdminSidebar from '../components/AdminSidebar.vue'
import AdminTopbar from '../components/AdminTopbar.vue'
import { usePlatformStore } from '../stores/platform'

const platformStore = usePlatformStore()
const supportTickets = ref([])
const refunds = ref([])
const notificationCount = computed(() => {
  const openSupport = supportTickets.value.filter((ticket) => !['RESOLVED', 'CLOSED'].includes(ticket.status)).length
  const pendingRefunds = refunds.value.filter((refund) => ['REQUESTED', 'UNDER_REVIEW'].includes(refund.status)).length
  return openSupport + pendingRefunds
})

onMounted(() => {
  platformStore.loadPlatform()
  loadCareNotifications()
})

async function loadCareNotifications() {
  try {
    const [supportData, refundData] = await Promise.all([
      fetchAdminSupportTickets(),
      fetchAdminRefunds()
    ])
    supportTickets.value = supportData
    refunds.value = refundData
  } catch {
    supportTickets.value = []
    refunds.value = []
  }
}
</script>
