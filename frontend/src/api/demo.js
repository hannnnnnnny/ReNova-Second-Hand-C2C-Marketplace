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
  },
  3: {
    id: 3,
    email: 'maya@renova.local',
    displayName: 'Maya Flores',
    avatarUrl: null,
    location: 'Christchurch',
    role: 'USER',
    bio: 'Vintage finds and camera gear. Fast shipper.',
    averageRating: 4.9,
    ratingCount: 11,
    memberSince: daysAgo(160)
  },
  4: {
    id: 4,
    email: 'theo@renova.local',
    displayName: 'Theo Novak',
    avatarUrl: null,
    location: 'Hamilton',
    role: 'USER',
    bio: 'Always open to a fair offer.',
    averageRating: 4.5,
    ratingCount: 8,
    memberSince: daysAgo(95)
  },
  5: {
    id: 5,
    email: 'ivy@renova.local',
    displayName: 'Ivy Bennett',
    avatarUrl: null,
    location: 'Dunedin',
    role: 'USER',
    bio: 'Plants, ceramics, and cosy knitwear.',
    averageRating: 5.0,
    ratingCount: 6,
    memberSince: daysAgo(70)
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
  { id: 1, sellerId: 1, categoryId: 1, title: 'Satin Wrap Dress', img: 'satin-wrap-dress.jpg', price: 68, originalPrice: 180, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Auckland', views: 142, favs: 18, age: 3,
    desc: 'Chocolate-brown satin wrap dress with long sleeves and a tie waist. Worn twice to weddings and dry-cleaned since. Size 10, runs slightly small.' },
  { id: 2, sellerId: 1, categoryId: 4, title: 'Tan Leather Tote', img: 'tan-leather-tote.jpg', price: 95, originalPrice: 240, condition: 'GOOD', status: 'ACTIVE', loc: 'Auckland', views: 98, favs: 12, age: 6,
    desc: 'Full-grain tan leather tote, about 38 x 34 cm, with long shoulder straps. The leather has softened into a natural patina; a couple of light scuffs on the base. Fits a 14-inch laptop.' },
  { id: 3, sellerId: 1, categoryId: 2, title: 'Grey Check Wool Blazer', img: 'check-wool-blazer.jpg', price: 54, originalPrice: 150, condition: 'GOOD', status: 'ACTIVE', loc: 'Auckland', views: 76, favs: 7, age: 9,
    desc: 'Single-breasted two-button blazer in a grey and brown windowpane check, wool blend. Men\'s 40R: chest 104 cm, sleeve 63 cm. Light wear to the lining at the cuffs; no pulls on the outer fabric.' },
  { id: 4, sellerId: 1, categoryId: 4, title: 'Silver Quilted Clutch', img: 'silver-quilted-clutch.jpg', price: 28, originalPrice: 70, condition: 'LIKE_NEW', status: 'RESERVED', loc: 'Auckland', views: 51, favs: 9, age: 12,
    desc: 'Metallic silver quilted clutch with a zip top, 26 x 16 cm. Carried for one evening and kept in its dust bag. Clean interior with a small slip pocket.' },
  { id: 5, sellerId: 1, categoryId: 4, title: 'Gold-tone Jhumka Earrings', img: 'gold-jhumka-earrings.jpg', price: 22, originalPrice: 55, condition: 'NEW', status: 'ACTIVE', loc: 'Auckland', views: 63, favs: 11, age: 15,
    desc: 'Gold-tone bell jhumka earrings with red stone accents and a bead fringe, about 5 cm drop. Never worn - bought as a spare set for a wedding. Push-back posts.' },
  { id: 6, sellerId: 2, categoryId: 3, title: 'Grey Knit Running Shoes', img: 'grey-knit-running-shoes.jpg', price: 60, originalPrice: 160, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 120, favs: 14, age: 4,
    desc: 'Lightweight grey knit running shoes with a lime midsole flash, men\'s US 10. Around 120 km on them, mostly on a treadmill. Plenty of tread left; washed and deodorised.' },
  { id: 7, sellerId: 2, categoryId: 6, title: 'Yoga Mat + Cork Blocks', img: 'yoga-mat-cork-blocks.jpg', price: 18, originalPrice: 45, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 44, favs: 5, age: 7,
    desc: 'Sage green 6 mm yoga mat with two cork yoga blocks. The mat has no tears and still grips well; the blocks have minor edge marks. Wiped down with mat cleaner.' },
  { id: 8, sellerId: 2, categoryId: 6, title: 'Brown Weekender Duffel', img: 'brown-weekender-duffel.jpg', price: 35, originalPrice: 90, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Wellington', views: 58, favs: 6, age: 10,
    desc: 'Brown weekender, roughly 50 x 28 x 25 cm, with twin carry handles and a detachable shoulder strap. Used on one short trip with no marks inside or out. Fits as cabin baggage.' },
  { id: 9, sellerId: 2, categoryId: 5, title: 'White Stacking Mugs (set of 3)', img: 'white-stacking-mugs.jpg', price: 24, originalPrice: 60, condition: 'NEW', status: 'ACTIVE', loc: 'Wellington', views: 39, favs: 4, age: 20,
    desc: 'Three plain white porcelain mugs that stack neatly, about 350 ml each. Unused gift with no chips or crazing. Dishwasher and microwave safe.' },
  { id: 10, sellerId: 2, categoryId: 5, title: 'Chunky Knit Throw', img: 'chunky-knit-throw.jpg', price: 30, originalPrice: 80, condition: 'GOOD', status: 'SOLD', loc: 'Wellington', views: 71, favs: 8, age: 24,
    desc: 'Hand-knitted chunky throw in oatmeal, around 120 x 150 cm. Hand-washed cold and dried flat; still plump with no loose loops. Sold - kept for demo browsing.' },
  { id: 11, sellerId: 5, categoryId: 1, title: 'Ivory Lace Sleeveless Dress', img: 'ivory-lace-dress.jpg', price: 38, originalPrice: 95, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Dunedin', views: 57, favs: 9, age: 2,
    desc: 'Ivory sleeveless dress with a floral lace overlay, beaded waistband and full gathered skirt. Worn once for photos. Zip back, fully lined. Size 8.' },
  { id: 12, sellerId: 3, categoryId: 1, title: 'Oversized Denim Jacket', img: 'oversized-denim-jacket.jpg', price: 45, originalPrice: 120, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 88, favs: 13, age: 5,
    desc: 'Mid-wash oversized denim trucker jacket with chest flap pockets. Soft, broken-in denim with natural fading at the seams; all buttons intact. Women\'s 12, fits roomy.' },
  { id: 13, sellerId: 5, categoryId: 1, title: 'Grey Ribbed Knit Cardigan', img: 'grey-knit-cardigan.jpg', price: 32, originalPrice: 85, condition: 'GOOD', status: 'ACTIVE', loc: 'Dunedin', views: 46, favs: 6, age: 8,
    desc: 'Grey marl cardigan in a chunky rib knit with dark horn buttons. Wool blend, only ever hand-washed. Slight pilling under the arms. Size S-M.' },
  { id: 14, sellerId: 5, categoryId: 1, title: 'Blush Tie-Front Blouse', img: 'blush-tie-front-blouse.jpg', price: 26, originalPrice: 69, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Dunedin', views: 33, favs: 4, age: 11,
    desc: 'Sheer blush chiffon blouse with pintuck detail, a front tie and relaxed long sleeves. Worn a handful of times with no marks. Size 10.' },
  { id: 15, sellerId: 4, categoryId: 2, title: 'Denim Jacket with Cord Collar', img: 'denim-jacket-cord-collar.jpg', price: 85, originalPrice: 220, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Hamilton', views: 104, favs: 16, age: 1,
    desc: 'Dark indigo denim jacket with a brown corduroy collar. Worn a few times, so the denim is still crisp and dark. Men\'s M, chest 112 cm.' },
  { id: 16, sellerId: 4, categoryId: 2, title: 'Button-down Shirts (bundle of 3)', img: 'button-down-shirts-bundle.jpg', price: 40, originalPrice: 150, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 41, favs: 5, age: 13,
    desc: 'Three cotton button-down shirts: white, pale blue and navy micro-print. Regular fit, men\'s M (39 cm collar). Freshly laundered and pressed; no stains or missing buttons.' },
  { id: 17, sellerId: 4, categoryId: 2, title: 'Brown Leather Jacket', img: 'brown-leather-jacket.jpg', price: 120, originalPrice: 350, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 131, favs: 21, age: 6,
    desc: 'Tan-brown leather jacket with a shirt collar and zip front. Supple leather with some creasing at the elbows that suits the style. Men\'s L. Conditioned last month.' }
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
  createdAt: daysAgo(l.age ?? l.id * 3),
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

const listingById = (id) => rawListings.find((l) => l.id === Number(id))

// ---------------------------------------------------------------------------
// Transaction graph for the static demo (no backend). Built so a visitor who
// "logs in" as ava@renova.local sees populated messages, offers, orders, and
// reviews — mirroring the backend seed. Records are stored raw and projected
// relative to the current user, so logging in as anyone else still works.
// ---------------------------------------------------------------------------

// Conversations: each has a buyer, a seller, a listing, and a message thread.
const rawConversations = [
  {
    id: 1, listingId: 6, buyerId: 1, sellerId: 2,
    messages: [
      { senderId: 1, body: 'Hi! Are the running sneakers still available?', days: 2, mins: 0 },
      { senderId: 2, body: 'Yep, barely 120km on them. Cleaned and ready to post.', days: 2, mins: 35 },
      { senderId: 1, body: 'Great — I just sent an offer at $50.', days: 1, mins: 0 },
      { senderId: 2, body: 'Accepted! I\'ll ship tomorrow and send tracking.', days: 1, mins: 20, unreadFor: 'buyer' }
    ]
  },
  {
    id: 2, listingId: 1, buyerId: 3, sellerId: 1,
    messages: [
      { senderId: 3, body: 'Is the silk wrap dress true to a size 10?', days: 1, mins: 0 },
      { senderId: 1, body: 'Runs slightly small — a comfy 8 to 10.', days: 1, mins: 25 },
      { senderId: 3, body: 'Perfect, putting an offer in now!', days: 0, mins: 90, unreadFor: 'seller' }
    ]
  },
  {
    id: 3, listingId: 2, buyerId: 4, sellerId: 1,
    messages: [
      { senderId: 4, body: 'Would you ship the leather tote to Hamilton?', days: 3, mins: 0 },
      { senderId: 1, body: 'Absolutely — free shipping on this one.', days: 3, mins: 40 }
    ]
  }
]

const convLastMsg = (c) => c.messages[c.messages.length - 1]
const convUnreadFor = (c, uid) => {
  const side = uid === c.buyerId ? 'buyer' : uid === c.sellerId ? 'seller' : null
  if (!side) return 0
  return c.messages.filter((m) => m.unreadFor === side).length
}
const msgTime = (m) => new Date(now - m.days * 86400000 + (m.mins || 0) * 60000).toISOString()

function conversationSummary(c, uid) {
  const l = listingById(c.listingId)
  const counterpartyId = uid === c.buyerId ? c.sellerId : c.buyerId
  const last = convLastMsg(c)
  return {
    id: c.id,
    listingId: c.listingId,
    listingTitle: l ? l.title : 'Listing',
    listingCoverImageUrl: l ? asset(l.img) : null,
    listingStatus: l ? l.status : 'ACTIVE',
    counterparty: publicUser(users[counterpartyId]),
    role: uid === c.buyerId ? 'BUYER' : 'SELLER',
    lastMessagePreview: last ? last.body : '',
    lastMessageAt: last ? msgTime(last) : daysAgo(1),
    unreadCount: convUnreadFor(c, uid)
  }
}
function conversationDetail(c, uid) {
  return {
    conversation: conversationSummary(c, uid),
    messages: c.messages.map((m, i) => ({
      id: c.id * 100 + i,
      conversationId: c.id,
      senderId: m.senderId,
      senderName: users[m.senderId] ? users[m.senderId].displayName : 'User',
      body: m.body,
      createdAt: msgTime(m),
      readAt: m.unreadFor ? null : msgTime(m)
    }))
  }
}

// Offers: buyer makes an offer on a listing (the seller is the listing owner).
const rawOffers = [
  { id: 1, listingId: 1, buyerId: 3, amount: 55, message: 'Love this dress! Would $55 work?', status: 'PENDING', fromSeller: false, days: 1 },
  { id: 2, listingId: 2, buyerId: 4, amount: 80, message: '$80 shipped to Hamilton?', status: 'PENDING', fromSeller: false, days: 2 },
  { id: 3, listingId: 6, buyerId: 1, amount: 50, message: 'Would you take $50?', status: 'ACCEPTED', fromSeller: false, days: 1, respondedDays: 1 },
  { id: 4, listingId: 7, buyerId: 1, amount: 14, message: '$14 for the yoga mat?', status: 'PENDING', fromSeller: false, days: 0 }
]
function offerResponse(o) {
  const l = listingById(o.listingId)
  return {
    id: o.id,
    listingId: o.listingId,
    listingTitle: l ? l.title : 'Listing',
    listingCoverImageUrl: l ? asset(l.img) : null,
    buyer: publicUser(users[o.buyerId]),
    seller: publicUser(users[l ? l.sellerId : 1]),
    amount: o.amount,
    message: o.message,
    status: o.status,
    fromSeller: o.fromSeller,
    parentOfferId: null,
    createdAt: daysAgo(o.days),
    respondedAt: o.respondedDays != null ? daysAgo(o.respondedDays) : null
  }
}

// Orders: a buyer's purchase from a seller, in various lifecycle states.
const rawOrders = [
  { id: 1, num: 'RN10231007', listingId: 6, buyerId: 1, sellerId: 2, agreed: 50, ship: 0, status: 'SHIPPED', days: 2, carrier: 'NZ Post', tracking: 'NZ1234567', name: 'Ava Thompson', phone: '+64 21 555 0142', addr: '14 Ponsonby Rd, Auckland 1011' },
  { id: 2, num: 'RN10240412', listingId: 9, buyerId: 1, sellerId: 2, agreed: 24, ship: 0, status: 'COMPLETED', days: 14, carrier: 'CourierPost', tracking: 'NZ9988776', name: 'Ava Thompson', phone: '+64 21 555 0142', addr: '14 Ponsonby Rd, Auckland 1011' },
  { id: 3, num: 'RN10255533', listingId: 3, buyerId: 3, sellerId: 1, agreed: 54, ship: 0, status: 'PAID', days: 2, name: 'Maya Flores', phone: '+64 21 555 0188', addr: '5 Colombo St, Christchurch 8011' },
  { id: 4, num: 'RN10260088', listingId: 4, buyerId: 5, sellerId: 1, agreed: 28, ship: 0, status: 'COMPLETED', days: 10, carrier: 'NZ Post', tracking: 'NZ5544332', name: 'Ivy Bennett', phone: '+64 21 555 0170', addr: '88 George St, Dunedin 9016' }
]
function orderResponse(o) {
  const l = listingById(o.listingId)
  const paid = ['PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED'].includes(o.status)
  const shipped = ['SHIPPED', 'DELIVERED', 'COMPLETED'].includes(o.status)
  const done = o.status === 'COMPLETED'
  return {
    id: o.id,
    orderNumber: o.num,
    listingId: o.listingId,
    listingTitle: l ? l.title : 'Listing',
    listingCoverImageUrl: l ? asset(l.img) : null,
    buyer: publicUser(users[o.buyerId]),
    seller: publicUser(users[o.sellerId]),
    agreedPrice: o.agreed,
    shippingFee: o.ship,
    totalAmount: o.agreed + o.ship,
    shippingName: o.name,
    shippingPhone: o.phone,
    shippingAddress: o.addr,
    buyerNote: null,
    trackingNumber: shipped ? o.tracking || null : null,
    carrier: shipped ? o.carrier || null : null,
    status: o.status,
    createdAt: daysAgo(o.days),
    paidAt: paid ? daysAgo(o.days) : null,
    shippedAt: shipped ? daysAgo(Math.max(0, o.days - 1)) : null,
    deliveredAt: done ? daysAgo(Math.max(0, o.days - 3)) : null,
    completedAt: done ? daysAgo(Math.max(0, o.days - 3)) : null,
    cancelledAt: null,
    cancelReason: null
  }
}

// Reviews left on completed orders (reviewee gets the rating).
const rawReviews = [
  { id: 1, orderId: 4, listingId: 4, reviewerId: 5, revieweeId: 1, rating: 5, comment: 'Beautiful clutch, even nicer in person. Fast shipping!', role: 'BUYER_REVIEWS_SELLER', days: 9 },
  { id: 2, orderId: 2, listingId: 9, reviewerId: 1, revieweeId: 2, rating: 5, comment: 'Mugs arrived perfectly packed. Thanks Liam!', role: 'BUYER_REVIEWS_SELLER', days: 12 },
  { id: 3, orderId: 4, listingId: 4, reviewerId: 1, revieweeId: 5, rating: 5, comment: 'Smooth, friendly buyer. Recommended.', role: 'SELLER_REVIEWS_BUYER', days: 9 }
]
function reviewResponse(r) {
  const l = listingById(r.listingId)
  return {
    id: r.id,
    orderId: r.orderId,
    listingTitle: l ? l.title : 'Listing',
    reviewer: publicUser(users[r.reviewerId]),
    reviewee: publicUser(users[r.revieweeId]),
    rating: r.rating,
    comment: r.comment,
    role: r.role,
    createdAt: daysAgo(r.days)
  }
}

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
  // The real API uses `keyword`; match title + description.
  const kw = params.keyword ?? params.q
  if (kw) {
    const q = String(kw).toLowerCase()
    items = items.filter((l) => l.title.toLowerCase().includes(q) || l.desc.toLowerCase().includes(q))
  }
  if (params.condition) items = items.filter((l) => l.condition === params.condition)
  if (params.minPrice != null && params.minPrice !== '') items = items.filter((l) => l.price >= Number(params.minPrice))
  if (params.maxPrice != null && params.maxPrice !== '') items = items.filter((l) => l.price <= Number(params.maxPrice))
  if (params.location) {
    const loc = String(params.location).toLowerCase()
    items = items.filter((l) => (l.loc || '').toLowerCase().includes(loc))
  }
  if (params.sort === 'price_asc') items = [...items].sort((a, b) => a.price - b.price)
  else if (params.sort === 'price_desc') items = [...items].sort((a, b) => b.price - a.price)
  else if (params.sort === 'popular') items = [...items].sort((a, b) => b.favs - a.favs)
  else items = [...items].sort((a, b) => b.id - a.id)
  return paginate(items.map(toSummary), params)
}

function currentUserId(config) {
  const auth = config.headers?.Authorization || config.headers?.authorization || ''
  const m = /demo\.(\d+)/.exec(auth)
  return m ? Number(m[1]) : 1
}

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
  if ((m = /^\/public\/users\/(\d+)\/reviews$/.exec(path))) {
    return rawReviews.filter((r) => r.revieweeId === Number(m[1])).map(reviewResponse)
  }
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

  // ---- offers (relative to the logged-in user) ----
  const uid = currentUserId(config)
  if (path === '/offers/received') {
    // offers on listings the current user is selling
    const mine = rawOffers.filter((o) => { const l = listingById(o.listingId); return l && l.sellerId === uid })
    return paginate(mine.map(offerResponse), params)
  }
  if (path === '/offers/sent') {
    const mine = rawOffers.filter((o) => o.buyerId === uid)
    return paginate(mine.map(offerResponse), params)
  }
  if ((m = /^\/offers\/(\d+)\/(accept|reject|counter|withdraw|accept-counter)$/.exec(path)) && method === 'POST') {
    const o = rawOffers.find((x) => x.id === Number(m[1]))
    if (!o) throw { status: 404, message: 'Offer not found' }
    const next = {
      accept: 'ACCEPTED', reject: 'REJECTED', counter: 'COUNTERED',
      withdraw: 'WITHDRAWN', 'accept-counter': 'ACCEPTED'
    }[m[2]]
    o.status = next
    o.respondedDays = 0
    // Accepting an offer reserves its listing and rejects the siblings,
    // mirroring the backend so the UI reflects a real state change.
    if (m[2] === 'accept' || m[2] === 'accept-counter') {
      const listing = listingById(o.listingId)
      if (listing) listing.status = 'RESERVED'
      rawOffers.forEach((x) => {
        if (x.id !== o.id && x.listingId === o.listingId && x.status === 'PENDING') x.status = 'REJECTED'
      })
    }
    return offerResponse(o)
  }

  // ---- orders ----
  if (path === '/orders/buying') {
    return paginate(rawOrders.filter((o) => o.buyerId === uid).map(orderResponse), params)
  }
  if (path === '/orders/selling') {
    return paginate(rawOrders.filter((o) => o.sellerId === uid).map(orderResponse), params)
  }
  if ((m = /^\/orders\/(\d+)\/reviews$/.exec(path))) {
    return rawReviews.filter((r) => r.orderId === Number(m[1])).map(reviewResponse)
  }
  if ((m = /^\/orders\/(\d+)\/(pay|ship|confirm-receipt|cancel)$/.exec(path)) && method === 'POST') {
    const o = rawOrders.find((x) => x.id === Number(m[1]))
    if (!o) throw { status: 404, message: 'Order not found' }
    // Advance the in-memory order so the detail page re-renders the new state
    // (and, critically, returns a full order object — not { ok: true } — which
    // the page assigns to `order.value`).
    if (m[2] === 'pay') o.status = 'PAID'
    else if (m[2] === 'ship') { o.status = 'SHIPPED'; o.carrier = body.carrier || o.carrier; o.tracking = body.trackingNumber || o.tracking }
    else if (m[2] === 'confirm-receipt') { o.status = 'COMPLETED'; const l = listingById(o.listingId); if (l) l.status = 'SOLD' }
    else if (m[2] === 'cancel') o.status = 'CANCELLED'
    return orderResponse(o)
  }
  if ((m = /^\/orders\/(\d+)$/.exec(path))) {
    const o = rawOrders.find((x) => x.id === Number(m[1]))
    if (!o) throw { status: 404, message: 'Order not found' }
    return orderResponse(o)
  }
  if (path === '/orders' && method === 'POST') {
    return orderResponse(rawOrders[0]) // demo checkout returns a sample order
  }

  // ---- conversations ----
  if (path === '/conversations') {
    if (method === 'POST') {
      // starting/continuing a conversation: return the matching thread or the first one
      const lid = Number(body.listingId)
      const c = rawConversations.find((x) => x.listingId === lid) || rawConversations[0]
      return conversationDetail(c, uid)
    }
    const mine = rawConversations
      .filter((c) => c.buyerId === uid || c.sellerId === uid)
      .sort((a, b) => new Date(conversationSummary(b, uid).lastMessageAt) - new Date(conversationSummary(a, uid).lastMessageAt))
    return mine.map((c) => conversationSummary(c, uid))
  }
  if (path === '/conversations/unread-count') {
    const total = rawConversations
      .filter((c) => c.buyerId === uid || c.sellerId === uid)
      .reduce((sum, c) => sum + convUnreadFor(c, uid), 0)
    return { count: total }
  }
  if ((m = /^\/conversations\/(\d+)\/messages$/.exec(path)) && method === 'POST') {
    return {
      id: Date.now(), conversationId: Number(m[1]), senderId: uid,
      senderName: users[uid] ? users[uid].displayName : 'You',
      body: body.body || '', createdAt: new Date().toISOString(), readAt: null
    }
  }
  if ((m = /^\/conversations\/(\d+)$/.exec(path))) {
    const c = rawConversations.find((x) => x.id === Number(m[1]))
    if (!c) throw { status: 404, message: 'Conversation not found' }
    return conversationDetail(c, uid)
  }

  // ---- reviews ----
  if (path === '/reviews' && method === 'POST') {
    const order = rawOrders.find((x) => x.id === Number(body.orderId))
    if (!order) throw { status: 404, message: 'Order not found' }
    const buyerReviews = order.buyerId === uid
    const revieweeId = buyerReviews ? order.sellerId : order.buyerId
    const review = {
      id: Date.now(),
      orderId: order.id,
      listingId: order.listingId,
      reviewerId: uid,
      revieweeId,
      rating: Number(body.rating) || 5,
      comment: body.comment || '',
      role: buyerReviews ? 'BUYER_REVIEWS_SELLER' : 'SELLER_REVIEWS_BUYER',
      days: 0
    }
    rawReviews.push(review)
    return reviewResponse(review)
  }

  // ---- image upload ----
  if (path === '/uploads/images' && method === 'POST') {
    // config.data is a FormData; turn each real File into an object URL so the
    // uploaded photo shows in the preview even though there's no backend.
    const files = (config.data && typeof config.data.getAll === 'function')
      ? config.data.getAll('files') : []
    const images = files.map((f, i) => ({
      url: (typeof URL !== 'undefined' && typeof URL.createObjectURL === 'function' && f && f.size != null)
        ? URL.createObjectURL(f)
        : asset('fashion-dress.jpg'),
      contentType: (f && f.type) || 'image/jpeg',
      size: (f && f.size) || 0,
      originalName: (f && f.name) || `image-${i + 1}.jpg`
    }))
    return { images }
  }

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
