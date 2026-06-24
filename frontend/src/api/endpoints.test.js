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
  mediaApi,
  offerApi,
  orderApi,
  reviewApi,
  userApi
} from './endpoints'

const apiResponse = { data: { data: { ok: true } } }

beforeEach(() => {
  vi.clearAllMocks()
  Object.values(client).forEach((method) => method.mockResolvedValue(apiResponse))
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true }))
})

describe('endpoint contracts', () => {
  it('maps authentication and catalog reads to real backend routes', async () => {
    await authApi.csrf()
    expect(client.get).toHaveBeenCalledWith('/auth/csrf')

    await expect(authApi.login({ email: 'ava@renova.local' })).resolves.toEqual({ ok: true })
    expect(client.post).toHaveBeenCalledWith('/auth/login', { email: 'ava@renova.local' })

    await authApi.logout()
    expect(client.post).toHaveBeenCalledWith('/auth/logout')

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

  it('uploads listing images through a server-issued storage intent', async () => {
    const file = new File(['image-bytes'], 'chair.png', { type: 'image/png' })
    client.post
      .mockResolvedValueOnce({ data: { data: {
        mediaId: 44,
        uploadUrl: 'https://storage.example/upload',
        requiredHeaders: { 'Content-Type': 'image/png' }
      } } })
      .mockResolvedValueOnce({ data: { data: { id: 44, status: 'READY' } } })

    await expect(mediaApi.upload(file)).resolves.toEqual({ id: 44, status: 'READY' })
    expect(client.post).toHaveBeenNthCalledWith(1, '/media/upload-intents', {
      fileName: 'chair.png',
      contentType: 'image/png',
      sizeBytes: file.size
    })
    expect(fetch).toHaveBeenCalledWith('https://storage.example/upload', {
      method: 'PUT',
      headers: { 'Content-Type': 'image/png' },
      body: file
    })
    expect(client.post).toHaveBeenNthCalledWith(2, '/media/44/complete')
  })

  it('covers trading workflows without local mock fallbacks', async () => {
    await offerApi.counter(5, { amount: 25 })
    expect(client.post).toHaveBeenCalledWith('/offers/5/counter', { amount: 25 })

    await conversationApi.send(9, { body: 'Still available?' })
    expect(client.post).toHaveBeenCalledWith('/conversations/9/messages', { body: 'Still available?' })

    await offerApi.get(5)
    expect(client.get).toHaveBeenCalledWith('/offers/5')

    await orderApi.create({ listingId: 3 }, '6ba7b810-9dad-41d1-80b4-00c04fd430c8')
    expect(client.post).toHaveBeenCalledWith('/orders', { listingId: 3 }, {
      headers: { 'Idempotency-Key': '6ba7b810-9dad-41d1-80b4-00c04fd430c8' }
    })

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
