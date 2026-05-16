import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useCartStore } from './cart'

const STORAGE_KEY = 'novacart_cart'

describe('cart store', () => {
  let storage

  beforeEach(() => {
    storage = new Map()
    Object.defineProperty(globalThis, 'localStorage', {
      configurable: true,
      value: {
        getItem: (key) => storage.get(key) || null,
        setItem: (key, value) => storage.set(key, value),
        removeItem: (key) => storage.delete(key)
      }
    })
    setActivePinia(createPinia())
  })

  it('adds products and clamps quantity to available stock', () => {
    const cartStore = useCartStore()

    cartStore.addItem({
      id: 1,
      name: 'Silk Wrap Blouse',
      price: '39.00',
      imageUrl: 'https://example.com/product.jpg',
      stockQuantity: 2
    }, 5)

    expect(cartStore.items).toHaveLength(1)
    expect(cartStore.items[0].quantity).toBe(2)
    expect(cartStore.itemCount).toBe(2)
    expect(cartStore.subtotal).toBe(78)
  })

  it('removes items when quantity is updated below one', () => {
    const cartStore = useCartStore()
    cartStore.addItem({
      id: 1,
      name: 'Silk Wrap Blouse',
      price: 39,
      imageUrl: '',
      stockQuantity: 5
    }, 1)

    cartStore.updateQuantity(1, 0)

    expect(cartStore.items).toHaveLength(0)
  })

  it('loads only valid persisted cart items', () => {
    storage.set(STORAGE_KEY, JSON.stringify([
      {
        productId: 1,
        name: 'Silk Wrap Blouse',
        price: 39,
        imageUrl: '',
        stockQuantity: 3,
        quantity: 9
      },
      {
        productId: 2,
        name: '',
        price: 20,
        stockQuantity: 2,
        quantity: 1
      }
    ]))
    const cartStore = useCartStore()

    cartStore.loadCart()

    expect(cartStore.items).toHaveLength(1)
    expect(cartStore.items[0].quantity).toBe(3)
  })
})
