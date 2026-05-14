import { defineStore } from 'pinia'

const STORAGE_KEY = 'novacart_cart'

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
        this.items = JSON.parse(rawCart)
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    },
    addItem(product, quantity = 1) {
      if (!product || product.stockQuantity < 1) return

      const existingItem = this.items.find((item) => item.productId === product.id)
      if (existingItem) {
        existingItem.quantity = Math.min(existingItem.quantity + quantity, product.stockQuantity)
      } else {
        this.items.push({
          productId: product.id,
          name: product.name,
          price: Number(product.price),
          imageUrl: product.imageUrl,
          stockQuantity: product.stockQuantity,
          quantity
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
        item.quantity = Math.min(quantity, item.stockQuantity)
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
