import { describe, expect, it } from 'vitest'
import { resolveRouteSeo } from './seo'

const demoStore = {
  name: 'Avery Studio',
  slug: 'demo-fashion',
  currency: 'USD',
  description: 'Independent fashion boutique for quiet tailoring.',
  products: [
    {
      id: 1001,
      name: 'Ivory Collarless Blazer',
      description: 'Soft structured blazer with clean weekday polish.',
      imageUrl: '/demo-images/products/fashion-blazer.jpg',
      price: 128,
      stockQuantity: 14,
      rating: 4.7,
      reviewCount: 42
    }
  ]
}

describe('SEO utilities', () => {
  it('returns product metadata and product structured data for generated storefront products', () => {
    const seo = resolveRouteSeo(
      {
        name: 'merchant-store-product-detail',
        path: '/store/demo-fashion/products/1001',
        params: { storeSlug: 'demo-fashion', productId: '1001' }
      },
      () => demoStore
    )

    expect(seo.title).toBe('Ivory Collarless Blazer | Avery Studio')
    expect(seo.description).toContain('Soft structured blazer')
    expect(seo.jsonLd).toMatchObject({
      '@type': 'Product',
      name: 'Ivory Collarless Blazer',
      offers: {
        '@type': 'Offer',
        priceCurrency: 'USD',
        availability: 'https://schema.org/InStock'
      },
      aggregateRating: {
        '@type': 'AggregateRating',
        ratingValue: '4.7',
        reviewCount: '42'
      }
    })
  })

  it('returns specific metadata for platform pages', () => {
    const seo = resolveRouteSeo({ name: 'platform-templates', path: '/templates', params: {} })

    expect(seo.title).toContain('NovaCart Templates')
    expect(seo.description).toContain('storefront templates')
    expect(seo.jsonLd['@type']).toBe('WebSite')
  })
})
