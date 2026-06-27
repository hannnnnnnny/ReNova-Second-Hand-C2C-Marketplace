/*
 * Front-end demo mode.
 *
 * When the app is published to GitHub Pages there is no backend, so every
 * API call would 404. This module provides an in-browser axios adapter that
 * serves seeded data and a fake auth flow, letting visitors actually browse
 * listings, "log in" with the demo accounts, and click through the UI with
 * no server. Enabled via VITE_RENOVA_DEMO=true at build time.
 */

const asset = (name) => `${import.meta.env.BASE_URL}demo-images/products/${name}`
const now = Date.now()
const daysAgo = (d) => new Date(now - d * 86400000).toISOString()

const users = {
  1: {
    id: 1,
    email: 'ava@renova.local',
    displayName: 'Ava Thompson',
    avatarUrl: null,
    location: 'Auckland',
    role: 'USER',
    bio: 'Pre-loved fashion and homeware curator. Everything cleaned and checked.',
    averageRating: 4.8,
    ratingCount: 23,
    memberSince: daysAgo(320)
  },
  2: {
    id: 2,
    email: 'liam@renova.local',
    displayName: 'Liam Carter',
    avatarUrl: null,
    location: 'Wellington',
    role: 'USER',
    bio: 'Sports gear and home odds and ends. Happy to bundle.',
    averageRating: 4.6,
    ratingCount: 15,
    memberSince: daysAgo(210)
  }
}

const summaryUser = (u) => ({
  id: u.id,
  email: u.email,
  displayName: u.displayName,
  avatarUrl: u.avatarUrl,
  location: u.location,
  role: u.role
})
const publicUser = (u) => ({
  id: u.id,
  displayName: u.displayName,
  avatarUrl: u.avatarUrl,
  bio: u.bio,
  location: u.location,
  averageRating: u.averageRating,
  ratingCount: u.ratingCount,
  memberSince: u.memberSince
})

const categories = [
  { id: 1, name: "Women's Fashion", slug: 'women', icon: '👗', sortOrder: 1 },
  { id: 2, name: "Men's Fashion", slug: 'men', icon: '👔', sortOrder: 2 },
  { id: 3, name: 'Shoes', slug: 'shoes', icon: '👟', sortOrder: 3 },
  { id: 4, name: 'Bags & Accessories', slug: 'bags', icon: '👜', sortOrder: 4 },
  { id: 5, name: 'Home', slug: 'home', icon: '🏠', sortOrder: 5 },
  { id: 6, name: 'Sports', slug: 'sports', icon: '🏀', sortOrder: 6 }
]
const categoryById = (id) => categories.find((c) => c.id === id) || categories[0]

const rawListings = [
  { id: 1, sellerId: 1, categoryId: 1, title: 'Silk Wrap Dress', img: 'fashion-dress.jpg', price: 68, originalPrice: 180, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Auckland', views: 142, favs: 18,
    desc: 'Elegant silk wrap dress, worn twice for events. No marks, freshly dry-cleaned. Size 10.' },
  { id: 2, sellerId: 1, categoryId: 4, title: 'Leather Tote Bag', img: 'fashion-bag.jpg', price: 95, originalPrice: 240, condition: 'GOOD', status: 'ACTIVE', loc: 'Auckland', views: 98, favs: 12,
    desc: 'Full-grain leather tote with a roomy interior. Light patina, all zips work perfectly.' },
  { id: 3, sellerId: 1, categoryId: 1, title: 'Tailored Wool Blazer', img: 'fashion-blazer.jpg', price: 54, originalPrice: 150, condition: 'GOOD', status: 'ACTIVE', loc: 'Auckland', views: 76, favs: 7,
    desc: 'Structured wool blazer, great for work. Minor wear on lining, exterior immaculate.' },
  { id: 4, sellerId: 1, categoryId: 4, title: 'Beaded Clutch', img: 'fashion-clutch.jpg', price: 28, originalPrice: 70, condition: 'LIKE_NEW', status: 'RESERVED', loc: 'Auckland', views: 51, favs: 9,
    desc: 'Hand-beaded evening clutch with chain strap. Stored in dust bag, like new.' },
  { id: 5, sellerId: 1, categoryId: 4, title: 'Gold Drop Earrings', img: 'fashion-earrings.jpg', price: 22, originalPrice: 55, condition: 'NEW', status: 'ACTIVE', loc: 'Auckland', views: 63, favs: 11,
    desc: 'Brand new gold-plated drop earrings, never worn. Hypoallergenic posts.' },
  { id: 6, sellerId: 2, categoryId: 3, title: 'Running Sneakers', img: 'sports-sneaker.jpg', price: 60, originalPrice: 160, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 120, favs: 14,
    desc: 'Cushioned running shoes, ~120km on them. Plenty of life left, cleaned and deodorised.' },
  { id: 7, sellerId: 2, categoryId: 6, title: 'Yoga Mat + Strap', img: 'sports-mat.jpg', price: 18, originalPrice: 45, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 44, favs: 5,
    desc: 'Non-slip 6mm yoga mat with carry strap. Wiped down, no tears.' },
  { id: 8, sellerId: 2, categoryId: 6, title: 'Weekender Duffel', img: 'sports-duffel.jpg', price: 35, originalPrice: 90, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Wellington', views: 58, favs: 6,
    desc: 'Water-resistant duffel, used on one trip. Shoe compartment and laptop sleeve.' },
  { id: 9, sellerId: 2, categoryId: 5, title: 'Ceramic Mug Set (4)', img: 'home-cups.jpg', price: 24, originalPrice: 60, condition: 'NEW', status: 'ACTIVE', loc: 'Wellington', views: 39, favs: 4,
    desc: 'Set of four handmade ceramic mugs, unused gift. Dishwasher safe.' },
  { id: 10, sellerId: 2, categoryId: 5, title: 'Woven Throw Blanket', img: 'home-throw.jpg', price: 30, originalPrice: 80, condition: 'GOOD', status: 'SOLD', loc: 'Wellington', views: 71, favs: 8,
    desc: 'Chunky knit throw, washed and cosy. Sold — kept for demo browsing.' }
]

const favorites = new Set([2, 5])

const toSummary = (l) => ({
  id: l.id,
  title: l.title,
  price: l.price,
  originalPrice: l.originalPrice,
  condition: l.condition,
  location: l.loc,
  negotiable: true,
  coverImageUrl: asset(l.img),
  status: l.status,
  viewCount: l.views,
  favoriteCount: l.favs,
  createdAt: daysAgo(l.id * 3),
  category: categoryById(l.categoryId),
  seller: publicUser(users[l.sellerId])
})
const toDetail = (l) => ({
  ...toSummary(l),
  description: l.desc,
  shippingFee: l.price > 60 ? 0 : 6.5,
  imageUrls: [asset(l.img)],
  updatedAt: daysAgo(l.id),
  favorited: favorites.has(l.id)
})

function paginate(items, params = {}) {
  const page = Number(params.page ?? 0)
  const size = Number(params.size ?? 12)
  const start = page * size
  const slice = items.slice(start, start + size)
  return {
    content: slice,
    page,
    size,
    totalElements: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / size)),
    first: page === 0,
    last: start + size >= items.length,
    empty: slice.length === 0
  }
}

function searchListings(params = {}) {
  let items = rawListings.filter((l) => l.status !== 'REMOVED')
  if (params.categoryId) items = items.filter((l) => l.categoryId === Number(params.categoryId))
  if (params.q) {
    const q = String(params.q).toLowerCase()
    items = items.filter((l) => l.title.toLowerCase().includes(q) || l.desc.toLowerCase().includes(q))
  }
  if (params.sort === 'price_asc') items = [...items].sort((a, b) => a.price - b.price)
  else if (params.sort === 'price_desc') items = [...items].sort((a, b) => b.price - a.price)
  else items = [...items].sort((a, b) => b.id - a.id)
  return paginate(items.map(toSummary), params)
}

function currentUserId(config) {
  const auth = config.headers?.Authorization || config.headers?.authorization || ''
  const m = /demo\.(\d+)/.exec(auth)
  return m ? Number(m[1]) : 1
}

const emptyPage = (params) => paginate([], params)

// Returns the unwrapped payload for a request, or throws { status } for 404.
function route(config) {
  const method = (config.method || 'get').toUpperCase()
  const path = (config.url || '').split('?')[0].replace(/\/$/, '')
  const params = config.params || {}
  const body = parseBody(config.data)
  const id = (re) => {
    const m = re.exec(path)
    return m ? Number(m[1]) : null
  }

  // ---- auth ----
  if (path === '/auth/login' && method === 'POST') {
    const u = Object.values(users).find((x) => x.email === (body.email || '').toLowerCase()) || users[1]
    return { token: `demo.${u.id}`, expiresInMinutes: 120, user: summaryUser(u) }
  }
  if (path === '/auth/signup' && method === 'POST') {
    const u = { ...users[1], email: body.email || 'guest@renova.local', displayName: body.displayName || 'Demo Guest' }
    return { token: 'demo.1', expiresInMinutes: 120, user: summaryUser(u) }
  }
  if (path === '/auth/me' || path === '/users/me') {
    return method === 'PUT' ? { ...summaryUser(users[currentUserId(config)]), ...body } : summaryUser(users[currentUserId(config)])
  }

  // ---- public catalogue ----
  if (path === '/public/categories') return categories
  if (path === '/public/listings') return searchListings(params)
  let m
  if ((m = /^\/public\/listings\/(\d+)$/.exec(path))) {
    const l = rawListings.find((x) => x.id === Number(m[1]))
    if (!l) throw { status: 404, message: 'Listing not found' }
    return toDetail(l)
  }
  if ((m = /^\/public\/users\/(\d+)\/listings$/.exec(path))) {
    return paginate(rawListings.filter((l) => l.sellerId === Number(m[1])).map(toSummary), params)
  }
  if ((m = /^\/public\/users\/(\d+)\/reviews$/.exec(path))) return []
  if ((m = /^\/public\/users\/(\d+)$/.exec(path))) {
    const u = users[Number(m[1])]
    if (!u) throw { status: 404, message: 'User not found' }
    return publicUser(u)
  }

  // ---- listings (auth) ----
  if (path === '/listings/mine') return paginate(rawListings.filter((l) => l.sellerId === currentUserId(config)).map(toSummary), params)
  if (path === '/listings/favorites') return paginate(rawListings.filter((l) => favorites.has(l.id)).map(toSummary), params)
  if ((m = /^\/listings\/(\d+)\/favorite$/.exec(path)) && method === 'POST') {
    const lid = Number(m[1])
    if (favorites.has(lid)) favorites.delete(lid)
    else favorites.add(lid)
    return { favorited: favorites.has(lid) }
  }
  if (path === '/listings' && method === 'POST') return { ...toDetail(rawListings[0]), id: 999, title: body.title || 'New listing' }
  if ((m = /^\/listings\/(\d+)$/.exec(path))) return { id: Number(m[1]) }

  // ---- offers / orders / conversations / reviews (read = empty, write = ok) ----
  if (path === '/offers/received' || path === '/offers/sent') return emptyPage(params)
  if (path === '/orders/buying' || path === '/orders/selling') return emptyPage(params)
  if (path === '/conversations') return method === 'POST' ? { id: 1 } : []
  if (path === '/conversations/unread-count') return { count: 0 }
  if ((m = /^\/orders\/(\d+)$/.exec(path))) return { id: Number(m[1]), status: 'PAID' }

  // default: succeed quietly so the demo never shows a hard error
  if (method === 'GET') return null
  return { ok: true }
}

function parseBody(data) {
  if (!data) return {}
  if (typeof data === 'object') return data
  try {
    return JSON.parse(data)
  } catch {
    return {}
  }
}

// Axios adapter: resolve with an ApiResponse-shaped envelope, or reject like the real API.
export function demoAdapter(config) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      try {
        const data = route(config)
        resolve({
          data: { success: true, message: '', data, timestamp: new Date().toISOString() },
          status: 200,
          statusText: 'OK',
          headers: { 'content-type': 'application/json' },
          config
        })
      } catch (err) {
        const status = err?.status || 500
        reject({
          response: {
            status,
            data: { success: false, message: err?.message || 'Demo request failed', data: null }
          },
          config,
          isAxiosError: true
        })
      }
    }, 180) // small latency so loading states are visible
  })
}

export const DEMO_MODE = import.meta.env.VITE_RENOVA_DEMO === 'true'
