import { beforeEach, describe, expect, it, vi } from 'vitest'
import { categoryApi, listingApi } from '../api/endpoints'
import { useHomeFeed } from './useHomeFeed'

vi.mock('../api/endpoints', () => ({ categoryApi: { list: vi.fn() }, listingApi: { search: vi.fn() } }))

beforeEach(() => vi.resetAllMocks())

describe('homepage feed', () => {
  it('keeps successful listings when categories fail', async () => {
    categoryApi.list.mockRejectedValue(new Error('offline'))
    listingApi.search.mockResolvedValue({ content: [{ id: 7 }] })
    const feed = useHomeFeed()
    await feed.load()
    expect(feed.listings.value).toEqual([{ id: 7 }])
    expect(feed.listingsError.value).toBe(false)
    expect(feed.categoriesError.value).toBe(true)
  })

  it('distinguishes failed listings from a successful empty feed and recovers on retry', async () => {
    listingApi.search.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({ content: [] })
    const feed = useHomeFeed()
    await feed.loadListings()
    expect(feed.listingsError.value).toBe(true)
    expect(feed.listingsLoading.value).toBe(false)
    await feed.loadListings()
    expect(feed.listingsError.value).toBe(false)
    expect(feed.listings.value).toEqual([])
  })

  it('shows loading and prevents concurrent retries', async () => {
    let resolve
    listingApi.search.mockReturnValue(new Promise((done) => { resolve = done }))
    const feed = useHomeFeed()
    const pending = feed.loadListings()
    await feed.loadListings()
    expect(feed.listingsLoading.value).toBe(true)
    expect(listingApi.search).toHaveBeenCalledTimes(1)
    resolve({ content: [{ id: 2 }] })
    await pending
    expect(feed.listingsLoading.value).toBe(false)
    expect(feed.listings.value[0].id).toBe(2)
  })
})
