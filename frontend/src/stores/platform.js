import { defineStore } from 'pinia'
import { createSlug, demoStores, getTemplateById, storeTemplates } from '../data/platform'
import { publicAsset } from '../utils/publicPath'

const STORAGE_KEY = 'novacart_platform_stores'
const OVERRIDE_KEY = 'novacart_platform_store_overrides'
const CURRENT_STORE_KEY = 'novacart_current_store'

export const usePlatformStore = defineStore('platform', {
  state: () => ({
    merchantStores: [],
    storeOverrides: {},
    currentStoreSlug: 'demo-fashion'
  }),
  getters: {
    stores: (state) => [
      ...demoStores.map((store) => ({
        ...store,
        ...(state.storeOverrides[store.slug] || {}),
        setup: {
          ...store.setup,
          ...(state.storeOverrides[store.slug]?.setup || {})
        }
      })),
      ...state.merchantStores
    ],
    currentStore() {
      return this.stores.find((store) => store.slug === this.currentStoreSlug) || this.stores[0]
    },
    templates: () => storeTemplates
  },
  actions: {
    loadPlatform() {
      const storedCurrent = localStorage.getItem(CURRENT_STORE_KEY)
      if (storedCurrent) this.currentStoreSlug = storedCurrent

      const rawStores = localStorage.getItem(STORAGE_KEY)
      if (rawStores) {
        try {
          const parsedStores = JSON.parse(rawStores)
          this.merchantStores = Array.isArray(parsedStores) ? parsedStores.map(normalizeStore).filter(Boolean) : []
        } catch {
          localStorage.removeItem(STORAGE_KEY)
        }
      }

      const rawOverrides = localStorage.getItem(OVERRIDE_KEY)
      if (rawOverrides) {
        try {
          const parsedOverrides = JSON.parse(rawOverrides)
          this.storeOverrides = parsedOverrides && typeof parsedOverrides === 'object' ? parsedOverrides : {}
        } catch {
          localStorage.removeItem(OVERRIDE_KEY)
        }
      }
    },
    setCurrentStore(slug) {
      const nextStore = this.stores.find((store) => store.slug === slug)
      if (!nextStore) return
      this.currentStoreSlug = nextStore.slug
      localStorage.setItem(CURRENT_STORE_KEY, nextStore.slug)
    },
    getStore(slug) {
      return this.stores.find((store) => store.slug === slug)
    },
    createStore(payload) {
      const template = getTemplateById(payload.template || 'fashion')
      const slug = uniqueSlug(payload.slug || payload.name, this.stores)
      const products = normalizeProducts(payload.products || [])
      const nextStore = normalizeStore({
        id: `store-${Date.now()}`,
        merchantName: payload.merchantName || payload.name || 'New Merchant',
        name: payload.name || 'New Store',
        slug,
        category: payload.category || 'Lifestyle',
        description: payload.description || 'A NovaCart merchant storefront.',
        template: template.id,
        brandColor: payload.brandColor || template.accentColor,
        logoText: payload.logoText || initials(payload.name || 'New Store'),
        currency: payload.currency || 'USD',
        shippingMessage: payload.shippingMessage || 'Free shipping on qualifying orders',
        announcement: payload.announcement || payload.shippingMessage || 'Welcome to our new NovaCart store.',
        heroTitle: payload.heroTitle || `Shop ${payload.name || 'our store'}`,
        heroText: payload.heroText || payload.description || 'Discover products from this independent merchant.',
        published: false,
        setup: {
          details: Boolean(payload.name),
          template: Boolean(payload.template),
          products: Boolean(payload.products?.length),
          shipping: Boolean(payload.shippingMessage),
          preview: false,
          publish: false
        },
        categories: categoryListForProducts(products, payload.categories),
        products,
        analytics: {
          sales: 0,
          orders: 0,
          visitors: 0,
          conversionRate: '0.0%',
          averageOrderValue: 0,
          topProducts: [],
          trafficSources: ['Direct', 'Search', 'Social']
        }
      })
      this.merchantStores.push(nextStore)
      this.setCurrentStore(nextStore.slug)
      this.persistStores()
      return nextStore
    },
    updateStore(slug, patch) {
      const nextPatch = {
        ...patch,
        slug: patch.slug ? createSlug(patch.slug) : undefined
      }
      if (!patch.slug) {
        delete nextPatch.slug
      }
      const index = this.merchantStores.findIndex((store) => store.slug === slug)
      if (index === -1) {
        if (!demoStores.some((store) => store.slug === slug)) return null
        this.storeOverrides[slug] = {
          ...(this.storeOverrides[slug] || {}),
          ...nextPatch,
          slug,
          setup: {
            ...(this.storeOverrides[slug]?.setup || {}),
            ...(nextPatch.setup || {})
          }
        }
        localStorage.setItem(OVERRIDE_KEY, JSON.stringify(this.storeOverrides))
        return this.getStore(slug)
      }
      if (nextPatch.slug && nextPatch.slug !== slug) {
        nextPatch.slug = uniqueSlug(nextPatch.slug, this.stores.filter((store) => store.slug !== slug))
        if (this.currentStoreSlug === slug) {
          this.currentStoreSlug = nextPatch.slug
          localStorage.setItem(CURRENT_STORE_KEY, nextPatch.slug)
        }
      }
      this.merchantStores[index] = {
        ...this.merchantStores[index],
        ...nextPatch,
        setup: {
          ...this.merchantStores[index].setup,
          ...nextPatch.setup
        }
      }
      this.persistStores()
      return this.merchantStores[index]
    },
    publishStore(slug) {
      return this.updateStore(slug, {
        published: true,
        setup: { preview: true, publish: true }
      })
    },
    applyInventoryChange(slug, items, direction = -1) {
      const store = this.getStore(slug)
      if (!store || !Array.isArray(items) || !items.length) return []
      const quantities = items.reduce((result, item) => {
        const productId = Number(item.productId)
        const quantity = Math.max(0, Math.floor(Number(item.quantity) || 0))
        if (!productId || !quantity) return result
        result.set(productId, (result.get(productId) || 0) + quantity)
        return result
      }, new Map())
      const movements = []
      const products = store.products.map((product) => {
        const quantity = quantities.get(Number(product.id))
        if (!quantity) return product
        const quantityChange = quantity * (direction >= 0 ? 1 : -1)
        const stockAfter = Math.max(0, Number(product.stockQuantity || 0) + quantityChange)
        movements.push({
          productId: product.id,
          productName: product.name,
          quantityChange,
          stockAfter
        })
        return {
          ...product,
          stockQuantity: stockAfter
        }
      })
      if (movements.length) {
        this.updateStore(slug, { products })
      }
      return movements
    },
    persistStores() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.merchantStores))
      localStorage.setItem(OVERRIDE_KEY, JSON.stringify(this.storeOverrides))
    }
  }
})

function normalizeStore(store) {
  if (!store?.name || !store?.slug) return null
  const template = getTemplateById(store.template || 'fashion')
  const products = normalizeProducts(store.products || [])
  const name = cleanText(store.name) || 'New Store'
  const category = cleanText(store.category) || 'Lifestyle'
  const description = meaningfulText(store.description) || defaultStoreDescription(name, category)
  const shippingMessage = meaningfulText(store.shippingMessage) || 'Free shipping on orders over $75'
  const categories = categoryListForProducts(products, store.categories)
  return {
    ...store,
    name,
    merchantName: cleanText(store.merchantName) || name,
    slug: createSlug(store.slug),
    category,
    description,
    template: template.id,
    brandColor: store.brandColor || template.accentColor,
    logoText: cleanText(store.logoText) || initials(name),
    currency: store.currency || 'USD',
    shippingMessage,
    announcement: meaningfulText(store.announcement) || `New arrivals are live. ${shippingMessage}.`,
    heroTitle: meaningfulText(store.heroTitle) || defaultHeroTitle(name, category),
    heroText: meaningfulText(store.heroText) || description,
    categories,
    products,
    setup: {
      details: false,
      template: false,
      products: false,
      shipping: false,
      preview: false,
      publish: false,
      ...(store.setup || {})
    }
  }
}

function normalizeProducts(products) {
  return products.map((product, index) => {
    const id = Number(product.id) || Date.now() + index
    const price = Number(product.price) || 0
    const compareAtPrice = Number(product.compareAtPrice) || null
    const discountPercent = compareAtPrice
      ? Math.max(0, Math.round(((compareAtPrice - price) / compareAtPrice) * 100))
      : 0
    return {
      id,
      name: product.name || `Product ${index + 1}`,
      slug: createSlug(product.slug || product.name || `product-${index + 1}`),
      category: product.category || 'General',
      price,
      compareAtPrice,
      effectivePrice: price,
      discountPercent,
      stockQuantity: normalizeNonNegativeInteger(product.stockQuantity, 12),
      lowStockThreshold: normalizeNonNegativeInteger(product.lowStockThreshold, 5),
      imageUrl: product.imageUrl || publicAsset('demo-images/products/boutique-shirt.jpg'),
      imageGallery: product.imageGallery || [product.imageUrl || publicAsset('demo-images/products/boutique-shirt.jpg')],
      sizes: Array.isArray(product.sizes) ? product.sizes.filter(Boolean) : [],
      colors: Array.isArray(product.colors) ? product.colors.filter(Boolean) : [],
      material: product.material || productOptionsForCategory(product.category).material,
      careInstructions: product.careInstructions || productOptionsForCategory(product.category).careInstructions,
      badges: product.badges || (discountPercent ? ['Sale'] : ['New']),
      status: product.status || 'ACTIVE',
      rating: normalizeRating(product.rating, id),
      reviewCount: Number(product.reviewCount) || 24 + ((id % 9) * 7),
      merchandisingLabel: product.merchandisingLabel || merchandisingLabel(product, compareAtPrice, discountPercent),
      deliveryPromise: product.deliveryPromise || deliveryPromiseForCategory(product.category),
      reviewHighlights: Array.isArray(product.reviewHighlights) && product.reviewHighlights.length
        ? product.reviewHighlights
        : reviewHighlightsForCategory(product.category),
      description: meaningfulText(product.description) || productDescription(product.name, product.category)
    }
  })
}

function normalizeNonNegativeInteger(value, fallback) {
  if (value === null || value === undefined || value === '') return fallback
  const numberValue = Math.floor(Number(value))
  return Number.isFinite(numberValue) && numberValue >= 0 ? numberValue : fallback
}

function categoryListForProducts(products, existingCategories = []) {
  const source = [
    'New Arrivals',
    ...asArray(existingCategories),
    ...products.map((product) => product.category)
  ]
  return uniqueClean(source).slice(0, 8)
}

function asArray(value) {
  return Array.isArray(value) ? value : []
}

function uniqueClean(values) {
  const seen = new Set()
  return values.reduce((result, value) => {
    const text = cleanText(value)
    const key = text.toLowerCase()
    if (!text || seen.has(key)) return result
    seen.add(key)
    result.push(text)
    return result
  }, [])
}

function cleanText(value) {
  return String(value || '').replace(/\s+/g, ' ').trim()
}

function meaningfulText(value) {
  const text = cleanText(value)
  return isWeakText(text) ? '' : text
}

function isWeakText(text) {
  if (!text || text.length < 9) return true
  const lower = text.toLowerCase()
  if (/(^|\s)(test|demo|asdf|qwer|wqwqd|lorem|placeholder|sample)(\s|$)/.test(lower)) return true
  const letters = lower.replace(/[^a-z]/g, '')
  if (letters.length >= 5 && !/[aeiou]/.test(letters)) return true
  if (letters.length >= 5 && new Set(letters).size <= 3) return true
  return false
}

function defaultStoreDescription(name, category) {
  return `${name} curates original ${category.toLowerCase()} pieces with clear product details, demo-safe checkout, and merchant-style delivery updates.`
}

function defaultHeroTitle(name, category) {
  if (category.toLowerCase().includes('fashion')) return `An edited wardrobe from ${name}`
  if (category.toLowerCase().includes('home')) return `Considered goods from ${name}`
  return `Fresh picks from ${name}`
}

function productDescription(name, category) {
  const categoryCopy = cleanText(category) || 'catalog'
  return `${cleanText(name) || 'This piece'} is part of the ${categoryCopy.toLowerCase()} edit, selected for everyday styling, clear fit notes, and reliable demo inventory.`
}

function normalizeRating(value, id) {
  const rating = Number(value)
  if (Number.isFinite(rating) && rating > 0) return Number(Math.min(5, rating).toFixed(1))
  return Number((4.4 + ((Number(id) % 6) * 0.08)).toFixed(1))
}

function merchandisingLabel(product, compareAtPrice, discountPercent) {
  if (discountPercent || compareAtPrice) return 'Limited markdown'
  if (Number(product.stockQuantity) <= Number(product.lowStockThreshold || 5)) return 'Low stock watch'
  if (product.badges?.includes?.('Best Seller')) return 'Customer favorite'
  return 'New this week'
}

function deliveryPromiseForCategory(category) {
  const key = cleanText(category).toLowerCase()
  if (['equipment', 'home living', 'kitchen', 'objects'].includes(key)) return 'Ships in 2-4 business days'
  if (['bags', 'jewelry', 'accessories'].includes(key)) return 'Gift-ready packing available'
  return 'Ships from merchant in 1-3 business days'
}

function reviewHighlightsForCategory(category) {
  const key = cleanText(category).toLowerCase()
  if (key === 'shoes') return ['Comfort noted by early shoppers', 'Fit guidance included before checkout']
  if (['bags', 'jewelry', 'accessories'].includes(key)) return ['Polished finish', 'Easy gifting choice']
  if (['home living', 'kitchen', 'objects'].includes(key)) return ['Looks refined in daily spaces', 'Packed with care by the merchant']
  return ['Easy to style with repeat outfits', 'Soft finish and reliable sizing notes']
}

function productOptionsForCategory(category) {
  const key = cleanText(category).toLowerCase()
  if (['women', 'men', 'knitwear', 'activewear', 'new arrivals'].includes(key)) {
    return {
      material: 'Responsibly sourced cotton blend',
      careInstructions: 'Machine wash cold and lay flat to dry.'
    }
  }
  if (key === 'shoes') {
    return {
      material: 'Structured upper with cushioned footbed',
      careInstructions: 'Wipe clean with a soft cloth.'
    }
  }
  if (['bags', 'accessories', 'jewelry'].includes(key)) {
    return {
      material: key === 'jewelry' ? 'Polished brass and glass pearl finish' : 'Structured vegan leather',
      careInstructions: 'Store dry and avoid prolonged moisture.'
    }
  }
  return {
    material: 'Small-batch mixed materials',
    careInstructions: 'Follow the merchant care card included with the order.'
  }
}

function uniqueSlug(value, stores) {
  const base = createSlug(value)
  let candidate = base
  let suffix = 2
  while (stores.some((store) => store.slug === candidate)) {
    candidate = `${base}-${suffix}`
    suffix += 1
  }
  return candidate
}

function initials(value) {
  return String(value || 'NS')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'NS'
}
