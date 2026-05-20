import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import PlatformLayout from '../layouts/PlatformLayout.vue'
import MerchantStorefrontLayout from '../layouts/MerchantStorefrontLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import PlatformHomePage from '../pages/platform/PlatformHomePage.vue'
import FeaturesPage from '../pages/platform/FeaturesPage.vue'
import TemplatesPage from '../pages/platform/TemplatesPage.vue'
import PricingPage from '../pages/platform/PricingPage.vue'
import SignupPage from '../pages/platform/SignupPage.vue'
import OnboardingPage from '../pages/platform/OnboardingPage.vue'
import NotFoundPage from '../pages/platform/NotFoundPage.vue'
import MerchantStoreHomePage from '../pages/store/MerchantStoreHomePage.vue'
import MerchantStoreProductsPage from '../pages/store/MerchantStoreProductsPage.vue'
import MerchantStoreProductDetailPage from '../pages/store/MerchantStoreProductDetailPage.vue'
import MerchantStoreCartPage from '../pages/store/MerchantStoreCartPage.vue'
import MerchantStoreCheckoutPage from '../pages/store/MerchantStoreCheckoutPage.vue'
import MerchantStoreOrderSuccessPage from '../pages/store/MerchantStoreOrderSuccessPage.vue'
import MerchantStoreOrderHistoryPage from '../pages/store/MerchantStoreOrderHistoryPage.vue'
import MerchantStoreOrderTrackingPage from '../pages/store/MerchantStoreOrderTrackingPage.vue'
import MerchantStoreSupportPage from '../pages/store/MerchantStoreSupportPage.vue'
import AdminLoginPage from '../pages/admin/AdminLoginPage.vue'
import AdminDashboardPage from '../pages/admin/AdminDashboardPage.vue'
import AdminProductsPage from '../pages/admin/AdminProductsPage.vue'
import AdminProductFormPage from '../pages/admin/AdminProductFormPage.vue'
import AdminCategoriesPage from '../pages/admin/AdminCategoriesPage.vue'
import AdminCollectionsPage from '../pages/admin/AdminCollectionsPage.vue'
import AdminPromotionsPage from '../pages/admin/AdminPromotionsPage.vue'
import AdminOrdersPage from '../pages/admin/AdminOrdersPage.vue'
import AdminOrderDetailPage from '../pages/admin/AdminOrderDetailPage.vue'
import AdminInventoryPage from '../pages/admin/AdminInventoryPage.vue'
import AdminSupportPage from '../pages/admin/AdminSupportPage.vue'
import AdminRefundsPage from '../pages/admin/AdminRefundsPage.vue'
import AdminAnalyticsPage from '../pages/admin/AdminAnalyticsPage.vue'
import AdminCustomersPage from '../pages/admin/AdminCustomersPage.vue'
import AdminSettingsPage from '../pages/admin/AdminSettingsPage.vue'
import AdminStoreSetupPage from '../pages/admin/AdminStoreSetupPage.vue'
import AdminTemplatesPage from '../pages/admin/AdminTemplatesPage.vue'
import AdminThemeEditorPage from '../pages/admin/AdminThemeEditorPage.vue'
import { applyRouteSeo } from '../utils/seo'
import { usePlatformStore } from '../stores/platform'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: PlatformLayout,
      children: [
        { path: '', name: 'platform-home', component: PlatformHomePage },
        { path: 'features', name: 'platform-features', component: FeaturesPage },
        { path: 'templates', name: 'platform-templates', component: TemplatesPage },
        { path: 'pricing', name: 'platform-pricing', component: PricingPage },
        { path: 'signup', name: 'merchant-signup', component: SignupPage },
        { path: 'onboarding', name: 'merchant-onboarding', component: OnboardingPage },
        { path: 'products', name: 'products', redirect: (to) => ({ path: '/store/demo-fashion/products', query: to.query }) },
        { path: 'products/:id', name: 'product-detail', redirect: (to) => ({ path: `/store/demo-fashion/products/${to.params.id}`, query: to.query }) },
        { path: 'cart', name: 'cart', redirect: (to) => ({ path: '/store/demo-fashion/cart', query: to.query }) },
        { path: 'checkout', name: 'checkout', redirect: (to) => ({ path: '/store/demo-fashion/checkout', query: to.query }) },
        { path: 'support', name: 'support', redirect: (to) => ({ path: '/store/demo-fashion/support', query: to.query }) },
        { path: 'refund-request', name: 'refund-request', redirect: (to) => ({ path: '/store/demo-fashion/support', query: { ...to.query, mode: 'refund' } }) }
      ]
    },
    { path: '/login', name: 'merchant-login', component: AdminLoginPage },
    { path: '/admin/login', redirect: '/login' },
    {
      path: '/store/:storeSlug',
      component: MerchantStorefrontLayout,
      children: [
        { path: '', name: 'merchant-store-home', component: MerchantStoreHomePage, props: true },
        { path: 'products', name: 'merchant-store-products', component: MerchantStoreProductsPage, props: true },
        { path: 'products/:productId', name: 'merchant-store-product-detail', component: MerchantStoreProductDetailPage, props: true },
        { path: 'cart', name: 'merchant-store-cart', component: MerchantStoreCartPage, props: true },
        { path: 'checkout', name: 'merchant-store-checkout', component: MerchantStoreCheckoutPage, props: true },
        { path: 'order-success', name: 'merchant-store-order-success', component: MerchantStoreOrderSuccessPage, props: true },
        { path: 'orders', name: 'merchant-store-order-history', component: MerchantStoreOrderHistoryPage, props: true },
        { path: 'orders/:orderId', name: 'merchant-store-order-tracking', component: MerchantStoreOrderTrackingPage, props: true },
        { path: 'support', name: 'merchant-store-support', component: MerchantStoreSupportPage, props: true }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'admin-dashboard', component: AdminDashboardPage },
        { path: 'store-setup', name: 'admin-store-setup', component: AdminStoreSetupPage },
        { path: 'products', name: 'admin-products', component: AdminProductsPage },
        { path: 'products/new', name: 'admin-product-new', component: AdminProductFormPage },
        { path: 'products/:id/edit', name: 'admin-product-edit', component: AdminProductFormPage },
        { path: 'categories', name: 'admin-categories', component: AdminCategoriesPage },
        { path: 'collections', name: 'admin-collections', component: AdminCollectionsPage },
        { path: 'promotions', name: 'admin-promotions', component: AdminPromotionsPage },
        { path: 'orders', name: 'admin-orders', component: AdminOrdersPage },
        { path: 'orders/:id', name: 'admin-order-detail', component: AdminOrderDetailPage },
        { path: 'inventory', name: 'admin-inventory', component: AdminInventoryPage },
        { path: 'support', name: 'admin-support', component: AdminSupportPage },
        { path: 'refunds', name: 'admin-refunds', component: AdminRefundsPage },
        { path: 'customers', name: 'admin-customers', component: AdminCustomersPage },
        { path: 'analytics', name: 'admin-analytics', component: AdminAnalyticsPage },
        { path: 'templates', name: 'admin-templates', component: AdminTemplatesPage },
        { path: 'theme-editor', name: 'admin-theme-editor', component: AdminThemeEditorPage },
        { path: 'settings', name: 'admin-settings', component: AdminSettingsPage }
      ]
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundPage }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const sessionStatus = authStore.token ? authStore.validateSession() : authStore.loadSession()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'merchant-login',
      query: {
        redirect: to.fullPath,
        ...(sessionStatus === 'expired' ? { session: 'expired' } : {})
      }
    }
  }
})

router.afterEach((to) => {
  const platformStore = usePlatformStore()
  platformStore.loadPlatform()
  applyRouteSeo(to, (slug) => platformStore.getStore(slug))
})

export default router
