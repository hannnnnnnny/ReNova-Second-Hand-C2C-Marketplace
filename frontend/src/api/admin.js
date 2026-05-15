import apiClient, { getApiData } from './client'

export async function loginAdmin(payload) {
  return getApiData(await apiClient.post('/admin/auth/login', payload))
}

export async function fetchDashboardMetrics() {
  return getApiData(await apiClient.get('/admin/dashboard/metrics'))
}

export async function fetchInventoryWarnings(threshold = 5) {
  return getApiData(await apiClient.get('/admin/inventory/warnings', { params: { threshold } }))
}

export async function fetchAdminProducts() {
  return (await fetchAdminProductPage({ size: 100 })).content
}

export async function fetchAdminProductPage(params = {}) {
  const data = getApiData(await apiClient.get('/admin/products', { params }))
  if (Array.isArray(data)) {
    return {
      content: data,
      page: 0,
      size: data.length,
      totalElements: data.length,
      totalPages: data.length ? 1 : 0,
      first: true,
      last: true,
      empty: data.length === 0
    }
  }
  return data
}

export async function fetchAdminProduct(id) {
  return getApiData(await apiClient.get(`/admin/products/${id}`))
}

export async function createAdminProduct(payload) {
  return getApiData(await apiClient.post('/admin/products', payload))
}

export async function updateAdminProduct(id, payload) {
  return getApiData(await apiClient.put(`/admin/products/${id}`, payload))
}

export async function deleteAdminProduct(id) {
  return getApiData(await apiClient.delete(`/admin/products/${id}`))
}

export async function fetchAdminCategories() {
  return getApiData(await apiClient.get('/admin/categories'))
}

export async function createAdminCategory(payload) {
  return getApiData(await apiClient.post('/admin/categories', payload))
}

export async function updateAdminCategory(id, payload) {
  return getApiData(await apiClient.put(`/admin/categories/${id}`, payload))
}

export async function deleteAdminCategory(id) {
  return getApiData(await apiClient.delete(`/admin/categories/${id}`))
}

export async function fetchAdminOrders() {
  return getApiData(await apiClient.get('/admin/orders'))
}

export async function fetchAdminOrder(id) {
  return getApiData(await apiClient.get(`/admin/orders/${id}`))
}

export async function updateOrderStatus(id, status) {
  return getApiData(await apiClient.patch(`/admin/orders/${id}/status`, { status }))
}
