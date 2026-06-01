import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { i18n } from '../i18n'

const HomePage = () => import('../pages/HomePage.vue')
const BrowsePage = () => import('../pages/BrowsePage.vue')
const ListingDetailPage = () => import('../pages/ListingDetailPage.vue')
const PostListingPage = () => import('../pages/PostListingPage.vue')
const EditListingPage = () => import('../pages/EditListingPage.vue')
const MyListingsPage = () => import('../pages/MyListingsPage.vue')
const FavoritesPage = () => import('../pages/FavoritesPage.vue')
const OffersPage = () => import('../pages/OffersPage.vue')
const MessagesPage = () => import('../pages/MessagesPage.vue')
const OrdersPage = () => import('../pages/OrdersPage.vue')
const OrderDetailPage = () => import('../pages/OrderDetailPage.vue')
const CheckoutPage = () => import('../pages/CheckoutPage.vue')
const ProfilePage = () => import('../pages/ProfilePage.vue')
const MyProfilePage = () => import('../pages/MyProfilePage.vue')
const LoginPage = () => import('../pages/LoginPage.vue')
const SignupPage = () => import('../pages/SignupPage.vue')
const NotFoundPage = () => import('../pages/NotFoundPage.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: HomePage },
        { path: 'browse', name: 'browse', component: BrowsePage },
        { path: 'listings/:id', name: 'listing-detail', component: ListingDetailPage, props: true },
        { path: 'sell', name: 'post-listing', component: PostListingPage, meta: { requiresAuth: true } },
        { path: 'listings/:id/edit', name: 'edit-listing', component: EditListingPage, props: true, meta: { requiresAuth: true } },
        { path: 'my/listings', name: 'my-listings', component: MyListingsPage, meta: { requiresAuth: true } },
        { path: 'my/favorites', name: 'favorites', component: FavoritesPage, meta: { requiresAuth: true } },
        { path: 'my/offers', name: 'offers', component: OffersPage, meta: { requiresAuth: true } },
        { path: 'messages', name: 'messages', component: MessagesPage, meta: { requiresAuth: true } },
        { path: 'messages/new', name: 'compose-message', component: MessagesPage, meta: { requiresAuth: true } },
        { path: 'messages/:id', name: 'conversation', component: MessagesPage, props: true, meta: { requiresAuth: true } },
        { path: 'orders', name: 'orders', component: OrdersPage, meta: { requiresAuth: true } },
        { path: 'orders/:id', name: 'order-detail', component: OrderDetailPage, props: true, meta: { requiresAuth: true } },
        { path: 'checkout/:listingId', name: 'checkout', component: CheckoutPage, props: true, meta: { requiresAuth: true } },
        { path: 'me', name: 'profile-mine', component: MyProfilePage, meta: { requiresAuth: true } },
        { path: 'users/:id', name: 'profile', component: ProfilePage, props: true },
        { path: 'login', name: 'login', component: LoginPage },
        { path: 'signup', name: 'signup', component: SignupPage },
        { path: ':pathMatch(.*)*', name: 'not-found', component: NotFoundPage }
      ]
    }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth) {
    const auth = useAuthStore()
    if (!auth.isAuthenticated) {
      const toast = useToastStore()
      toast.error(i18n.global.t('errors.mustLogin'))
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
