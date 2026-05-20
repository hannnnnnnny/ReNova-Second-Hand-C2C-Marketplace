<template>
  <section class="page-section">
    <PageHeader
      eyebrow="Store cart"
      :title="`${store.name} cart`"
      description="Review products from this merchant before continuing to demo checkout."
    />
    <EmptyState v-if="!items.length" title="Your cart is empty" :message="`Browse ${store.name} and add products to checkout.`">
      <RouterLink class="primary-button" :to="`/store/${store.slug}/products`">Browse products</RouterLink>
    </EmptyState>
    <div v-else class="cart-layout">
      <div class="cart-items">
        <article v-for="item in items" :key="item.itemId || item.productId" class="cart-item fashion-cart-item">
          <img :src="item.imageUrl" :alt="item.name" loading="lazy" decoding="async" />
          <div class="cart-item-body">
            <h2>{{ item.name }}</h2>
            <p v-if="selectedOptionsLabel(item)" class="cart-variant-line">{{ selectedOptionsLabel(item) }}</p>
            <div class="price-stack">
              <strong>{{ formatCurrency(item.price) }}</strong>
              <span v-if="item.compareAtPrice">{{ formatCurrency(item.compareAtPrice) }}</span>
            </div>
            <div class="cart-item-controls">
              <QuantityStepper :model-value="item.quantity" :max="Math.max(item.stockQuantity, 1)" @update:model-value="cartStore.updateQuantity(store.slug, item.itemId || item.productId, $event)" />
              <button class="text-button danger" type="button" @click="cartStore.removeItem(store.slug, item.itemId || item.productId)">Remove</button>
            </div>
          </div>
        </article>
      </div>
      <div class="cart-sidebar-stack">
        <section class="summary-panel cart-promo-panel" aria-label="Discount code">
          <h2>Discount code</h2>
          <form class="discount-code-form" @submit.prevent="applyPromotion">
            <label>
              Promo code
              <input v-model.trim="discountCode" placeholder="WELCOME10" autocomplete="off" />
            </label>
            <button class="secondary-button" type="submit">Apply</button>
          </form>
          <p v-if="promotionMessage" :class="['promo-message', { success: activePromotion }]">{{ promotionMessage }}</p>
          <button v-if="activePromotion" class="text-button" type="button" @click="clearPromotion">Remove {{ activePromotion.code }}</button>
        </section>
        <section class="summary-panel shipping-estimate-panel" aria-label="Shipping estimate">
          <h2>Shipping estimate</h2>
          <div class="summary-line"><span>Standard</span><strong>{{ standardShippingLabel }}</strong></div>
          <div class="summary-line"><span>Express</span><strong>{{ formatCurrency(14) }}</strong></div>
          <p>Final delivery method is selected during demo checkout.</p>
        </section>
        <CartSummary
          :item-count="itemCount"
          :subtotal="subtotal"
          :discount-total="summaryDiscountTotal"
          :shipping="shipping"
        >
          <RouterLink class="primary-button" :to="`/store/${store.slug}/checkout`">Continue to checkout</RouterLink>
          <RouterLink class="text-link" :to="`/store/${store.slug}/products`">Keep shopping</RouterLink>
        </CartSummary>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import CartSummary from '../../components/CartSummary.vue'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import QuantityStepper from '../../components/QuantityStepper.vue'
import { useStorefrontCartStore } from '../../stores/storefrontCart'
import { formatCurrency } from '../../utils/format'

const props = defineProps({
  store: {
    type: Object,
    required: true
  }
})

const cartStore = useStorefrontCartStore()
const discountCode = ref('')
const promotionMessage = ref('')
const items = computed(() => cartStore.itemsForStore(props.store.slug))
const itemCount = computed(() => cartStore.itemCountForStore(props.store.slug))
const subtotal = computed(() => cartStore.subtotalForStore(props.store.slug))
const discountTotal = computed(() => cartStore.discountTotalForStore(props.store.slug))
const activePromotion = computed(() => cartStore.promotionForStore(props.store.slug))
const promotionDiscount = computed(() => cartStore.promotionDiscountForStore(props.store.slug, subtotal.value))
const summaryDiscountTotal = computed(() => discountTotal.value + promotionDiscount.value)
const subtotalAfterDiscounts = computed(() => Math.max(0, subtotal.value - summaryDiscountTotal.value))
const shipping = computed(() => {
  if (!items.value.length) return 0
  if (activePromotion.value?.type === 'free_shipping') return 0
  return subtotalAfterDiscounts.value >= 75 ? 0 : 6
})
const standardShippingLabel = computed(() => (shipping.value === 0 ? 'Free' : formatCurrency(6)))

function selectedOptionsLabel(item) {
  return [item.options?.size ? `Size ${item.options.size}` : '', item.options?.color ? `Color ${item.options.color}` : '']
    .filter(Boolean)
    .join(' / ')
}

function applyPromotion() {
  const result = cartStore.applyPromotion(props.store.slug, discountCode.value)
  promotionMessage.value = result.message
  if (result.applied) discountCode.value = ''
}

function clearPromotion() {
  cartStore.clearPromotion(props.store.slug)
  promotionMessage.value = 'Discount code removed.'
}
</script>
