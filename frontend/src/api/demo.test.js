import { describe, expect, it } from 'vitest'
import { demoAdapter } from './demo'

const call = async (config) => (await demoAdapter(config)).data.data

describe('demo adapter', () => {
  it('logs in a demo account and returns a token + user', async () => {
    const res = await call({
      method: 'post',
      url: '/auth/login',
      data: JSON.stringify({ email: 'ava@renova.local', password: 'anything' })
    })
    expect(res.token).toMatch(/^demo\./)
    expect(res.user.email).toBe('ava@renova.local')
    expect(res.user.displayName).toBeTruthy()
  })

  it('resolves the current user from the bearer token', async () => {
    const res = await call({ method: 'get', url: '/auth/me', headers: { Authorization: 'Bearer demo.2' } })
    expect(res.id).toBe(2)
    expect(res.email).toBe('liam@renova.local')
  })

  it('returns seeded categories', async () => {
    const res = await call({ method: 'get', url: '/public/categories' })
    expect(Array.isArray(res)).toBe(true)
    expect(res.length).toBeGreaterThan(0)
    expect(res[0]).toHaveProperty('icon')
  })

  it('returns a paginated listing search', async () => {
    const res = await call({ method: 'get', url: '/public/listings', params: { page: 0, size: 12 } })
    expect(res.content.length).toBeGreaterThan(0)
    expect(res.content[0]).toMatchObject({ id: expect.any(Number), title: expect.any(String) })
    expect(res.content[0].seller).toHaveProperty('displayName')
    expect(res).toHaveProperty('totalElements')
  })

  it('filters listings by category', async () => {
    const res = await call({ method: 'get', url: '/public/listings', params: { categoryId: 6 } })
    expect(res.content.every((l) => l.category.id === 6)).toBe(true)
  })

  it('returns a full listing detail', async () => {
    const res = await call({ method: 'get', url: '/public/listings/1' })
    expect(res.id).toBe(1)
    expect(res.description).toBeTruthy()
    expect(res.imageUrls.length).toBeGreaterThan(0)
  })

  it('toggles a favorite', async () => {
    const first = await call({ method: 'post', url: '/listings/3/favorite' })
    const second = await call({ method: 'post', url: '/listings/3/favorite' })
    expect(first.favorited).not.toBe(second.favorited)
  })

  it('rejects an unknown listing like the real API', async () => {
    await expect(call({ method: 'get', url: '/public/listings/9999' })).rejects.toMatchObject({
      response: { status: 404 }
    })
  })
})
