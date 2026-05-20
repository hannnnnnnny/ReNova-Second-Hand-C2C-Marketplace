import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
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
})
