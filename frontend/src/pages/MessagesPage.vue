<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { conversationApi } from '../api/endpoints'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatRelative } from '../utils/format'
import Avatar from '../components/Avatar.vue'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const conversations = ref([])
const active = ref(null)
const composeBody = ref('')
const sending = ref(false)
const loading = ref(true)
const msgsRef = ref(null)

const activeId = computed(() => Number(route.params.id) || null)

async function loadList() {
  loading.value = true
  try { conversations.value = await conversationApi.list() }
  catch (err) { toast.error(apiError(err)) }
  finally { loading.value = false }
}

async function openConversation(id) {
  try {
    active.value = await conversationApi.open(id)
    if (route.params.id !== String(id)) {
      router.replace({ name: 'conversation', params: { id } })
    }
    nextTick(scrollToBottom)
    // refresh list to clear unread
    loadList()
  } catch (err) { toast.error(apiError(err)) }
}

function scrollToBottom() {
  if (msgsRef.value) msgsRef.value.scrollTop = msgsRef.value.scrollHeight
}

async function send() {
  const body = composeBody.value.trim()
  if (!body || !active.value) return
  sending.value = true
  try {
    const m = await conversationApi.send(active.value.conversation.id, { body })
    active.value.messages.push(m)
    active.value.conversation.lastMessagePreview = body
    composeBody.value = ''
    nextTick(scrollToBottom)
    loadList()
  } catch (err) { toast.error(apiError(err)) } finally { sending.value = false }
}

watch(activeId, async (id) => {
  if (id) await openConversation(id)
})

onMounted(async () => {
  await loadList()
  if (activeId.value) await openConversation(activeId.value)
  else if (conversations.value.length > 0) await openConversation(conversations.value[0].id)
})
</script>

<template>
  <main class="page">
    <div class="container">
      <h1 style="margin-bottom: 24px">{{ t('chat.title') }}</h1>

      <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
      <div v-else-if="conversations.length === 0" class="empty-state">{{ t('chat.empty') }}</div>

      <div v-else class="chat-shell">
        <div class="chat-list">
          <div v-for="c in conversations" :key="c.id" class="chat-list-item" :class="{ 'is-active': active?.conversation?.id === c.id }" @click="openConversation(c.id)">
            <div class="thumb" :style="{ backgroundImage: c.listingCoverImageUrl ? `url('${c.listingCoverImageUrl}')` : '' }"></div>
            <div class="grow" style="min-width: 0">
              <div class="row" style="justify-content: space-between">
                <span class="bold" style="overflow:hidden; text-overflow:ellipsis; white-space:nowrap">{{ c.counterparty.displayName }}</span>
                <span v-if="c.unreadCount > 0" class="badge badge-danger">{{ c.unreadCount }}</span>
              </div>
              <div class="soft" style="overflow:hidden; text-overflow:ellipsis; white-space:nowrap">{{ c.listingTitle }}</div>
              <div class="preview">{{ c.lastMessagePreview || '—' }}</div>
              <div class="soft" style="font-size:11px">{{ formatRelative(c.lastMessageAt, locale) }} · {{ t(`chat.role.${c.role}`) }}</div>
            </div>
          </div>
        </div>

        <div class="chat-thread" v-if="active">
          <div class="chat-thread-head">
            <Avatar :user="active.conversation.counterparty" />
            <div class="grow">
              <div class="bold">{{ active.conversation.counterparty.displayName }}</div>
              <div class="soft">
                <RouterLink :to="{ name: 'listing-detail', params: { id: active.conversation.listingId } }">{{ active.conversation.listingTitle }}</RouterLink>
              </div>
            </div>
          </div>
          <div class="chat-msgs" ref="msgsRef">
            <div v-for="m in active.messages" :key="m.id" class="bubble" :class="m.senderId === auth.user?.id ? 'from-me' : 'from-them'">
              <div>{{ m.body }}</div>
              <div class="bubble-time">{{ formatRelative(m.createdAt, locale) }}</div>
            </div>
          </div>
          <form class="chat-input" @submit.prevent="send">
            <input class="input" v-model="composeBody" :placeholder="t('chat.placeholder')" maxlength="2000" />
            <button class="btn btn-primary" :disabled="sending" type="submit">{{ t('common.send') }}</button>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>
