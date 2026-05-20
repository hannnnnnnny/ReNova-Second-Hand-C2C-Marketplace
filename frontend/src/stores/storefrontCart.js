import { defineStore } from 'pinia'

const STORAGE_KEY = 'novacart_storefront_carts'
const FAVORITES_KEY = 'novacart_storefront_favorites'
const RECENTLY_VIEWED_KEY = 'novacart_storefront_recently_viewed'
const PROMOTIONS_KEY = 'novacart_storefront_promotions'

export const useStorefrontCartStore = defineStore('storefrontCart', {
  state: () => ({
    carts: {},
    favorites: {},
    recentlyViewed: {},
    promotions: {}
  }),
  getters: {
    itemsForStore: (state) => (storeSlug) => state.carts[storeSlug] || [],
    itemCountForStore: (state) => (storeSlug) => (state.carts[storeSlug] || []).reduce((total, item) => total + item.quantity, 0),
    subtotalForStore: (state) => (storeSlug) => (state.carts[storeSlug] || []).reduce((total, item) => total + item.price * item.quantity, 0),
    discountTotalForStore: (state) => (storeSlug) => (state.carts[storeSlug] || []).reduce((total, item) => total + (item.discountAmount || 0) * item.quantity, 0),
    favoriteIdsForStore: (state) => (storeSlug) => state.favorites[storeSlug] || [],
    favoriteCountForStore: (state) => (storeSlug) => (state.favorites[storeSlug] || []).length,
    isFavoriteForStore: (state) => (storeSlug, productId) => (state.favorites[storeSlug] || []).includes(normalizeProductId(productId)),
    recentlyViewedForStore: (state) => (storeSlug) => state.recentlyViewed[storeSlug] || [],
    promotionForStore: (state) => (storeSlug) => state.promotions[storeSlug] || null,
    promotionDiscountForStore: (state) => (storeSlug, subtotal) => promotionDiscount(state.promotions[storeSlug], subtotal)
  },
  actions: {
    loadCarts() {
      const rawCarts = localStorage.getItem(STORAGE_KEY)
      if (rawCarts) {
        try {
          this.carts = normalizeCartMap(JSON.parse(rawCarts))
          this.persistCarts()
        } catch {
          localStorage.removeItem(STORAGE_KEY)
          this.carts = {}
        }
      }

      const rawFavorites = localStorage.getItem(FAVORITES_KEY)
      if (rawFavorites) {
        try {
          this.favorites = normalizeFavorites(JSON.parse(rawFavorites))
          this.persistFavorites()
        } catch {
          localStorage.removeItem(FAVORITES_KEY)
          this.favorites = {}
        }
      }

      const rawRecentlyViewed = localStorage.getItem(RECENTLY_VIEWED_KEY)
      if (rawRecentlyViewed) {
        try {
          this.recentlyViewed = normalizeRecentlyViewed(JSON.parse(rawRecentlyViewed))
          this.persistRecentlyViewed()
        } catch {
          localStorage.removeItem(RECENTLY_VIEWED_KEY)
          this.recentlyViewed = {}
        }
      }

      const rawPromotions = localStorage.getItem(PROMOTIONS_KEY)
      if (rawPromotions) {
        try {
          this.promotions = normalizePromotions(JSON.parse(rawPromotions))
          this.persistPromotions()
        } catch {
          localStorage.removeItem(PROMOTIONS_KEY)
          this.promotions = {}
        }
      }
    },
    addItem(storeSlug, product, quantity = 1, options = {}) {
      if (!storeSlug || !product || normalizeStock(product.stockQuantity) < 1) return
      if (!this.carts[storeSlug]) this.carts[storeSlug] = []
      const stockQuantity = normalizeStock(product.stockQuantity)
      const requestedQuantity = normalizeQuantity(quantity)
      const selectedOptions = {
        size: clean(options.size),
        color: clean(options.color)
      }
      const itemId = cartItemId(product.id, selectedOptions)
      const existing = this.carts[storeSlug].find((item) => (item.itemId || cartItemId(item.productId, item.options || {})) === itemId)
      if (existing) {
        existing.quantity = Math.min(existing.quantity + requestedQuantity, stockQuantity)
        existing.stockQuantity = stockQuantity
      } else {
        const compareAtPrice = Number(product.compareAtPrice) || null
        const price = normalizePrice(product.effectivePrice ?? product.price)
        this.carts[storeSlug].push({
          itemId,
          productId: product.id,
          name: String(product.name || 'Product'),
          price,
          compareAtPrice,
          discountAmount: compareAtPrice ? Math.max(0, compareAtPrice - price) : 0,
          discountPercent: product.discountPercent || 0,
          imageUrl: product.imageUrl || '',
          stockQuantity,
          options: selectedOptions,
          quantity: Math.min(requestedQuantity, stockQuantity)
        })
      }
      this.persistCarts()
    },
    updateQuantity(storeSlug, itemId, quantity) {
      const cart = this.carts[storeSlug] || []
      const item = cart.find((entry) => matchesCartItem(entry, itemId))
      if (!item) return
      const nextQuantity = Math.floor(Number(quantity) || 0)
      if (nextQuantity < 1) {
        this.removeItem(storeSlug, itemId)
        return
      }
      item.quantity = Math.min(nextQuantity, item.stockQuantity)
      this.persistCarts()
    },
    removeItem(storeSlug, itemId) {
      this.carts[storeSlug] = (this.carts[storeSlug] || []).filter((item) => !matchesCartItem(item, itemId))
      this.persistCarts()
    },
    clearStoreCart(storeSlug) {
      this.carts[storeSlug] = []
      delete this.promotions[storeSlug]
      this.persistCarts()
      this.persistPromotions()
    },
    toggleFavorite(storeSlug, productId) {
      const normalizedId = normalizeProductId(productId)
      if (!storeSlug || !normalizedId) return false
      const favorites = new Set(this.favorites[storeSlug] || [])
      if (favorites.has(normalizedId)) {
        favorites.delete(normalizedId)
      } else {
        favorites.add(normalizedId)
      }
      this.favorites[storeSlug] = Array.from(favorites)
      this.persistFavorites()
      return favorites.has(normalizedId)
    },
    recordRecentlyViewed(storeSlug, product) {
      if (!storeSlug || !product) return
      const productId = normalizeProductId(product.id)
      if (!productId) return

      const viewedProduct = {
        productId,
        viewedAt: new Date().toISOString(),
        name: clean(product.name) || 'Product',
        category: clean(product.category),
        imageUrl: clean(product.imageUrl),
        price: normalizePrice(product.effectivePrice ?? product.price),
        rating: normalizeRating(product.rating),
        reviewCount: normalizeStock(product.reviewCount)
      }

      const previousItems = this.recentlyViewed[storeSlug] || []
      this.recentlyViewed[storeSlug] = [
        viewedProduct,
        ...previousItems.filter((item) => item.productId !== productId)
      ].slice(0, 6)
      this.persistRecentlyViewed()
    },
    applyPromotion(storeSlug, code) {
      const promotion = promotionForCode(code)
      if (!storeSlug || !promotion) {
        return {
          applied: false,
          message: 'Enter WELCOME10, STYLE10, or FREESHIP to preview a demo promotion.'
        }
      }
      this.promotions[storeSlug] = promotion
      this.persistPromotions()
      return {
        applied: true,
        message: `${promotion.code} applied.`
      }
    },
    clearPromotion(storeSlug) {
      delete this.promotions[storeSlug]
      this.persistPromotions()
    },
    persistCarts() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.carts))
    },
    persistFavorites() {
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(this.favorites))
    },
    persistRecentlyViewed() {
      localStorage.setItem(RECENTLY_VIEWED_KEY, JSON.stringify(this.recentlyViewed))
    },
    persistPromotions() {
      localStorage.setItem(PROMOTIONS_KEY, JSON.stringify(this.promotions))
    }
  }
})

function cartItemId(productId, options = {}) {
  return [productId, options.size || '', options.color || ''].join('::')
}

function matchesCartItem(item, itemId) {
  return item.itemId === itemId || String(item.productId) === String(itemId)
}

function normalizeCartMap(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {}
  return Object.entries(value).reduce((result, [storeSlug, items]) => {
    if (!Array.isArray(items)) return result
    const normalizedItems = items.map(normalizeStoredItem).filter(Boolean)
    if (normalizedItems.length) result[storeSlug] = normalizedItems
    return result
  }, {})
}

function normalizeStoredItem(item) {
  const productId = normalizeProductId(item?.productId)
  const name = clean(item?.name)
  const price = normalizePrice(item?.price)
  const stockQuantity = normalizeStock(item?.stockQuantity)
  const quantity = normalizeQuantity(item?.quantity)
  if (!productId || !name || stockQuantity < 1) return null
  const options = {
    size: clean(item?.options?.size),
    color: clean(item?.options?.color)
  }
  const compareAtPrice = normalizeNullablePrice(item?.compareAtPrice)
  return {
    itemId: item?.itemId || cartItemId(productId, options),
    productId,
    name,
    price,
    compareAtPrice,
    discountAmount: normalizePrice(item?.discountAmount),
    discountPercent: normalizePrice(item?.discountPercent),
    imageUrl: clean(item?.imageUrl),
    stockQuantity,
    options,
    quantity: Math.min(quantity, stockQuantity)
  }
}

function normalizeFavorites(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {}
  return Object.entries(value).reduce((result, [storeSlug, productIds]) => {
    if (!Array.isArray(productIds)) return result
    const normalizedIds = Array.from(new Set(productIds.map(normalizeProductId).filter(Boolean)))
    if (normalizedIds.length) result[storeSlug] = normalizedIds
    return result
  }, {})
}

function normalizeRecentlyViewed(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {}
  return Object.entries(value).reduce((result, [storeSlug, products]) => {
    if (!Array.isArray(products)) return result
    const normalizedProducts = products.map(normalizeRecentlyViewedItem).filter(Boolean)
    if (normalizedProducts.length) result[storeSlug] = normalizedProducts.slice(0, 6)
    return result
  }, {})
}

function normalizeRecentlyViewedItem(item) {
  const productId = normalizeProductId(item?.productId)
  const name = clean(item?.name)
  if (!productId || !name) return null
  return {
    productId,
    viewedAt: clean(item?.viewedAt) || new Date().toISOString(),
    name,
    category: clean(item?.category),
    imageUrl: clean(item?.imageUrl),
    price: normalizePrice(item?.price),
    rating: normalizeRating(item?.rating),
    reviewCount: normalizeStock(item?.reviewCount)
  }
}

function promotionForCode(code) {
  const normalizedCode = clean(code).toUpperCase()
  const promotions = {
    WELCOME10: {
      code: 'WELCOME10',
      label: 'Welcome 10% off',
      type: 'percentage',
      value: 0.1
    },
    STYLE10: {
      code: 'STYLE10',
      label: 'Style edit 10% off',
      type: 'percentage',
      value: 0.1
    },
    FREESHIP: {
      code: 'FREESHIP',
      label: 'Free standard shipping',
      type: 'free_shipping',
      value: 0
    }
  }
  return promotions[normalizedCode] || null
}

function promotionDiscount(promotion, subtotal) {
  if (!promotion || promotion.type !== 'percentage') return 0
  const amount = normalizePrice(subtotal) * Number(promotion.value || 0)
  return Number(amount.toFixed(2))
}

function normalizePromotions(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {}
  return Object.entries(value).reduce((result, [storeSlug, promotion]) => {
    const normalized = promotionForCode(promotion?.code)
    if (normalized) result[storeSlug] = normalized
    return result
  }, {})
}

function normalizeProductId(value) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : null
}

function normalizeQuantity(value) {
  const quantity = Math.floor(Number(value) || 1)
  return Math.max(1, quantity)
}

function normalizeStock(value) {
  const stock = Math.floor(Number(value) || 0)
  return Math.max(0, stock)
}

function normalizePrice(value) {
  const price = Number(value)
  return Number.isFinite(price) && price > 0 ? price : 0
}

function normalizeNullablePrice(value) {
  const price = Number(value)
  return Number.isFinite(price) && price > 0 ? price : null
}

function normalizeRating(value) {
  const rating = Number(value)
  return Number.isFinite(rating) && rating > 0 ? Math.min(5, rating) : 0
}

function clean(value) {
  return value ? String(value).trim() : ''
}
