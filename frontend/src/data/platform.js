import { publicAsset } from '../utils/publicPath'

const heroPath = (name) => publicAsset(`demo-images/heroes/${name}.jpg`)
const productPath = (name) => publicAsset(`demo-images/products/${name}.jpg`)

export const storeTemplates = [
  {
    id: 'fashion',
    name: 'Fashion Boutique',
    description: 'Editorial merchandising for apparel, bags, jewelry, footwear, and seasonal edits.',
    bestFor: 'Fashion labels, thrift boutiques, accessory shops',
    previewImage: heroPath('fashion'),
    accentColor: '#2f5d50',
    fontStyle: 'Clean retail sans',
    layoutName: 'Editorial launch',
    focus: 'Lookbook hero, seasonal edits, and polished outfit-first merchandising.',
    primaryGoal: 'Help shoppers understand the collection mood before they browse.',
    homepageModules: ['Large visual hero', 'Editorial category rail', 'Featured outfit edit'],
    easyEdits: ['Hero image and headline', 'Collection description', 'Featured products'],
    trafficReadiness: 'Static preview pages, guarded forms, and checkout idempotency for high-click launches.',
    wireframe: ['Hero', 'Edit rail', 'Products']
  },
  {
    id: 'thrift',
    name: 'Thrift Classic',
    description: 'A warm discovery-led layout for curated vintage, rare finds, and mixed lifestyle goods.',
    bestFor: 'Secondhand stores, charity shops, vintage sellers',
    previewImage: heroPath('boutique'),
    accentColor: '#6d6239',
    fontStyle: 'Classic sans',
    layoutName: 'Discovery drop',
    focus: 'One-off finds, story-led product notes, and fast scanning for rotating stock.',
    primaryGoal: 'Make every product feel findable even when inventory changes every week.',
    homepageModules: ['Drop announcement', 'Condition-aware product list', 'Quick category filters'],
    easyEdits: ['Weekly drop message', 'Find categories', 'One-off product cards'],
    trafficReadiness: 'Duplicate-submit guards keep high-click drops from creating repeated actions.',
    wireframe: ['Drop', 'Filters', 'One-offs']
  },
  {
    id: 'sports',
    name: 'Sports Gear',
    description: 'Fast shopping paths for activewear, equipment, accessories, and club-ready collections.',
    bestFor: 'Sports retailers, active lifestyle brands',
    previewImage: heroPath('sports'),
    accentColor: '#285f83',
    fontStyle: 'Modern condensed',
    layoutName: 'Performance shop',
    focus: 'Speed, stock confidence, training categories, and equipment-first buying paths.',
    primaryGoal: 'Get repeat shoppers to the right gear with the fewest taps.',
    homepageModules: ['Performance hero', 'Training category shortcuts', 'Stock and shipping proof'],
    easyEdits: ['Hero action copy', 'Sport categories', 'Low-stock products'],
    trafficReadiness: 'Optimized image loading and locked submit buttons reduce wasted requests.',
    wireframe: ['Action', 'Shortcuts', 'Stock']
  },
  {
    id: 'home',
    name: 'Home Living',
    description: 'Quiet merchandising for home goods, gifts, daily essentials, and lifestyle collections.',
    bestFor: 'Homeware, lifestyle, gift stores',
    previewImage: heroPath('home'),
    accentColor: '#59694f',
    fontStyle: 'Warm minimal',
    layoutName: 'Room story',
    focus: 'Room-by-room browsing, gifting cues, and calm product detail for home shoppers.',
    primaryGoal: 'Help shoppers imagine the product inside daily routines.',
    homepageModules: ['Room hero', 'Giftable category tiles', 'Home ritual product row'],
    easyEdits: ['Room background image', 'Gift description', 'Home categories'],
    trafficReadiness: 'Mostly static storefront pages stay responsive during traffic spikes.',
    wireframe: ['Room', 'Gifts', 'Rituals']
  },
  {
    id: 'minimal',
    name: 'Minimal Modern',
    description: 'A clean catalog-first template for small merchants that need speed, clarity, and trust.',
    bestFor: 'New stores, digital-first boutiques',
    previewImage: heroPath('boutique'),
    accentColor: '#343a35',
    fontStyle: 'System clean',
    layoutName: 'Catalog first',
    focus: 'Simple navigation, direct product discovery, and trust signals for first-time merchants.',
    primaryGoal: 'Publish a clear store quickly without visual noise or complex setup.',
    homepageModules: ['Compact hero', 'Trust strip', 'Fast product grid'],
    easyEdits: ['Store name', 'Short description', 'Starter catalog'],
    trafficReadiness: 'Lean layout and guarded public forms keep first launches stable.',
    wireframe: ['Intro', 'Trust', 'Grid']
  }
]

export const platformFeatures = [
  ['Store builder', 'Create a branded storefront with templates, theme settings, and live previews.'],
  ['Product management', 'Add products, variants, images, categories, stock, and sale pricing from one workspace.'],
  ['Order management', 'Review payments, fulfillment states, customer details, and refund context.'],
  ['Inventory tracking', 'Watch low stock, adjust counts, and keep checkout quantities honest.'],
  ['Promotions and discounts', 'Create product, category, collection, season, or tag-based markdowns.'],
  ['Analytics dashboard', 'Track sales, orders, conversion signals, top products, and customer regions.'],
  ['Customer support tools', 'Manage support tickets, refund requests, and internal merchant notes.']
]

export const workflowSteps = [
  ['Create account', 'Start a merchant workspace for your brand.'],
  ['Pick a template', 'Choose a store design that fits your catalog.'],
  ['Add products', 'Load your first products, pricing, and inventory.'],
  ['Publish store', 'Preview the customer storefront before going live.'],
  ['Track performance', 'Use analytics, promotions, and care tools to improve.']
]

export const pricingPlans = [
  {
    name: 'Starter',
    price: '$0 demo',
    description: 'For portfolio demos and first store setup.',
    features: ['One store workspace', 'Template preview', 'Products and orders', 'Demo checkout']
  },
  {
    name: 'Growth',
    price: '$29 demo',
    description: 'For merchants ready to run campaigns.',
    features: ['Multiple templates', 'Promotions', 'Inventory alerts', 'Support queue']
  },
  {
    name: 'Pro',
    price: '$79 demo',
    description: 'For teams that need deeper operations.',
    features: ['Advanced analytics', 'Multiple staff seats', 'Theme editor', 'Priority roadmap features']
  }
]

export const demoStores = [
  {
    id: 'store-fashion',
    merchantName: 'Avery Studio',
    name: 'Avery Studio',
    slug: 'demo-fashion',
    category: 'Fashion',
    description: 'Independent fashion boutique for quiet tailoring, bags, jewelry, shoes, and seasonal wardrobe pieces.',
    template: 'fashion',
    brandColor: '#2f5d50',
    logoText: 'AS',
    currency: 'USD',
    shippingMessage: 'Free shipping on orders over $75',
    announcement: 'Spring edit now live. Free shipping on orders over $75.',
    heroTitle: 'Classic pieces for everyday polish',
    heroText: 'Tailored layers, clean accessories, and event-ready details from Avery Studio.',
    heroImage: heroPath('fashion'),
    published: true,
    setup: completeSetup(),
    categories: ['New Arrivals', 'Women', 'Bags', 'Jewelry', 'Shoes', 'Sale'],
    products: [
      product(1001, 'Ivory Collarless Blazer', 'ivory-collarless-blazer', 'Women', 128, 168, 14, productPath('fashion-blazer'), ['Sale'], 'Soft structured blazer with a clean neckline and relaxed weekday polish.'),
      product(1002, 'Sculpted Day Bag', 'sculpted-day-bag', 'Bags', 96, null, 8, productPath('fashion-bag'), ['Best Seller'], 'Compact structured bag with a top handle and removable strap.'),
      product(1003, 'Pearl Drop Earring Set', 'pearl-drop-earring-set', 'Jewelry', 42, 58, 5, productPath('fashion-earrings'), ['Sale', 'Low Stock'], 'Lightweight pearl drops for everyday styling and evening detail.'),
      product(1004, 'Sand Knit Midi Dress', 'sand-knit-midi-dress', 'Women', 88, null, 18, productPath('fashion-dress'), ['New'], 'Soft knit midi dress with easy drape and a minimal silhouette.'),
      product(1005, 'Low Profile Leather Sneaker', 'low-profile-leather-sneaker', 'Shoes', 112, 140, 11, productPath('fashion-sneaker'), ['Sale'], 'Clean leather sneakers with a cushioned footbed for all-day wear.'),
      product(1006, 'Black Satin Evening Clutch', 'black-satin-evening-clutch', 'Bags', 64, null, 4, productPath('fashion-clutch'), ['Low Stock'], 'Slim satin clutch with a quiet shine and interior card pocket.')
    ],
    analytics: analytics(18420, 214, 5620, '3.8%', 86.07, ['Ivory Collarless Blazer', 'Sculpted Day Bag', 'Pearl Drop Earring Set'])
  },
  {
    id: 'store-thrift',
    merchantName: 'Found Loop',
    name: 'Found Loop',
    slug: 'demo-thrift',
    category: 'Thrift and vintage',
    description: 'Curated secondhand layers, small objects, and one-off accessories refreshed for weekly discovery.',
    template: 'thrift',
    brandColor: '#6d6239',
    logoText: 'FL',
    currency: 'USD',
    shippingMessage: 'Flat $6 shipping, free over $70',
    announcement: 'Friday vintage drop is live with one-off finds and small home pieces.',
    heroTitle: 'One-off finds with a story',
    heroText: 'Vintage layers, useful objects, and carefully checked secondhand goods from Found Loop.',
    heroImage: heroPath('boutique'),
    published: true,
    setup: completeSetup(),
    categories: ['New Finds', 'Vintage Apparel', 'Bags', 'Objects', 'Accessories', 'Sale'],
    products: [
      product(5001, 'Vintage Cotton Chore Jacket', 'vintage-cotton-chore-jacket', 'Vintage Apparel', 74, null, 1, productPath('fashion-blazer'), ['One-off', 'Low Stock'], 'Softly worn chore jacket with clean seams, roomy pockets, and one available piece.'),
      product(5002, 'Market Woven Shoulder Bag', 'market-woven-shoulder-bag', 'Bags', 46, null, 2, productPath('fashion-bag'), ['One-off'], 'Textured shoulder bag selected for everyday errands and relaxed weekend styling.'),
      product(5003, 'Found Ceramic Bud Vase', 'found-ceramic-bud-vase', 'Objects', 28, null, 3, productPath('boutique-vase'), ['New'], 'Small ceramic vase with gentle surface character and shelf-ready proportions.'),
      product(5004, 'Soft Washed Utility Shirt', 'soft-washed-utility-shirt', 'Vintage Apparel', 38, 52, 4, productPath('boutique-shirt'), ['Sale'], 'Washed utility shirt with a relaxed collar and easy layered shape.'),
      product(5005, 'Slim Leather Card Wallet', 'slim-leather-card-wallet', 'Accessories', 32, null, 2, productPath('boutique-wallet'), ['One-off'], 'Compact card wallet with a clean fold and visible vintage grain.'),
      product(5006, 'Evening Satin Pouch', 'evening-satin-pouch', 'Bags', 36, null, 1, productPath('fashion-clutch'), ['Low Stock'], 'Small satin pouch for evening styling, checked and ready for a second life.')
    ],
    analytics: analytics(6240, 89, 3210, '2.8%', 70.11, ['Vintage Cotton Chore Jacket', 'Market Woven Shoulder Bag', 'Found Ceramic Bud Vase'])
  },
  {
    id: 'store-sports',
    merchantName: 'Northline Active',
    name: 'Northline Active',
    slug: 'demo-sports',
    category: 'Sports',
    description: 'Performance apparel, training extras, and compact sports equipment for active weekends.',
    template: 'sports',
    brandColor: '#285f83',
    logoText: 'NA',
    currency: 'USD',
    shippingMessage: 'Free shipping over $90',
    announcement: 'Training kits, equipment, and active layers ready for the weekend.',
    heroTitle: 'Gear that keeps moving',
    heroText: 'Activewear, sport accessories, and training essentials built for everyday sessions.',
    heroImage: heroPath('sports'),
    published: true,
    setup: completeSetup(),
    categories: ['New Arrivals', 'Activewear', 'Equipment', 'Bags', 'Shoes', 'Sale'],
    products: [
      product(2001, 'Pace Training Jacket', 'pace-training-jacket', 'Activewear', 84, null, 21, productPath('sports-jacket'), ['New'], 'Breathable training layer with zip pockets and a lightweight shell.'),
      product(2002, 'Compact Gym Duffel', 'compact-gym-duffel', 'Bags', 58, 72, 9, productPath('sports-duffel'), ['Sale'], 'Compact duffel with separate shoe storage and reinforced handles.'),
      product(2003, 'Grip Court Sneaker', 'grip-court-sneaker', 'Shoes', 118, null, 13, productPath('sports-sneaker'), ['Best Seller'], 'Court-ready sneakers with stable grip and everyday comfort.'),
      product(2004, 'Balance Mat Set', 'balance-mat-set', 'Equipment', 46, null, 6, productPath('sports-mat'), ['Low Stock'], 'Portable training mat with carry strap and wipe-clean surface.'),
      product(2005, 'Core Run Short', 'core-run-short', 'Activewear', 42, 54, 17, productPath('sports-shorts'), ['Sale'], 'Quick-dry running shorts with a secure inner pocket.'),
      product(2006, 'Resistance Band Kit', 'resistance-band-kit', 'Equipment', 32, null, 28, productPath('sports-bands'), ['New'], 'Five-piece resistance kit for home, travel, and warmups.')
    ],
    analytics: analytics(11980, 148, 4380, '3.4%', 80.94, ['Grip Court Sneaker', 'Pace Training Jacket', 'Compact Gym Duffel'])
  },
  {
    id: 'store-home',
    merchantName: 'Harbor Home',
    name: 'Harbor Home',
    slug: 'demo-home',
    category: 'Home goods',
    description: 'Warm home pieces, thoughtful gifts, and minimal lifestyle goods for calm daily routines.',
    template: 'home',
    brandColor: '#59694f',
    logoText: 'HH',
    currency: 'USD',
    shippingMessage: 'Free shipping on orders over $60',
    announcement: 'New home living pieces for quieter everyday rituals.',
    heroTitle: 'Useful goods for softer daily spaces',
    heroText: 'Homewares, gifting pieces, and quiet details for calm routines.',
    heroImage: heroPath('home'),
    published: true,
    setup: completeSetup(),
    categories: ['New Arrivals', 'Home Living', 'Kitchen', 'Gifts', 'Textiles', 'Sale'],
    products: [
      product(3001, 'Linen Table Runner', 'linen-table-runner', 'Textiles', 48, null, 16, productPath('home-runner'), ['New'], 'Washed linen runner for simple dining tables and layered styling.'),
      product(3002, 'Stoneware Pourer', 'stoneware-pourer', 'Kitchen', 34, null, 10, productPath('home-pourer'), ['Best Seller'], 'Small stoneware pourer for sauces, cream, or countertop styling.'),
      product(3003, 'Soft Cotton Throw', 'soft-cotton-throw', 'Home Living', 72, 92, 7, productPath('home-throw'), ['Sale'], 'Breathable cotton throw with a subtle ribbed texture.'),
      product(3004, 'Cedar Drawer Sachet Set', 'cedar-drawer-sachet-set', 'Gifts', 22, null, 32, productPath('home-sachets'), ['Gift'], 'Cedar sachet set for drawers, wardrobes, and packaged gifts.'),
      product(3005, 'Minimal Brass Hook', 'minimal-brass-hook', 'Home Living', 18, null, 5, productPath('home-hook'), ['Low Stock'], 'Compact brass wall hook for entryways, closets, and small spaces.'),
      product(3006, 'Ceramic Tea Cup Pair', 'ceramic-tea-cup-pair', 'Kitchen', 38, 46, 12, productPath('home-cups'), ['Sale'], 'Pair of handleless ceramic tea cups with a warm matte glaze.')
    ],
    analytics: analytics(7420, 96, 2980, '3.2%', 77.29, ['Soft Cotton Throw', 'Stoneware Pourer', 'Linen Table Runner'])
  },
  {
    id: 'store-boutique',
    merchantName: 'Luma Goods',
    name: 'Luma Goods',
    slug: 'demo-boutique',
    category: 'Minimal boutique',
    description: 'Small-batch wardrobe staples, simple objects, and refined accessories for a quiet premium shop.',
    template: 'minimal',
    brandColor: '#343a35',
    logoText: 'LG',
    currency: 'USD',
    shippingMessage: 'Free shipping on orders over $80',
    announcement: 'Minimal essentials, edited weekly.',
    heroTitle: 'Small collection, considered choices',
    heroText: 'A restrained boutique template for merchants who want clarity, calm, and polished catalog photography.',
    heroImage: heroPath('boutique'),
    published: true,
    setup: completeSetup(),
    categories: ['New Arrivals', 'Knitwear', 'Shoes', 'Objects', 'Accessories', 'Sale'],
    products: [
      product(4001, 'Cream Ribbed Sweater', 'cream-ribbed-sweater', 'Knitwear', 86, null, 18, productPath('boutique-sweater'), ['New'], 'Soft ribbed sweater with a clean neckline and easy drape.'),
      product(4002, 'Ivory Everyday Loafer', 'ivory-everyday-loafer', 'Shoes', 124, 148, 9, productPath('boutique-loafer'), ['Sale'], 'Minimal loafer with a flexible sole and polished profile.'),
      product(4003, 'Small Leather Wallet', 'small-leather-wallet', 'Accessories', 48, null, 15, productPath('boutique-wallet'), ['Best Seller'], 'Compact leather wallet with clean slots and a smooth finish.'),
      product(4004, 'Minimal Ring Set', 'minimal-ring-set', 'Accessories', 52, null, 6, productPath('boutique-rings'), ['Low Stock'], 'Stackable ring set with refined shine and everyday wearability.'),
      product(4005, 'White Ceramic Vase', 'white-ceramic-vase', 'Objects', 68, null, 11, productPath('boutique-vase'), ['New'], 'Matte ceramic vase sized for stems, shelves, and simple tables.'),
      product(4006, 'Soft Cotton Shirt', 'soft-cotton-shirt', 'New Arrivals', 74, null, 20, productPath('boutique-shirt'), ['New'], 'Crisp cotton shirt with a relaxed collar and quiet tailoring.')
    ],
    analytics: analytics(9830, 121, 3560, '3.4%', 81.24, ['Cream Ribbed Sweater', 'Small Leather Wallet', 'Ivory Everyday Loafer'])
  }
]

function product(id, name, slug, category, price, compareAtPrice, stockQuantity, imageUrl, badges, description) {
  const discountPercent = compareAtPrice
    ? Math.round(((Number(compareAtPrice) - Number(price)) / Number(compareAtPrice)) * 100)
    : 0
  const options = productOptionsForCategory(category)
  const rating = productRating(id, badges, stockQuantity)
  const reviewCount = productReviewCount(id, stockQuantity)
  return {
    id,
    name,
    slug,
    category,
    price,
    compareAtPrice,
    effectivePrice: price,
    discountPercent,
    stockQuantity,
    lowStockThreshold: 6,
    imageUrl,
    imageGallery: [imageUrl],
    sizes: options.sizes,
    colors: options.colors,
    material: options.material,
    careInstructions: options.careInstructions,
    rating,
    reviewCount,
    merchandisingLabel: merchandisingLabel({ badges, compareAtPrice, stockQuantity }),
    deliveryPromise: deliveryPromiseForCategory(category),
    reviewHighlights: reviewHighlightsForCategory(category),
    badges,
    status: stockQuantity > 0 ? 'ACTIVE' : 'ARCHIVED',
    description
  }
}

function productRating(id, badges = [], stockQuantity = 0) {
  const base = 4.35 + ((Number(id) % 7) * 0.07)
  const boost = badges.includes('Best Seller') ? 0.12 : badges.includes('New') ? 0.04 : 0
  const scarcityAdjustment = stockQuantity <= 6 ? -0.03 : 0
  return Number(Math.min(4.9, base + boost + scarcityAdjustment).toFixed(1))
}

function productReviewCount(id, stockQuantity = 0) {
  return 18 + ((Number(id) % 11) * 9) + Math.max(0, Math.min(32, Number(stockQuantity) || 0))
}

function merchandisingLabel({ badges = [], compareAtPrice, stockQuantity }) {
  if (compareAtPrice) return 'Limited markdown'
  if (badges.includes('Best Seller')) return 'Customer favorite'
  if (stockQuantity > 0 && stockQuantity <= 6) return 'Low stock watch'
  if (badges.includes('Gift')) return 'Giftable pick'
  if (badges.includes('New')) return 'New this week'
  return 'Core catalog'
}

function deliveryPromiseForCategory(category) {
  const key = String(category || '').toLowerCase()
  if (key === 'equipment' || key === 'home living' || key === 'kitchen') return 'Ships in 2-4 business days'
  if (key === 'objects' || key === 'gifts') return 'Gift-ready packing available'
  return 'Ships from merchant in 1-3 business days'
}

function reviewHighlightsForCategory(category) {
  const key = String(category || '').toLowerCase()
  if (key === 'shoes') return ['Comfortable for long wear', 'True-to-size fit notes']
  if (['bags', 'accessories', 'jewelry'].includes(key)) return ['Polished finish', 'Easy gifting choice']
  if (key === 'equipment' || key === 'activewear') return ['Reliable for regular sessions', 'Compact everyday storage']
  if (['home living', 'kitchen', 'textiles', 'objects', 'gifts'].includes(key)) return ['Feels considered in daily use', 'Looks refined on open shelves']
  return ['Soft hand feel', 'Easy to style with repeat outfits']
}

function productOptionsForCategory(category) {
  const key = String(category || '').toLowerCase()
  if (['women', 'knitwear', 'activewear', 'new arrivals'].includes(key)) {
    return {
      sizes: ['XS', 'S', 'M', 'L', 'XL'],
      colors: ['Ivory', 'Black', 'Taupe'],
      material: 'Responsibly sourced cotton blend',
      careInstructions: 'Machine wash cold, reshape, and lay flat to dry.'
    }
  }
  if (key === 'shoes') {
    return {
      sizes: ['6', '7', '8', '9', '10', '11'],
      colors: ['Ivory', 'Black', 'Sand'],
      material: 'Leather upper with cushioned footbed',
      careInstructions: 'Wipe clean with a soft cloth and store away from direct heat.'
    }
  }
  if (['bags', 'accessories', 'jewelry'].includes(key)) {
    return {
      sizes: [],
      colors: ['Black', 'Taupe', 'Wine'],
      material: key === 'jewelry' ? 'Polished brass and glass pearl finish' : 'Structured vegan leather',
      careInstructions: 'Store in the dust bag and avoid prolonged moisture.'
    }
  }
  if (key === 'equipment') {
    return {
      sizes: ['Standard'],
      colors: ['Slate', 'Pine'],
      material: 'Performance-grade mixed materials',
      careInstructions: 'Wipe clean after use and air dry before storage.'
    }
  }
  return {
    sizes: [],
    colors: ['Natural', 'Charcoal'],
    material: 'Small-batch mixed materials',
    careInstructions: 'Follow the merchant care card included with the order.'
  }
}

function completeSetup() {
  return {
    details: true,
    template: true,
    products: true,
    shipping: true,
    preview: true,
    publish: true
  }
}

function analytics(sales, orders, visitors, conversionRate, averageOrderValue, topProducts) {
  return {
    sales,
    orders,
    visitors,
    conversionRate,
    averageOrderValue,
    topProducts,
    trafficSources: ['Direct', 'Search', 'Social']
  }
}

export function getTemplateById(templateId) {
  return storeTemplates.find((template) => template.id === templateId) || storeTemplates[0]
}

export function createSlug(value) {
  return String(value || '')
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    || 'new-store'
}
