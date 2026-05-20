<template>
  <main v-if="store" class="template-detail-page" :style="{ '--store-accent': store.brandColor }">
    <RouterLink class="text-link template-back-link" to="/templates">Back to templates</RouterLink>

    <section class="template-detail-hero">
      <div>
        <p class="eyebrow">{{ template.name }} preview</p>
        <h1>{{ store.name }} page system</h1>
        <p>{{ store.description }}</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" :to="`/store/${store.slug}`">Open live storefront</RouterLink>
          <RouterLink class="secondary-button" :to="{ path: '/onboarding', query: { template: template.id } }">Use this template</RouterLink>
        </div>
      </div>
      <aside class="template-detail-summary" aria-label="Template summary">
        <span>{{ store.logoText }}</span>
        <strong>{{ store.name }}</strong>
        <p>{{ store.heroTitle }}</p>
        <small>{{ store.shippingMessage }} / {{ store.products.length }} demo products</small>
      </aside>
    </section>

    <section class="template-page-preview-section">
      <div class="platform-section-heading">
        <p class="eyebrow">Page previews</p>
        <h2>See the customer journey before opening the live route</h2>
      </div>
      <div class="template-page-preview-grid">
        <RouterLink
          v-for="page in pagePreviews"
          :key="page.key"
          class="template-page-preview-card"
          :to="page.to"
          :aria-label="`Open ${page.title} for ${store.name}`"
        >
          <div class="preview-card-topline">
            <span>{{ page.badge }}</span>
            <small>{{ page.path }}</small>
          </div>
          <component :is="page.component" :store="store" :product="primaryProduct" />
          <div class="preview-card-copy">
            <h3>{{ page.title }}</h3>
            <p>{{ page.description }}</p>
          </div>
        </RouterLink>
      </div>
    </section>
  </main>
  <main v-else class="platform-page-hero">
    <p class="eyebrow">Template not found</p>
    <h1>This preview is not available</h1>
    <p>Choose another storefront preview from the template gallery.</p>
    <RouterLink class="primary-button" to="/templates">Back to templates</RouterLink>
  </main>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute } from 'vue-router'
import { demoStores, getTemplateById } from '../../data/platform'
import { formatCurrency } from '../../utils/format'

const route = useRoute()

const store = computed(() => {
  const key = String(route.params.templateId || '')
  return demoStores.find((entry) => entry.slug === key || entry.template === key)
})
const template = computed(() => getTemplateById(store.value?.template || 'fashion'))
const primaryProduct = computed(() => store.value?.products?.[0] || null)
const pagePreviews = computed(() => {
  if (!store.value) return []
  const productPath = primaryProduct.value ? `/store/${store.value.slug}/products/${primaryProduct.value.id}` : `/store/${store.value.slug}/products`
  return [
    {
      key: 'home',
      title: 'Store home',
      badge: 'Homepage',
      path: `/store/${store.value.slug}`,
      to: `/store/${store.value.slug}`,
      description: 'Brand story, hero merchandising, category entry points, and featured products.',
      component: HomePreview
    },
    {
      key: 'catalog',
      title: 'Product listing',
      badge: 'Browse',
      path: `/store/${store.value.slug}/products`,
      to: `/store/${store.value.slug}/products`,
      description: 'Search, category filtering, product cards, quick add, ratings, and stock states.',
      component: CatalogPreview
    },
    {
      key: 'detail',
      title: 'Product detail',
      badge: 'PDP',
      path: productPath,
      to: productPath,
      description: 'Variant choices, price proof, delivery copy, reviews, and related products.',
      component: ProductPreview
    },
    {
      key: 'cart',
      title: 'Cart',
      badge: 'Basket',
      path: `/store/${store.value.slug}/cart`,
      to: `/store/${store.value.slug}/cart`,
      description: 'Quantity editing, item removal, discounts, shipping estimate, and checkout CTA.',
      component: CartPreview
    },
    {
      key: 'checkout',
      title: 'Checkout',
      badge: 'Payment',
      path: `/store/${store.value.slug}/checkout`,
      to: `/store/${store.value.slug}/checkout`,
      description: 'Demo-safe payment methods, address validation, delivery options, and order review.',
      component: CheckoutPreview
    },
    {
      key: 'orders',
      title: 'Orders and tracking',
      badge: 'Logistics',
      path: `/store/${store.value.slug}/orders`,
      to: `/store/${store.value.slug}/orders`,
      description: 'Fulfillment status, payment status, tracking timeline, carrier, and reorder paths.',
      component: TrackingPreview
    },
    {
      key: 'support',
      title: 'Support and refunds',
      badge: 'Care',
      path: `/store/${store.value.slug}/support`,
      to: `/store/${store.value.slug}/support`,
      description: 'Support tickets, refund request copy, policy reassurance, and customer care states.',
      component: SupportPreview
    },
    {
      key: 'admin',
      title: 'Merchant operations',
      badge: 'Admin',
      path: '/admin/dashboard',
      to: '/admin/dashboard',
      description: 'Dashboard, inventory, orders, promotions, analytics, support, refunds, and setup tasks.',
      component: AdminPreview
    }
  ]
})

function previewFrame(className, children) {
  return h('div', { class: ['mini-page-frame', className] }, children)
}

const HomePreview = {
  props: ['store'],
  setup(props) {
    return () => previewFrame('mini-home-preview', [
      h('div', { class: 'mini-nav' }, [
        h('span', props.store.logoText),
        h('i'),
        h('i'),
        h('i')
      ]),
      h('div', { class: 'mini-hero-split' }, [
        h('div', [
          h('small', props.store.category),
          h('strong', props.store.heroTitle),
          h('em', props.store.shippingMessage)
        ]),
        h('img', { src: props.store.heroImage, alt: '' })
      ]),
      h('div', { class: 'mini-chip-row' }, props.store.categories.slice(0, 4).map((category) => h('span', category)))
    ])
  }
}

const CatalogPreview = {
  props: ['store'],
  setup(props) {
    return () => previewFrame('mini-catalog-preview', [
      h('div', { class: 'mini-filter-row' }, [
        h('span', 'Search'),
        h('span', 'Category'),
        h('span', 'Sort')
      ]),
      h('div', { class: 'mini-product-grid' }, props.store.products.slice(0, 4).map((product) => (
        h('article', [
          h('img', { src: product.imageUrl, alt: '' }),
          h('strong', product.name),
          h('small', formatCurrency(product.price))
        ])
      )))
    ])
  }
}

const ProductPreview = {
  props: ['product'],
  setup(props) {
    return () => previewFrame('mini-product-preview', [
      h('img', { src: props.product?.imageUrl, alt: '' }),
      h('div', [
        h('small', props.product?.category || 'Product'),
        h('strong', props.product?.name || 'Featured product'),
        h('p', props.product?.description || ''),
        h('div', { class: 'mini-option-row' }, ['XS', 'S', 'M', 'L'].map((size) => h('span', size)))
      ])
    ])
  }
}

const CartPreview = {
  props: ['store'],
  setup(props) {
    return () => previewFrame('mini-cart-preview', [
      h('div', { class: 'mini-cart-lines' }, props.store.products.slice(0, 3).map((product, index) => (
        h('div', [
          h('img', { src: product.imageUrl, alt: '' }),
          h('span', product.name),
          h('strong', index === 0 ? 'x2' : 'x1')
        ])
      ))),
      h('div', { class: 'mini-summary-box' }, [
        h('span', 'Subtotal'),
        h('strong', formatCurrency(props.store.products.slice(0, 2).reduce((sum, product) => sum + product.price, 0)))
      ])
    ])
  }
}

const CheckoutPreview = {
  setup() {
    return () => previewFrame('mini-checkout-preview', [
      h('div', { class: 'mini-form-stack' }, [
        h('span', 'Email'),
        h('span', 'Shipping address'),
        h('span', 'Delivery option')
      ]),
      h('div', { class: 'mini-payment-panel' }, [
        h('strong', 'Demo payment'),
        h('span', 'Card'),
        h('span', 'PayPal'),
        h('span', 'Pay later')
      ])
    ])
  }
}

const TrackingPreview = {
  setup() {
    return () => previewFrame('mini-tracking-preview', [
      h('ol', ['Placed', 'Paid', 'Packed', 'Shipped', 'Delivered'].map((step, index) => (
        h('li', { class: index < 3 ? 'complete' : '' }, [
          h('span'),
          h('strong', step)
        ])
      ))),
      h('div', { class: 'mini-carrier-box' }, [
        h('span', 'Carrier'),
        h('strong', 'Northstar Express')
      ])
    ])
  }
}

const SupportPreview = {
  setup() {
    return () => previewFrame('mini-support-preview', [
      h('div', { class: 'mini-ticket-form' }, [
        h('span', 'Topic'),
        h('span', 'Order number'),
        h('span', 'Message')
      ]),
      h('div', { class: 'mini-policy-row' }, [
        h('strong', '30-day refund window'),
        h('small', 'Support ticket saved in merchant queue')
      ])
    ])
  }
}

const AdminPreview = {
  setup() {
    return () => previewFrame('mini-admin-preview', [
      h('div', { class: 'mini-admin-sidebar' }, [
        h('span'),
        h('span'),
        h('span'),
        h('span')
      ]),
      h('div', { class: 'mini-admin-main' }, [
        h('div', { class: 'mini-admin-kpis' }, [
          h('strong', '$18.4k'),
          h('strong', '214'),
          h('strong', '3.8%')
        ]),
        h('div', { class: 'mini-admin-chart' }),
        h('div', { class: 'mini-admin-table' })
      ])
    ])
  }
}
</script>
