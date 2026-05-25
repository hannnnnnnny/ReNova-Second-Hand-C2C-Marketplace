<script setup>
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { conversationApi } from '../api/endpoints'
import LocaleSwitcher from './LocaleSwitcher.vue'
import Avatar from './Avatar.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const menuOpen = ref(false)
const unread = ref(0)

const query = ref(route.query.keyword || '')

async function fetchUnread() {
  if (!auth.isAuthenticated) { unread.value = 0; return }
  try {
    const result = await conversationApi.unread()
    unread.value = Number(result?.count || 0)
  } catch { unread.value = 0 }
}

function submitSearch() {
  router.push({ name: 'browse', query: query.value ? { keyword: query.value } : {} })
}

function logout() {
  auth.logout()
  menuOpen.value = false
  router.push({ name: 'home' })
}

watch(() => route.fullPath, () => { menuOpen.value = false; fetchUnread() })
onMounted(fetchUnread)
</script>

<template>
  <header class="topbar">
    <div class="container topbar-inner">
      <RouterLink :to="{ name: 'home' }" class="brand">
        <span class="brand-mark">R</span>
        <span>ReNova</span>
      </RouterLink>

      <form class="search-wrap" @submit.prevent="submitSearch">
        <input :placeholder="t('common.searchPlaceholder')" v-model="query" />
        <button type="submit" class="btn btn-primary btn-sm">{{ t('common.search') }}</button>
      </form>

      <div class="nav-actions">
        <LocaleSwitcher />
        <RouterLink :to="{ name: 'browse' }" class="btn btn-ghost btn-sm">{{ t('common.browse') }}</RouterLink>
        <template v-if="auth.isAuthenticated">
          <RouterLink :to="{ name: 'post-listing' }" class="btn btn-accent btn-sm">{{ t('common.sell') }}</RouterLink>
          <RouterLink :to="{ name: 'messages' }" class="btn btn-ghost btn-sm" style="position: relative">
            {{ t('common.messages') }}
            <span v-if="unread > 0" class="badge badge-danger" style="margin-left:4px">{{ unread }}</span>
          </RouterLink>
          <div style="position: relative">
            <button class="btn btn-ghost btn-sm" @click="menuOpen = !menuOpen" type="button">
              <Avatar :user="auth.user" />
              <span>{{ auth.user?.displayName }}</span>
            </button>
            <div v-if="menuOpen" class="card" style="position:absolute; top: 110%; right: 0; min-width: 220px; padding: 8px; z-index: 60;">
              <RouterLink :to="{ name: 'profile-mine' }" class="btn btn-ghost btn-sm" style="justify-content:flex-start; width:100%">{{ t('common.profile') }}</RouterLink>
              <RouterLink :to="{ name: 'my-listings' }" class="btn btn-ghost btn-sm" style="justify-content:flex-start; width:100%">{{ t('post.title') }}</RouterLink>
              <RouterLink :to="{ name: 'offers' }" class="btn btn-ghost btn-sm" style="justify-content:flex-start; width:100%">{{ t('common.offers') }}</RouterLink>
              <RouterLink :to="{ name: 'orders' }" class="btn btn-ghost btn-sm" style="justify-content:flex-start; width:100%">{{ t('common.orders') }}</RouterLink>
              <RouterLink :to="{ name: 'favorites' }" class="btn btn-ghost btn-sm" style="justify-content:flex-start; width:100%">{{ t('common.favorites') }}</RouterLink>
              <button class="btn btn-ghost btn-sm" style="justify-content:flex-start; width:100%" type="button" @click="logout">{{ t('common.logout') }}</button>
            </div>
          </div>
        </template>
        <template v-else>
          <RouterLink :to="{ name: 'login' }" class="btn btn-outline btn-sm">{{ t('common.login') }}</RouterLink>
          <RouterLink :to="{ name: 'signup' }" class="btn btn-primary btn-sm">{{ t('common.signup') }}</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>
