import { beforeEach, describe, expect, it, vi } from 'vitest'

const { client } = vi.hoisted(() => ({
  client: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

vi.mock('./client', () => ({
  default: client,
  unwrap: (response) => response.data?.data ?? response.data
}))

import {
  authApi,
  categoryApi,
  conversationApi,
  listingApi,
  offerApi,
  orderApi,
  reviewApi,
  userApi
} from './endpoints'

const apiResponse = { data: { data: { ok: true } } }

beforeEach(() => {
  vi.clearAllMocks()
  Object.values(client).forEach((method) => method.mockResolvedValue(apiResponse))
})

describe('endpoint contracts', () => {
  it('maps authentication and catalog reads to real backend routes', async () => {
    await expect(authApi.login({ email: 'ava@renova.local' })).resolves.toEqual({ ok: true })
    expect(client.post).toHaveBeenCalledWith('/auth/login', { email: 'ava@renova.local' })

    await categoryApi.list()
    expect(client.get).toHaveBeenCalledWith('/public/categories')

    await listingApi.search({ page: 0, size: 24, sort: 'newest' })
    expect(client.get).toHaveBeenCalledWith('/public/listings', {
      params: { page: 0, size: 24, sort: 'newest' }
    })
  })

  it('keeps listing mutations behind authenticated API calls', async () => {
    await listingApi.create({ title: 'Camera' })
    expect(client.post).toHaveBeenCalledWith('/listings', { title: 'Camera' })

    await listingApi.update(7, { price: 120 })
    expect(client.put).toHaveBeenCalledWith('/listings/7', { price: 120 })

    await listingApi.remove(7)
    expect(client.delete).toHaveBeenCalledWith('/listings/7')
  })

  it('covers trading workflows without local mock fallbacks', async () => {
    await offerApi.counter(5, { amount: 25 })
    expect(client.post).toHaveBeenCalledWith('/offers/5/counter', { amount: 25 })

    await conversationApi.send(9, { body: 'Still available?' })
    expect(client.post).toHaveBeenCalledWith('/conversations/9/messages', { body: 'Still available?' })

    await orderApi.ship(3, { carrier: 'UPS', trackingNumber: '1Z' })
    expect(client.post).toHaveBeenCalledWith('/orders/3/ship', { carrier: 'UPS', trackingNumber: '1Z' })
  })

  it('maps public profile and review routes explicitly', async () => {
    await userApi.publicListings(4, { page: 0, size: 4 })
    expect(client.get).toHaveBeenCalledWith('/public/users/4/listings', { params: { page: 0, size: 4 } })

    await reviewApi.forUser(4)
    expect(client.get).toHaveBeenCalledWith('/public/users/4/reviews')
  })
})
