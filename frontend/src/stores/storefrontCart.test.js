import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useStorefrontCartStore } from './storefrontCart'

describe('storefront cart store', () => {
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

  it('keeps generated storefront variants as separate cart lines', () => {
    const cartStore = useStorefrontCartStore()
    const product = {
      id: 1001,
      name: 'Ivory Collarless Blazer',
      price: 128,
      compareAtPrice: 168,
      imageUrl: '/demo-images/products/fashion-blazer.jpg',
      stockQuantity: 4,
      discountPercent: 24
    }

    cartStore.addItem('demo-fashion', product, 1, { size: 'S', color: 'Ivory' })
    cartStore.addItem('demo-fashion', product, 2, { size: 'M', color: 'Black' })

    const items = cartStore.itemsForStore('demo-fashion')
    expect(items).toHaveLength(2)
    expect(items.map((item) => item.itemId)).toEqual(['1001::S::Ivory', '1001::M::Black'])
    expect(cartStore.itemCountForStore('demo-fashion')).toBe(3)
    expect(cartStore.discountTotalForStore('demo-fashion')).toBe(120)
  })

  it('updates and removes the selected generated storefront variant', () => {
    const cartStore = useStorefrontCartStore()
    const product = {
      id: 2003,
      name: 'Grip Court Sneaker',
      price: 118,
      imageUrl: '/demo-images/products/sports-sneaker.jpg',
      stockQuantity: 5
    }

    cartStore.addItem('demo-sports', product, 1, { size: '8', color: 'Ivory' })
    cartStore.addItem('demo-sports', product, 1, { size: '9', color: 'Black' })
    cartStore.updateQuantity('demo-sports', '2003::9::Black', 4)
    cartStore.removeItem('demo-sports', '2003::8::Ivory')

    const items = cartStore.itemsForStore('demo-sports')
    expect(items).toHaveLength(1)
    expect(items[0]).toMatchObject({
      itemId: '2003::9::Black',
      quantity: 4,
      options: { size: '9', color: 'Black' }
    })
  })

  it('normalizes generated storefront cart data loaded from localStorage', () => {
    storage.set('novacart_storefront_carts', JSON.stringify({
      'demo-fashion': [
        {
          productId: 1001,
          name: 'Ivory Collarless Blazer',
          price: '128',
          compareAtPrice: '168',
          stockQuantity: '3',
          quantity: '9',
          imageUrl: '/demo-images/products/fashion-blazer.jpg',
          options: { size: 'M', color: 'Ivory' }
        },
        {
          productId: 0,
          name: '',
          price: -2,
          stockQuantity: 0,
          quantity: 1
        }
      ]
    }))
    const cartStore = useStorefrontCartStore()

    cartStore.loadCarts()

    const items = cartStore.itemsForStore('demo-fashion')
    expect(items).toHaveLength(1)
    expect(items[0]).toMatchObject({
      itemId: '1001::M::Ivory',
      productId: 1001,
      price: 128,
      quantity: 3
    })
  })

  it('tracks saved products per generated storefront', () => {
    const cartStore = useStorefrontCartStore()

    expect(cartStore.toggleFavorite('demo-fashion', 1001)).toBe(true)
    expect(cartStore.toggleFavorite('demo-fashion', 1002)).toBe(true)
    expect(cartStore.favoriteCountForStore('demo-fashion')).toBe(2)
    expect(cartStore.isFavoriteForStore('demo-fashion', 1001)).toBe(true)

    expect(cartStore.toggleFavorite('demo-fashion', 1001)).toBe(false)
    expect(cartStore.favoriteIdsForStore('demo-fashion')).toEqual([1002])
  })

  it('keeps a bounded recently viewed list per generated storefront', () => {
    const cartStore = useStorefrontCartStore()

    Array.from({ length: 8 }).forEach((_, index) => {
      cartStore.recordRecentlyViewed('demo-home', {
        id: 3000 + index,
        name: `Home Product ${index}`,
        category: 'Home Living',
        imageUrl: '/demo-images/products/home-throw.jpg',
        price: 48 + index,
        rating: 4.6,
        reviewCount: 20 + index
      })
    })
    cartStore.recordRecentlyViewed('demo-home', {
      id: 3004,
      name: 'Cedar Drawer Sachet Set',
      category: 'Gifts',
      imageUrl: '/demo-images/products/home-sachets.jpg',
      price: 22,
      rating: 4.8,
      reviewCount: 64
    })

    const viewed = cartStore.recentlyViewedForStore('demo-home')
    expect(viewed).toHaveLength(6)
    expect(viewed[0]).toMatchObject({
      productId: 3004,
      name: 'Cedar Drawer Sachet Set',
      rating: 4.8,
      reviewCount: 64
    })
    expect(new Set(viewed.map((item) => item.productId)).size).toBe(6)
  })
})
