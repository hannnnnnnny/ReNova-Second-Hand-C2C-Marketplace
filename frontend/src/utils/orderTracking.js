const ORDER_KEY_PREFIX = 'novacart_order_'
const ORDER_INDEX_KEY = 'novacart_storefront_orders'
const LOCAL_CARE_REQUESTS_KEY = 'novacart_local_care_requests'
const STOCK_MOVEMENTS_KEY = 'novacart_storefront_stock_movements'

export const PAYMENT_METHODS = [
  {
    id: 'CARD',
    label: 'Credit or debit card',
    description: 'Demo card form with validation. No real payment is processed.',
    status: 'PAID'
  },
  {
    id: 'PAYPAL',
    label: 'PayPal demo',
    description: 'Creates a simulated PayPal-approved order.',
    status: 'PAID'
  },
  {
    id: 'APPLE_GOOGLE_PAY',
    label: 'Apple Pay / Google Pay',
    description: 'Wallet placeholder for responsive checkout previews.',
    status: 'PAID'
  },
  {
    id: 'BNPL',
    label: 'Buy now, pay later',
    description: 'Demo instalment approval with no external provider.',
    status: 'PAID'
  },
  {
    id: 'BANK_TRANSFER',
    label: 'Bank transfer',
    description: 'Marks payment as pending until the merchant confirms it.',
    status: 'PENDING'
  },
  {
    id: 'COD',
    label: 'Cash on delivery',
    description: 'Payment is collected when the parcel is delivered.',
    status: 'PENDING'
  }
]

export const TRACKING_STEPS = [
  'Order placed',
  'Payment confirmed',
  'Processing',
  'Packed',
  'Shipped',
  'Out for delivery',
  'Delivered'
]

export function paymentMethodLabel(methodId) {
  return PAYMENT_METHODS.find((method) => method.id === methodId)?.label || 'Demo payment'
}

export function paymentStatusForMethod(methodId) {
  return PAYMENT_METHODS.find((method) => method.id === methodId)?.status || 'PENDING'
}

export function deliveryMethodLabel(methodId) {
  const labels = {
    STANDARD: 'Standard delivery',
    EXPRESS: 'Express delivery',
    PICKUP: 'Merchant pickup'
  }
  return labels[methodId] || labels.STANDARD
}

export function createDemoTracking(order, store = {}, now = new Date()) {
  const createdAt = new Date(order.createdAt || now)
  const paid = order.paymentStatus === 'PAID'
  const currentStepIndex = paid ? 2 : 0
  const carrier = carrierForDelivery(order.deliveryMethod)
  const estimatedDeliveryDate = addDays(createdAt, daysForDelivery(order.deliveryMethod)).toISOString()

  return {
    carrier,
    trackingNumber: order.trackingNumber || trackingNumberFor(order.id, store.slug || order.storeSlug),
    estimatedDeliveryDate,
    currentStatus: TRACKING_STEPS[currentStepIndex],
    timeline: TRACKING_STEPS.map((label, index) => ({
      label,
      status: index < currentStepIndex ? 'complete' : index === currentStepIndex ? 'current' : 'upcoming',
      timestamp: index <= currentStepIndex ? addHours(createdAt, index * 4).toISOString() : null,
      description: trackingDescription(label, carrier)
    }))
  }
}

export function saveStoreOrder(order) {
  if (!order?.id || !order?.storeSlug) return null
  const normalizedOrder = {
    ...order,
    tracking: order.tracking || createDemoTracking(order, { slug: order.storeSlug })
  }
  localStorage.setItem(`${ORDER_KEY_PREFIX}${normalizedOrder.id}`, JSON.stringify(normalizedOrder))

  const index = loadOrderIndex()
  const orderIds = index[normalizedOrder.storeSlug] || []
  index[normalizedOrder.storeSlug] = [
    normalizedOrder.id,
    ...orderIds.filter((orderId) => orderId !== normalizedOrder.id)
  ].slice(0, 25)
  localStorage.setItem(ORDER_INDEX_KEY, JSON.stringify(index))
  return normalizedOrder
}

export function updateStoredOrder(orderId, patch = {}) {
  const order = loadOrder(orderId)
  if (!order) return null
  return saveStoreOrder({
    ...order,
    ...patch,
    updatedAt: new Date().toISOString()
  })
}

export function loadOrder(orderId) {
  const id = String(orderId || '')
  if (!id) return null
  const rawOrder = localStorage.getItem(`${ORDER_KEY_PREFIX}${id}`)
  if (!rawOrder) return null
  try {
    const parsedOrder = JSON.parse(rawOrder)
    return parsedOrder?.id ? parsedOrder : null
  } catch {
    return null
  }
}

export function loadStoreOrders(storeSlug) {
  const index = loadOrderIndex()
  return (index[storeSlug] || [])
    .map(loadOrder)
    .filter(Boolean)
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
}

export function saveLocalCareRequest(type, payload = {}) {
  const normalizedType = type === 'refund' ? 'refund' : 'support'
  const current = readLocalArray(LOCAL_CARE_REQUESTS_KEY)
  const request = {
    id: payload.id || `${normalizedType}-${Date.now()}`,
    type: normalizedType,
    status: payload.status || (normalizedType === 'refund' ? 'REQUESTED' : 'OPEN'),
    createdAt: payload.createdAt || new Date().toISOString(),
    updatedAt: payload.updatedAt || new Date().toISOString(),
    ...payload
  }
  localStorage.setItem(LOCAL_CARE_REQUESTS_KEY, JSON.stringify([request, ...current.filter((entry) => entry.id !== request.id)].slice(0, 75)))
  return request
}

export function loadLocalCareRequests(filters = {}) {
  const requests = readLocalArray(LOCAL_CARE_REQUESTS_KEY)
  return requests.filter((request) => {
    const matchesStore = !filters.storeSlug || request.storeSlug === filters.storeSlug
    const matchesType = !filters.type || request.type === filters.type
    const matchesStatus = !filters.status || request.status === filters.status
    return matchesStore && matchesType && matchesStatus
  })
}

export function updateLocalCareRequest(id, patch = {}) {
  const requests = readLocalArray(LOCAL_CARE_REQUESTS_KEY)
  let updatedRequest = null
  const nextRequests = requests.map((request) => {
    if (request.id !== id) return request
    updatedRequest = {
      ...request,
      ...patch,
      updatedAt: new Date().toISOString()
    }
    return updatedRequest
  })
  if (!updatedRequest) return null
  localStorage.setItem(LOCAL_CARE_REQUESTS_KEY, JSON.stringify(nextRequests))
  return updatedRequest
}

export function saveStoreStockMovement(payload = {}) {
  if (!payload.storeSlug || !payload.productId) return null
  const movement = {
    id: payload.id || `movement-${Date.now()}-${payload.productId}`,
    storeSlug: payload.storeSlug,
    productId: payload.productId,
    productName: payload.productName || 'Product',
    orderId: payload.orderId || null,
    type: payload.type || 'MANUAL_ADJUSTMENT',
    quantityChange: Number(payload.quantityChange) || 0,
    stockAfter: Math.max(0, Number(payload.stockAfter) || 0),
    reason: payload.reason || '',
    createdAt: payload.createdAt || new Date().toISOString()
  }
  const current = readLocalArray(STOCK_MOVEMENTS_KEY)
  localStorage.setItem(STOCK_MOVEMENTS_KEY, JSON.stringify([movement, ...current].slice(0, 100)))
  return movement
}

export function loadStoreStockMovements(storeSlug) {
  return readLocalArray(STOCK_MOVEMENTS_KEY)
    .filter((movement) => !storeSlug || movement.storeSlug === storeSlug)
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
}

export function formatOrderAddress(order) {
  const customer = order?.customer || {}
  return [customer.address, customer.city, customer.region, customer.postalCode, customer.country]
    .filter(Boolean)
    .join(', ')
}

export function maskCardNumber(cardNumber) {
  const digits = String(cardNumber || '').replace(/\D/g, '')
  return digits ? `**** ${digits.slice(-4)}` : ''
}

function loadOrderIndex() {
  const rawIndex = localStorage.getItem(ORDER_INDEX_KEY)
  if (!rawIndex) return {}
  try {
    const parsedIndex = JSON.parse(rawIndex)
    return parsedIndex && typeof parsedIndex === 'object' && !Array.isArray(parsedIndex) ? parsedIndex : {}
  } catch {
    localStorage.removeItem(ORDER_INDEX_KEY)
    return {}
  }
}

function readLocalArray(storageKey) {
  try {
    const parsed = JSON.parse(localStorage.getItem(storageKey) || '[]')
    return Array.isArray(parsed) ? parsed : []
  } catch {
    localStorage.removeItem(storageKey)
    return []
  }
}

function carrierForDelivery(methodId) {
  if (methodId === 'EXPRESS') return 'MetroParcel Express'
  if (methodId === 'PICKUP') return 'Merchant pickup desk'
  return 'NovaPost Standard'
}

function daysForDelivery(methodId) {
  if (methodId === 'EXPRESS') return 2
  if (methodId === 'PICKUP') return 1
  return 5
}

function trackingNumberFor(orderId, storeSlug) {
  const hash = String(`${storeSlug || 'store'}-${orderId || Date.now()}`)
    .split('')
    .reduce((total, char) => total + char.charCodeAt(0), 0)
    .toString(36)
    .toUpperCase()
  return `NVC-${hash}-${String(orderId || '').slice(-5).toUpperCase()}`
}

function trackingDescription(label, carrier) {
  const descriptions = {
    'Order placed': 'The merchant received the order in NovaCart.',
    'Payment confirmed': 'The selected demo payment method was approved or marked for confirmation.',
    Processing: 'The merchant is checking inventory and preparing the order.',
    Packed: 'Items are packed with the selected shipping details.',
    Shipped: `${carrier} has received the parcel information.`,
    'Out for delivery': 'The parcel is on the final delivery route.',
    Delivered: 'The demo delivery is complete.'
  }
  return descriptions[label] || ''
}

function addDays(date, days) {
  const nextDate = new Date(date)
  nextDate.setDate(nextDate.getDate() + days)
  return nextDate
}

function addHours(date, hours) {
  const nextDate = new Date(date)
  nextDate.setHours(nextDate.getHours() + hours)
  return nextDate
}
