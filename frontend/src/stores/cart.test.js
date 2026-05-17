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

  it('keeps size and color variants as separate cart lines', () => {
    const cartStore = useCartStore()
    const product = {
      id: 7,
      name: 'Tailored Travel Trouser',
      price: 88,
      effectivePrice: 70,
      discountAmount: 18,
      discountPercent: 20,
      imageUrl: '',
      stockQuantity: 4,
      sizes: ['S', 'M'],
      colors: ['Black', 'Stone']
    }

    cartStore.addItem(product, 1, { selectedSize: 'S', selectedColor: 'Black' })
    cartStore.addItem(product, 2, { selectedSize: 'M', selectedColor: 'Stone' })

    expect(cartStore.items).toHaveLength(2)
    expect(cartStore.itemCount).toBe(3)
    expect(cartStore.subtotal).toBe(210)
    expect(cartStore.discountTotal).toBe(54)
    expect(cartStore.items.map((item) => item.lineKey)).toEqual(['7|S|Black', '7|M|Stone'])
  })

  it('updates a specific fashion variant line by line key', () => {
    const cartStore = useCartStore()
    const product = {
      id: 11,
      name: 'Mesh Sling Bag',
      price: 76,
      imageUrl: '',
      stockQuantity: 5,
      sizes: ['One Size'],
      colors: ['Black', 'Taupe']
    }

    cartStore.addItem(product, 1, { selectedSize: 'One Size', selectedColor: 'Black' })
    cartStore.addItem(product, 2, { selectedSize: 'One Size', selectedColor: 'Taupe' })
    cartStore.updateQuantity('11|One Size|Taupe', 4)

    expect(cartStore.items).toHaveLength(2)
    expect(cartStore.items.find((item) => item.selectedColor === 'Black').quantity).toBe(1)
    expect(cartStore.items.find((item) => item.selectedColor === 'Taupe').quantity).toBe(4)
    expect(cartStore.itemCount).toBe(5)
  })

  it('does not add unavailable fashion products', () => {
    const cartStore = useCartStore()

    cartStore.addItem({
      id: 12,
      name: 'Sold Out Satin Wrap',
      price: 86,
      imageUrl: '',
      stockQuantity: 0
    })

    expect(cartStore.items).toHaveLength(0)
  })

  it('defaults to the first available size and color when adding from a product card', () => {
    const cartStore = useCartStore()

    cartStore.addItem({
      id: 8,
      name: 'Weekend Knit Polo',
      price: 52,
      imageUrl: '',
      stockQuantity: 3,
      sizes: ['M', 'L'],
      colors: ['Navy', 'Cream']
    })

    expect(cartStore.items[0]).toMatchObject({
      selectedSize: 'M',
      selectedColor: 'Navy',
      lineKey: '8|M|Navy'
    })
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
