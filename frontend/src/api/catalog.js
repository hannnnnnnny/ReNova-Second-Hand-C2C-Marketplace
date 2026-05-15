import apiClient, { getApiData } from './client'

export async function fetchCategories() {
  return getApiData(await apiClient.get('/public/categories'))
}

export async function fetchProductPage(params = {}) {
  const data = getApiData(await apiClient.get('/public/products', { params }))
  return normalizeProductPage(data)
}

export async function fetchProducts(paramsOrCategoryId = {}) {
  const params = typeof paramsOrCategoryId === 'number' ? { categoryId: paramsOrCategoryId } : paramsOrCategoryId
  return (await fetchProductPage({ size: 100, ...params })).content
}

export async function fetchProduct(id) {
  return getApiData(await apiClient.get(`/public/products/${id}`))
}

function normalizeProductPage(data) {
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
