import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { demoStores, storeTemplates } from '../data/platform'
import { usePlatformStore } from './platform'

describe('platform store generated merchant normalization', () => {
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

  it('replaces weak generated-store copy and dedupes repeated categories', () => {
    const platformStore = usePlatformStore()

    const store = platformStore.createStore({
      name: 'Shop 123',
      template: 'fashion',
      category: 'Fashion',
      description: 'wqwqd',
      heroText: 'wqwqd',
      products: [
        {
          id: 9001,
          name: 'Soft Cotton Shirt',
          category: 'New Arrivals',
          description: 'wqwqd',
          price: 74,
          stockQuantity: 8
        },
        {
          id: 9002,
          name: 'Small Leather Wallet',
          category: 'New Arrivals',
          price: 48,
          stockQuantity: 4
        }
      ]
    })

    expect(store.description).not.toContain('wqwqd')
    expect(store.heroText).not.toContain('wqwqd')
    expect(store.categories).toEqual(['New Arrivals'])
    expect(store.products[0]).toMatchObject({
      rating: expect.any(Number),
      merchandisingLabel: expect.any(String)
    })
    expect(store.products[0].description).not.toContain('wqwqd')
  })

  it('preserves zero stock and applies local generated-store inventory changes', () => {
    const platformStore = usePlatformStore()

    const store = platformStore.createStore({
      name: 'Stock Studio',
      template: 'minimal',
      category: 'Home',
      products: [
        {
          id: 9101,
          name: 'Sold Out Vase',
          category: 'Objects',
          price: 52,
          stockQuantity: 0
        },
        {
          id: 9102,
          name: 'Linen Runner',
          category: 'Home Living',
          price: 42,
          stockQuantity: 3
        }
      ]
    })

    expect(store.products[0].stockQuantity).toBe(0)

    const deducted = platformStore.applyInventoryChange(store.slug, [{ productId: 9102, quantity: 2 }], -1)

    expect(deducted).toEqual([
      {
        productId: 9102,
        productName: 'Linen Runner',
        quantityChange: -2,
        stockAfter: 1
      }
    ])
    expect(platformStore.getStore(store.slug).products.find((product) => product.id === 9102).stockQuantity).toBe(1)

    platformStore.applyInventoryChange(store.slug, [{ productId: 9102, quantity: 1 }], 1)

    expect(platformStore.getStore(store.slug).products.find((product) => product.id === 9102).stockQuantity).toBe(2)
  })

  it('keeps every storefront template distinct and previewable', () => {
    for (const template of storeTemplates) {
      expect(template.layoutName).toBeTruthy()
      expect(template.primaryGoal).toBeTruthy()
      expect(template.homepageModules.length).toBeGreaterThanOrEqual(3)
      expect(template.easyEdits.length).toBeGreaterThanOrEqual(3)
      expect(demoStores.some((store) => store.template === template.id)).toBe(true)
    }
  })

  it('normalizes products updated from the simple store builder', () => {
    const platformStore = usePlatformStore()
    const store = platformStore.createStore({
      name: 'Builder Studio',
      template: 'home',
      products: []
    })

    platformStore.updateStore(store.slug, {
      categories: ['Objects', 'Objects', 'Gifts'],
      products: [
        {
          id: 9301,
          name: 'Gift Vase',
          category: 'Objects',
          price: 34,
          stockQuantity: 0
        }
      ]
    })

    const updatedStore = platformStore.getStore(store.slug)
    expect(updatedStore.categories).toEqual(['New Arrivals', 'Objects', 'Gifts'])
    expect(updatedStore.products[0]).toMatchObject({
      slug: 'gift-vase',
      stockQuantity: 0,
      imageGallery: expect.any(Array),
      deliveryPromise: expect.any(String)
    })
  })

  it('creates merchant stores with editable website content defaults', () => {
    const platformStore = usePlatformStore()

    const store = platformStore.createStore({
      name: 'Content Studio',
      template: 'minimal',
      description: 'A merchant storefront with editable public content.',
      products: [
        {
          id: 9401,
          name: 'First Product',
          category: 'Objects',
          price: 24,
          stockQuantity: 5
        },
        {
          id: 9402,
          name: 'Second Product',
          category: 'Gifts',
          price: 30,
          stockQuantity: 5
        }
      ]
    })

    expect(store).toMatchObject({
      heroButtonLabel: 'Shop products',
      secondaryButtonLabel: 'Browse new arrivals',
      aboutTitle: 'Content Studio story',
      seoTitle: 'Content Studio | NovaCart Storefront'
    })
    expect(store.featuredProductIds).toEqual([9401, 9402])

    platformStore.updateStore(store.slug, {
      aboutTitle: 'Updated public story',
      seoDescription: 'Updated SEO description.',
      featuredProductIds: [9402],
      categories: ['Gifts']
    })

    const updatedStore = platformStore.getStore(store.slug)
    expect(updatedStore.aboutTitle).toBe('Updated public story')
    expect(updatedStore.seoDescription).toBe('Updated SEO description.')
    expect(updatedStore.featuredProductIds).toEqual([9402])
    expect(updatedStore.categories).toEqual(['New Arrivals', 'Gifts', 'Objects'])
  })
})
