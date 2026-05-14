import { defineStore } from 'pinia'

const STORAGE_KEY = 'novacart_cart'
const MIN_QUANTITY = 1

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),
  getters: {
    itemCount: (state) => state.items.reduce((total, item) => total + item.quantity, 0),
    subtotal: (state) => state.items.reduce((total, item) => total + Number(item.price) * item.quantity, 0)
  },
  actions: {
    loadCart() {
      const rawCart = localStorage.getItem(STORAGE_KEY)
      if (!rawCart) return

      try {
        const parsedCart = JSON.parse(rawCart)
        this.items = Array.isArray(parsedCart)
          ? parsedCart.map(normalizeStoredItem).filter(Boolean)
          : []
        this.persist()
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    },
    addItem(product, quantity = 1) {
      const stockQuantity = normalizeStock(product?.stockQuantity)
      if (!product || stockQuantity < MIN_QUANTITY) return

      const requestedQuantity = Math.min(normalizeQuantity(quantity), stockQuantity)

      const existingItem = this.items.find((item) => item.productId === product.id)
      if (existingItem) {
        existingItem.name = product.name
        existingItem.price = normalizePrice(product.price)
        existingItem.imageUrl = product.imageUrl
        existingItem.stockQuantity = stockQuantity
        existingItem.quantity = Math.min(existingItem.quantity + requestedQuantity, stockQuantity)
      } else {
        this.items.push({
          productId: product.id,
          name: product.name,
          price: normalizePrice(product.price),
          imageUrl: product.imageUrl,
          stockQuantity,
          quantity: requestedQuantity
        })
      }
      this.persist()
    },
    updateQuantity(productId, quantity) {
      if (!Number.isFinite(quantity) || quantity < 1) {
        this.removeItem(productId)
        return
      }

      const item = this.items.find((entry) => entry.productId === productId)
      if (item) {
        item.quantity = Math.min(normalizeQuantity(quantity), item.stockQuantity)
        this.persist()
      }
    },
    removeItem(productId) {
      this.items = this.items.filter((item) => item.productId !== productId)
      this.persist()
    },
    clearCart() {
      this.items = []
      this.persist()
    },
    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.items))
    }
  }
})

function normalizeStoredItem(item) {
  const stockQuantity = normalizeStock(item?.stockQuantity)
  const productId = Number(item?.productId)
  const price = Number(item?.price)

  if (!productId || !item?.name || stockQuantity < MIN_QUANTITY || !Number.isFinite(price) || price < 0) {
    return null
  }

  return {
    productId,
    name: String(item.name),
    price,
    imageUrl: item.imageUrl || '',
    stockQuantity,
    quantity: Math.min(normalizeQuantity(item.quantity), stockQuantity)
  }
}

function normalizeQuantity(value) {
  const quantity = Number(value)
  if (!Number.isFinite(quantity)) return MIN_QUANTITY
  return Math.max(MIN_QUANTITY, Math.floor(quantity))
}

function normalizeStock(value) {
  const stock = Number(value)
  if (!Number.isFinite(stock)) return 0
  return Math.max(0, Math.floor(stock))
}

function normalizePrice(value) {
  const price = Number(value)
  if (!Number.isFinite(price)) return 0
  return Math.max(0, price)
}
