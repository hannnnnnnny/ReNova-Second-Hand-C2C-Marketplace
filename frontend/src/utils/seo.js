const DEFAULT_TITLE = 'NovaCart Ecommerce Website Builder'
const DEFAULT_DESCRIPTION = 'NovaCart is a multi-merchant ecommerce website builder for launching storefronts, managing products, orders, inventory, promotions, support, refunds, and analytics.'

const PLATFORM_SEO = {
  'platform-home': {
    title: 'NovaCart | Multi-Merchant Ecommerce Website Builder',
    description: DEFAULT_DESCRIPTION
  },
  'platform-features': {
    title: 'NovaCart Features | Storefronts, Orders, Inventory, Support',
    description: 'Explore NovaCart features for merchant storefront setup, catalog management, promotions, analytics, support queues, refunds, and safe demo checkout.'
  },
  'platform-templates': {
    title: 'NovaCart Templates | Premium Storefront Starting Points',
    description: 'Browse original NovaCart storefront templates for fashion boutiques, sports retailers, home goods, thrift shops, and minimal merchant catalogs.'
  },
  'platform-template-detail': {
    title: 'NovaCart Template Preview | Storefront Page Gallery',
    description: 'Review NovaCart template page previews across home, product listing, product detail, cart, checkout, order tracking, support, and merchant operations screens.'
  },
  'platform-pricing': {
    title: 'NovaCart Pricing | Demo Ecommerce SaaS Plans',
    description: 'Review NovaCart demo pricing tiers for portfolio storefront building, promotion management, inventory alerts, analytics, and merchant operations.'
  },
  'merchant-signup': {
    title: 'Create a NovaCart Merchant Workspace',
    description: 'Start a NovaCart merchant workspace and prepare a branded ecommerce storefront with products, templates, and store setup steps.'
  },
  'merchant-onboarding': {
    title: 'NovaCart Onboarding | Build a Merchant Storefront',
    description: 'Configure store basics, choose a template, add first products, and preview a generated NovaCart storefront.'
  },
  'merchant-login': {
    title: 'NovaCart Merchant Login',
    description: 'Sign in to the NovaCart merchant operations workspace for catalog, orders, inventory, analytics, support, and refunds.'
  }
}

const ADMIN_SEO = {
  'admin-dashboard': 'Merchant Dashboard',
  'admin-store-setup': 'Store Setup',
  'admin-products': 'Product Management',
  'admin-product-new': 'New Product',
  'admin-product-edit': 'Edit Product',
  'admin-categories': 'Category Management',
  'admin-collections': 'Collection Management',
  'admin-promotions': 'Promotion Management',
  'admin-orders': 'Order Management',
  'admin-order-detail': 'Order Detail',
  'admin-inventory': 'Inventory Management',
  'admin-support': 'Support Queue',
  'admin-refunds': 'Refund Queue',
  'admin-customers': 'Customer Records',
  'admin-analytics': 'Analytics',
  'admin-templates': 'Template Management',
  'admin-theme-editor': 'Theme Editor',
  'admin-settings': 'Settings'
}

export function resolveRouteSeo(to, getStore = () => null) {
  const routeName = String(to?.name || '')
  const canonicalPath = routePath(to)

  if (PLATFORM_SEO[routeName]) {
    return {
      ...PLATFORM_SEO[routeName],
      canonicalPath,
      jsonLd: websiteJsonLd()
    }
  }

  if (ADMIN_SEO[routeName]) {
    return {
      title: `${ADMIN_SEO[routeName]} | NovaCart Admin`,
      description: `NovaCart admin workspace for ${ADMIN_SEO[routeName].toLowerCase()} in a multi-merchant ecommerce platform demo.`,
      canonicalPath,
      jsonLd: softwareJsonLd()
    }
  }

  const storeSlug = String(to?.params?.storeSlug || '')
  const store = storeSlug ? getStore(storeSlug) : null
  if (store) {
    const product = to?.params?.productId
      ? store.products?.find((entry) => String(entry.id) === String(to.params.productId))
      : null
    if (product) {
      return {
        title: `${product.name} | ${store.name}`,
        description: product.description || `Shop ${product.name} from ${store.name}, a generated NovaCart merchant storefront.`,
        canonicalPath,
        jsonLd: productJsonLd(store, product, canonicalPath)
      }
    }

    const storeRouteLabel = routeName === 'merchant-store-products'
      ? 'Products'
      : routeName === 'merchant-store-cart'
        ? 'Cart'
        : routeName === 'merchant-store-checkout'
          ? 'Checkout'
          : routeName === 'merchant-store-support'
            ? 'Support and Refunds'
            : 'Storefront'

    return {
      title: `${store.name} ${storeRouteLabel} | NovaCart Storefront`,
      description: store.description || `Shop ${store.name}, a generated merchant storefront built with NovaCart.`,
      canonicalPath,
      jsonLd: storeJsonLd(store, canonicalPath)
    }
  }

  return {
    title: routeName === 'not-found' ? 'Page Not Found | NovaCart' : DEFAULT_TITLE,
    description: DEFAULT_DESCRIPTION,
    canonicalPath,
    jsonLd: websiteJsonLd()
  }
}

export function applyRouteSeo(to, getStore = () => null) {
  if (typeof document === 'undefined') return

  const seo = resolveRouteSeo(to, getStore)
  const canonicalUrl = absoluteUrl(seo.canonicalPath)
  document.title = seo.title
  setMeta('name', 'description', seo.description)
  setMeta('name', 'robots', 'index,follow')
  setMeta('property', 'og:title', seo.title)
  setMeta('property', 'og:description', seo.description)
  setMeta('property', 'og:url', canonicalUrl)
  setMeta('name', 'twitter:title', seo.title)
  setMeta('name', 'twitter:description', seo.description)
  setCanonical(canonicalUrl)
  setJsonLd(seo.jsonLd, canonicalUrl)
}

function routePath(to) {
  return String(to?.path || '/').split('?')[0] || '/'
}

function websiteJsonLd() {
  return {
    '@context': 'https://schema.org',
    '@type': 'WebSite',
    name: 'NovaCart',
    description: DEFAULT_DESCRIPTION
  }
}

function softwareJsonLd() {
  return {
    '@context': 'https://schema.org',
    '@type': 'SoftwareApplication',
    name: 'NovaCart',
    applicationCategory: 'BusinessApplication',
    operatingSystem: 'Web',
    description: DEFAULT_DESCRIPTION
  }
}

function storeJsonLd(store, path) {
  return {
    '@context': 'https://schema.org',
    '@type': 'OnlineStore',
    name: store.name,
    description: store.description,
    url: path,
    brand: {
      '@type': 'Brand',
      name: store.name
    }
  }
}

function productJsonLd(store, product, path) {
  const productData = {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: product.name,
    description: product.description,
    image: product.imageUrl,
    brand: {
      '@type': 'Brand',
      name: store.name
    },
    offers: {
      '@type': 'Offer',
      priceCurrency: store.currency || 'USD',
      price: String(product.price),
      availability: product.stockQuantity > 0 ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock',
      url: path
    }
  }

  if (product.rating && product.reviewCount) {
    productData.aggregateRating = {
      '@type': 'AggregateRating',
      ratingValue: String(product.rating),
      reviewCount: String(product.reviewCount)
    }
  }

  return productData
}

function absoluteUrl(path) {
  if (typeof window === 'undefined') return path
  return new URL(path, window.location.origin).toString()
}

function setMeta(attribute, key, content) {
  let element = document.head.querySelector(`meta[${attribute}="${key}"]`)
  if (!element) {
    element = document.createElement('meta')
    element.setAttribute(attribute, key)
    document.head.appendChild(element)
  }
  element.setAttribute('content', content)
}

function setCanonical(url) {
  let element = document.head.querySelector('link[rel="canonical"]')
  if (!element) {
    element = document.createElement('link')
    element.setAttribute('rel', 'canonical')
    document.head.appendChild(element)
  }
  element.setAttribute('href', url)
}

function setJsonLd(jsonLd, canonicalUrl) {
  let element = document.head.querySelector('script[data-novacart-route-jsonld="true"]')
  if (!element) {
    element = document.createElement('script')
    element.type = 'application/ld+json'
    element.setAttribute('data-novacart-route-jsonld', 'true')
    document.head.appendChild(element)
  }
  element.textContent = JSON.stringify({
    ...jsonLd,
    url: canonicalUrl
  })
}
