import {
  loadLocalCareRequests,
  loadOrder,
  loadStoreOrders,
  loadStoreStockMovements,
  updateLocalCareRequest,
  updateStoredOrder
} from './orderTracking'

export const LOCAL_DEMO_ADMIN_TOKEN = 'local-demo-admin-session'

export const LOCAL_ORDER_STATUS_TRANSITIONS = {
  PENDING: ['PAID', 'PROCESSING', 'CANCELLED'],
  PAID: ['PROCESSING', 'CANCELLED'],
  PROCESSING: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['COMPLETED'],
  COMPLETED: [],
  CANCELLED: []
}

export function isLocalDemoAdminSession(authStore) {
  return authStore?.token === LOCAL_DEMO_ADMIN_TOKEN
}

export function shouldUseLocalDemoFallback(error) {
  const status = error?.response?.status
  return !error?.response || status === 404 || status >= 500
}

export function localAdminOrdersForStore(storeSlug) {
  return loadStoreOrders(storeSlug).map(localOrderToAdminOrder)
}

export function localAdminOrderById(orderId) {
  const order = loadOrder(orderId)
  return order ? localOrderToAdminOrder(order) : null
}

export function updateLocalAdminOrderStatus(orderId, status) {
  const order = loadOrder(orderId)
  if (!order) return null
  const currentStatus = localOrderStatus(order)
  if (status !== currentStatus && !LOCAL_ORDER_STATUS_TRANSITIONS[currentStatus]?.includes(status)) {
    return null
  }
  const updatedOrder = updateStoredOrder(orderId, {
    status,
    fulfillmentStatus: fulfillmentLabelForStatus(status),
    shippingStatus: fulfillmentLabelForStatus(status)
  })
  return updatedOrder ? localOrderToAdminOrder(updatedOrder) : null
}

export function localSupportTicketsForStore(storeSlug) {
  return loadLocalCareRequests({ storeSlug, type: 'support' }).map((request) => ({
    id: request.id,
    localDemo: true,
    customerName: request.customerName || 'Demo customer',
    email: request.email || '',
    orderNumber: request.orderNumber || '',
    issueType: request.issueType || 'OTHER',
    message: request.message || '',
    status: request.status || 'OPEN',
    internalNotes: request.internalNotes || '',
    createdAt: request.createdAt,
    updatedAt: request.updatedAt
  }))
}

export function localRefundsForStore(storeSlug, status = '') {
  return loadLocalCareRequests({ storeSlug, type: 'refund' })
    .filter((request) => !status || request.status === status)
    .map((request) => ({
      id: request.id,
      localDemo: true,
      orderId: request.orderNumber,
      orderNumber: request.orderNumber,
      customerName: request.customerName || 'Demo customer',
      email: request.email || '',
      reason: request.reason || request.message || '',
      status: request.status || 'REQUESTED',
      internalNotes: request.internalNotes || '',
      createdAt: request.createdAt,
      updatedAt: request.updatedAt
    }))
}

export function updateLocalSupportTicket(id, payload = {}) {
  const updated = updateLocalCareRequest(id, {
    status: payload.status || 'OPEN',
    internalNotes: payload.internalNotes || ''
  })
  return updated ? localSupportTicketsForStore(updated.storeSlug).find((ticket) => ticket.id === id) : null
}

export function updateLocalRefund(id, payload = {}) {
  const updated = updateLocalCareRequest(id, {
    status: payload.status || 'REQUESTED',
    internalNotes: payload.internalNotes || ''
  })
  return updated ? localRefundsForStore(updated.storeSlug).find((refund) => refund.id === id) : null
}

export function localInventoryWarnings(store, threshold = 5) {
  const warningThreshold = Math.max(0, Math.floor(Number(threshold) || 0))
  return (store?.products || [])
    .filter((product) => Number(product.stockQuantity || 0) <= Number(product.lowStockThreshold ?? warningThreshold))
    .map((product) => ({
      productId: product.id,
      productName: product.name,
      categoryName: product.category,
      stockQuantity: Number(product.stockQuantity || 0),
      lowStockThreshold: Number(product.lowStockThreshold ?? warningThreshold),
      status: Number(product.stockQuantity || 0) < 1 ? 'out of stock' : 'low stock'
    }))
}

export function localInventoryMovementsForStore(storeSlug) {
  return loadStoreStockMovements(storeSlug)
}

export function buildLocalDashboardData(store) {
  const orders = localAdminOrdersForStore(store?.slug)
  const totalRevenue = orders.reduce((total, order) => total + Number(order.totalAmount || 0), 0)
  const totalOrders = orders.length
  const averageOrderValue = totalOrders ? totalRevenue / totalOrders : 0
  return {
    metrics: {
      revenue: totalRevenue,
      totalOrders
    },
    analytics: {
      totalRevenue,
      totalOrders,
      averageOrderValue,
      salesTrend: localSalesTrend(orders),
      bestSellingProducts: localBestSellers(orders)
    },
    orders,
    warnings: localInventoryWarnings(store, 5),
    customers: localCustomers(orders),
    supportTickets: localSupportTicketsForStore(store?.slug),
    refunds: localRefundsForStore(store?.slug)
  }
}

export function localOrderToAdminOrder(order) {
  const customer = order.customer || {}
  const status = localOrderStatus(order)
  return {
    id: order.id,
    localDemo: true,
    storeSlug: order.storeSlug,
    orderNumber: order.orderNumber || order.id,
    customerName: customer.name || order.customerName || 'Demo customer',
    customerEmail: customer.email || order.customerEmail || '',
    customerPhone: customer.phone || order.customerPhone || '',
    shippingAddress: customer.address || '',
    city: customer.city || '',
    region: customer.region || '',
    postalCode: customer.postalCode || '',
    country: customer.country || '',
    shippingMethod: order.deliveryMethod || 'STANDARD',
    paymentMethod: order.paymentMethodLabel || order.paymentMethod || 'Demo payment',
    paymentStatus: order.paymentStatus || 'PENDING',
    refundStatus: order.refundStatus || refundStatusForOrder(order),
    status,
    subtotalAmount: Number(order.subtotal || 0),
    shippingAmount: Number(order.shipping || 0),
    taxAmount: Number(order.tax || 0),
    discountAmount: Number(order.discountTotal || 0),
    totalAmount: Number(order.total || 0),
    items: (order.items || []).map((item, index) => ({
      id: item.itemId || `${order.id}-${index}`,
      productId: item.productId,
      productName: item.name || item.productName || 'Product',
      selectedSize: item.options?.size || item.selectedSize || '',
      selectedColor: item.options?.color || item.selectedColor || '',
      unitPrice: Number(item.price || item.unitPrice || 0),
      originalUnitPrice: Number(item.compareAtPrice || item.originalUnitPrice || item.price || 0),
      discountAmount: Number(item.discountAmount || 0),
      quantity: Number(item.quantity || 0),
      lineTotal: Number(item.lineTotal || (Number(item.price || item.unitPrice || 0) * Number(item.quantity || 0)))
    })),
    createdAt: order.createdAt,
    updatedAt: order.updatedAt || order.createdAt
  }
}

function localOrderStatus(order) {
  if (order.status) return order.status
  if (order.paymentStatus !== 'PAID') return 'PENDING'
  if (order.fulfillmentStatus === 'Shipped') return 'SHIPPED'
  if (order.fulfillmentStatus === 'Delivered') return 'COMPLETED'
  return 'PROCESSING'
}

function fulfillmentLabelForStatus(status) {
  const labels = {
    PENDING: 'Awaiting payment',
    PAID: 'Paid',
    PROCESSING: 'Processing',
    SHIPPED: 'Shipped',
    COMPLETED: 'Delivered',
    CANCELLED: 'Cancelled'
  }
  return labels[status] || 'Processing'
}

function refundStatusForOrder(order) {
  const hasRefund = loadLocalCareRequests({ storeSlug: order.storeSlug, type: 'refund' })
    .some((request) => request.orderNumber === order.id || request.orderNumber === order.orderNumber)
  return hasRefund ? 'REQUESTED' : 'NONE'
}

function localBestSellers(orders) {
  const products = new Map()
  orders.forEach((order) => {
    order.items.forEach((item) => {
      const key = item.productId || item.productName
      const current = products.get(key) || {
        productId: item.productId,
        productName: item.productName,
        unitsSold: 0,
        revenue: 0
      }
      current.unitsSold += Number(item.quantity || 0)
      current.revenue += Number(item.lineTotal || 0)
      products.set(key, current)
    })
  })
  return Array.from(products.values())
    .sort((a, b) => b.unitsSold - a.unitsSold)
    .slice(0, 5)
}

function localSalesTrend(orders) {
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date()
    date.setDate(date.getDate() - (6 - index))
    const dateKey = date.toISOString().slice(0, 10)
    const revenue = orders
      .filter((order) => String(order.createdAt || '').slice(0, 10) === dateKey)
      .reduce((total, order) => total + Number(order.totalAmount || 0), 0)
    return { date: dateKey, revenue }
  })
}

function localCustomers(orders) {
  const customers = new Map()
  orders.forEach((order) => {
    const key = order.customerEmail || order.id
    if (!customers.has(key)) {
      customers.set(key, {
        id: key,
        name: order.customerName,
        email: order.customerEmail,
        country: order.country,
        lastOrderAt: order.createdAt
      })
    }
  })
  return Array.from(customers.values())
}
