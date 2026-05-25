<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { listingApi, orderApi, offerApi } from '../api/endpoints'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatPrice } from '../utils/format'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const listing = ref(null)
const acceptedOffer = ref(null)
const submitting = ref(false)

const form = ref({
  shippingName: auth.user?.displayName || '',
  shippingPhone: '',
  shippingAddress: '',
  buyerNote: ''
})

const offerId = computed(() => route.query.offerId ? Number(route.query.offerId) : null)

onMounted(async () => {
  try {
    listing.value = await listingApi.get(route.params.listingId)
    if (offerId.value) {
      const sent = await offerApi.sent({ page: 0, size: 100 })
      acceptedOffer.value = (sent.content || []).find((o) => o.id === offerId.value && o.status === 'ACCEPTED')
    }
  } catch (err) { toast.error(apiError(err)); router.back() }
})

const price = computed(() => acceptedOffer.value ? Number(acceptedOffer.value.amount) : Number(listing.value?.price || 0))
const shipping = computed(() => Number(listing.value?.shippingFee || 0))
const total = computed(() => price.value + shipping.value)

async function placeOrder() {
  submitting.value = true
  try {
    const order = await orderApi.create({
      listingId: listing.value.id,
      acceptedOfferId: acceptedOffer.value?.id || null,
      shippingName: form.value.shippingName,
      shippingPhone: form.value.shippingPhone,
      shippingAddress: form.value.shippingAddress,
      buyerNote: form.value.buyerNote
    })
    toast.success(t('checkout.orderCreated'))
    router.push({ name: 'order-detail', params: { id: order.id } })
  } catch (err) { toast.error(apiError(err)) } finally { submitting.value = false }
}
</script>

<template>
  <main class="page">
    <div class="container" v-if="listing">
      <h1 style="margin-bottom: 24px">{{ t('checkout.title') }}</h1>
      <div class="grid grid-2">
        <form class="panel" @submit.prevent="placeOrder">
          <h3 style="margin-bottom: 16px">{{ t('checkout.shippingTo') }}</h3>
          <div class="field">
            <label class="label">{{ t('checkout.name') }}</label>
            <input class="input" v-model="form.shippingName" required maxlength="80" />
          </div>
          <div class="field">
            <label class="label">{{ t('checkout.phone') }}</label>
            <input class="input" v-model="form.shippingPhone" required maxlength="40" />
          </div>
          <div class="field">
            <label class="label">{{ t('checkout.address') }}</label>
            <textarea class="textarea" v-model="form.shippingAddress" required maxlength="400"></textarea>
          </div>
          <div class="field">
            <label class="label">{{ t('checkout.note') }}</label>
            <textarea class="textarea" v-model="form.buyerNote" maxlength="500"></textarea>
          </div>
          <button class="btn btn-primary btn-lg btn-block" :disabled="submitting" type="submit">{{ submitting ? t('common.saving') : t('checkout.payNow') }}</button>
        </form>

        <aside class="panel" style="height: fit-content">
          <h3 style="margin-bottom: 16px">{{ t('checkout.orderSummary') }}</h3>
          <div class="row" style="margin-bottom: 16px">
            <div class="thumb" style="width:72px;height:72px;border-radius:12px;background:var(--bg-muted) center/cover no-repeat;" :style="{ backgroundImage: `url('${listing.imageUrls?.[0]}')` }"></div>
            <div>
              <div class="bold">{{ listing.title }}</div>
              <div class="soft">{{ t(`condition.${listing.condition}`) }}</div>
              <div v-if="acceptedOffer" class="soft">{{ t('offerStatus.ACCEPTED') }}: {{ formatPrice(acceptedOffer.amount) }}</div>
            </div>
          </div>
          <div class="between"><span class="muted">{{ t('common.price') }}</span><span>{{ formatPrice(price) }}</span></div>
          <div class="between"><span class="muted">{{ t('common.shipping') }}</span><span>{{ shipping > 0 ? formatPrice(shipping) : t('common.free') }}</span></div>
          <div class="divider"></div>
          <div class="between"><span class="bold">{{ t('common.total') }}</span><span class="bold" style="font-family:var(--font-display); font-size:22px">{{ formatPrice(total) }}</span></div>
          <div class="soft" style="margin-top: 16px; font-size: 12px">{{ t('orders.escrowHint') }}</div>
        </aside>
      </div>
    </div>
  </main>
</template>
