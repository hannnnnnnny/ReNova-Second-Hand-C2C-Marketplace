import { beforeEach, describe, expect, it } from 'vitest'
import {
  createDemoTracking,
  loadLocalCareRequests,
  loadOrder,
  loadStoreOrders,
  loadStoreStockMovements,
  maskCardNumber,
  paymentStatusForMethod,
  saveLocalCareRequest,
  saveStoreOrder,
  saveStoreStockMovement,
  updateLocalCareRequest,
  updateStoredOrder
} from './orderTracking'

describe('order tracking utilities', () => {
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
  })

  it('creates a paid order tracking timeline with demo carrier data', () => {
    const order = {
      id: 'local-demo-fashion-1',
      storeSlug: 'demo-fashion',
      deliveryMethod: 'EXPRESS',
      paymentStatus: 'PAID',
      createdAt: '2026-05-20T10:00:00.000Z'
    }

    const tracking = createDemoTracking(order, { slug: 'demo-fashion' }, new Date('2026-05-20T10:00:00.000Z'))

    expect(tracking.carrier).toBe('MetroParcel Express')
    expect(tracking.currentStatus).toBe('Processing')
    expect(tracking.trackingNumber).toMatch(/^NVC-/)
    expect(tracking.timeline.map((event) => event.status).slice(0, 3)).toEqual(['complete', 'complete', 'current'])
  })

  it('saves and loads store order history by storefront', () => {
    const order = {
      id: 'local-demo-fashion-2',
      storeSlug: 'demo-fashion',
      paymentStatus: 'PENDING',
      paymentMethod: 'BANK_TRANSFER',
      deliveryMethod: 'STANDARD',
      createdAt: '2026-05-20T10:00:00.000Z',
      items: []
    }

    saveStoreOrder(order)

    expect(loadOrder(order.id)).toMatchObject({
      id: order.id,
      tracking: {
        currentStatus: 'Order placed',
        carrier: 'NovaPost Standard'
      }
    })
    expect(loadStoreOrders('demo-fashion')).toHaveLength(1)
  })

  it('updates local orders and records local care and stock movement queues', () => {
    saveStoreOrder({
      id: 'local-demo-fashion-3',
      storeSlug: 'demo-fashion',
      paymentStatus: 'PAID',
      deliveryMethod: 'STANDARD',
      createdAt: '2026-05-20T10:00:00.000Z',
      items: []
    })

    expect(updateStoredOrder('local-demo-fashion-3', { status: 'CANCELLED' })).toMatchObject({
      id: 'local-demo-fashion-3',
      status: 'CANCELLED'
    })

    const support = saveLocalCareRequest('support', {
      storeSlug: 'demo-fashion',
      customerName: 'Morgan Lee',
      email: 'morgan@example.com',
      message: 'Need help with delivery.'
    })
    expect(loadLocalCareRequests({ storeSlug: 'demo-fashion', type: 'support' })).toHaveLength(1)
    expect(updateLocalCareRequest(support.id, { status: 'RESOLVED' })).toMatchObject({ status: 'RESOLVED' })

    saveStoreStockMovement({
      storeSlug: 'demo-fashion',
      productId: 1001,
      productName: 'Ivory Collarless Blazer',
      orderId: 'local-demo-fashion-3',
      type: 'ORDER_PLACED',
      quantityChange: -1,
      stockAfter: 3
    })

    expect(loadStoreStockMovements('demo-fashion')[0]).toMatchObject({
      productId: 1001,
      quantityChange: -1,
      stockAfter: 3
    })
  })

  it('maps demo payment methods and masks card numbers safely', () => {
    expect(paymentStatusForMethod('CARD')).toBe('PAID')
    expect(paymentStatusForMethod('COD')).toBe('PENDING')
    expect(maskCardNumber('4242 4242 4242 4242')).toBe('**** 4242')
  })
})
