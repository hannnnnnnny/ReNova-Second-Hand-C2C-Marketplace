<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { conversationApi, listingApi, userApi } from '../api/endpoints'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatPrice, formatRelative } from '../utils/format'
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

// ---- compose-new-conversation state ----
// Activated when the route name is 'compose-message' with ?listingId=…
const composeMode = computed(() => route.name === 'compose-message')
const composeAnchor = ref(null)         // the listing the user came from
const composeOthers = ref([])           // other active listings by the same seller
const composeSelectedId = ref(null)     // the listing id the user actually wants to ask about
const composeNote = ref('')             // textarea body in compose mode
const composeLoading = ref(false)

const activeId = computed(() => {
  if (composeMode.value) return null
  return Number(route.params.id) || null
})

const composeSeller = computed(() => composeAnchor.value?.seller || null)
const composeSelectedListing = computed(() => {
  if (!composeAnchor.value) return null
  if (composeSelectedId.value === composeAnchor.value.id) return composeAnchor.value
  return composeOthers.value.find((l) => l.id === composeSelectedId.value) || composeAnchor.value
})

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

// ---- compose flow ----
async function loadComposeData() {
  const lid = Number(route.query.listingId)
  if (!lid) {
    // No listing context given: bail back to the message list.
    router.replace({ name: 'messages' })
    return
  }
  composeLoading.value = true
  composeOthers.value = []
  composeAnchor.value = null
  composeSelectedId.value = lid
  composeNote.value = ''
  try {
    const listing = await listingApi.get(lid)
    // Can't message yourself.
    if (auth.user?.id === listing.seller?.id) {
      toast.error(apiError({ response: { data: { message: 'You cannot message yourself.' } } }))
      router.replace({ name: 'listing-detail', params: { id: lid } })
      return
    }
    composeAnchor.value = listing
    if (listing.seller?.id) {
      try {
        const more = await userApi.publicListings(listing.seller.id, { page: 0, size: 8 })
        composeOthers.value = (more.content || [])
          .filter((l) => l.id !== listing.id && l.status === 'ACTIVE')
      } catch {
        composeOthers.value = []
      }
    }
  } catch (err) {
    toast.error(apiError(err))
    router.replace({ name: 'messages' })
  } finally {
    composeLoading.value = false
  }
}

function selectAnchor(id) {
  composeSelectedId.value = id
}

async function startConversation() {
  const body = composeNote.value.trim()
  if (!body) return
  const target = composeSelectedListing.value
  if (!target) return
  sending.value = true
  try {
    const detail = await conversationApi.start({ listingId: target.id, body })
    // After send: jump straight to the actual thread.
    router.replace({ name: 'conversation', params: { id: detail.conversation.id } })
  } catch (err) {
    toast.error(apiError(err))
  } finally {
    sending.value = false
  }
}

// ---- route reactions ----
watch(activeId, async (id) => {
  if (id) await openConversation(id)
})

watch(
  () => [route.name, route.query.listingId],
  async ([name]) => {
    if (name === 'compose-message') {
      await loadComposeData()
    }
  }
)

onMounted(async () => {
  await loadList()
  if (composeMode.value) {
    await loadComposeData()
  } else if (activeId.value) {
    await openConversation(activeId.value)
  } else if (conversations.value.length > 0) {
    await openConversation(conversations.value[0].id)
  }
})
</script>

<template>
  <main class="page">
    <div class="container">
      <h1 style="margin-bottom: 24px">{{ t('chat.title') }}</h1>

      <div v-if="loading && !composeMode" class="muted">{{ t('common.loading') }}</div>
      <div v-else-if="!composeMode && conversations.length === 0" class="empty-state">{{ t('chat.empty') }}</div>

      <div v-else class="chat-shell">
        <div class="chat-list">
          <RouterLink
            v-for="c in conversations" :key="c.id"
            :to="{ name: 'conversation', params: { id: c.id } }"
            class="chat-list-item"
            :class="{ 'is-active': !composeMode && active?.conversation?.id === c.id }"
          >
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
          </RouterLink>
        </div>

        <!-- ===== COMPOSE MODE ===== -->
        <div v-if="composeMode" class="chat-thread">
          <div v-if="composeLoading" class="muted" style="padding: 24px">{{ t('common.loading') }}</div>
          <template v-else-if="composeAnchor">
            <div class="chat-thread-head">
              <Avatar :user="composeSeller" />
              <div class="grow">
                <div class="bold">{{ t('chat.composeTitle', { name: composeSeller?.displayName }) }}</div>
                <div class="soft">
                  <RouterLink :to="{ name: 'profile', params: { id: composeSeller?.id } }">{{ composeSeller?.location || '' }}</RouterLink>
                </div>
              </div>
              <RouterLink :to="{ name: 'messages' }" class="btn btn-ghost btn-sm">{{ t('chat.back') }}</RouterLink>
            </div>

            <div class="compose-body">
              <!-- Selected (anchor) listing card -->
              <div class="compose-section">
                <div class="label" style="margin-bottom: 8px">{{ t('chat.aboutThisListing') }}</div>
                <div class="compose-anchor">
                  <div class="thumb"
                       :style="{ backgroundImage: composeSelectedListing?.coverImageUrl ? `url('${composeSelectedListing.coverImageUrl}')` : '' }"></div>
                  <div class="grow">
                    <div class="bold">{{ composeSelectedListing?.title }}</div>
                    <div class="soft">{{ formatPrice(composeSelectedListing?.price) }} · {{ t(`condition.${composeSelectedListing?.condition}`) }}</div>
                  </div>
                  <RouterLink
                    v-if="composeSelectedListing?.id"
                    :to="{ name: 'listing-detail', params: { id: composeSelectedListing.id } }"
                    class="btn btn-ghost btn-sm">
                    {{ t('common.view') }}
                  </RouterLink>
                </div>
              </div>

              <!-- Other listings from the same seller -->
              <div v-if="composeOthers.length > 0" class="compose-section">
                <div class="label" style="margin-bottom: 8px">
                  {{ t('chat.switchListing', { name: composeSeller?.displayName }) }}
                </div>
                <div class="compose-other-row">
                  <button
                    v-for="l in composeOthers" :key="l.id" type="button"
                    class="compose-other-card"
                    :class="{ 'is-active': l.id === composeSelectedId }"
                    @click="selectAnchor(l.id)"
                  >
                    <div class="thumb"
                         :style="{ backgroundImage: l.coverImageUrl ? `url('${l.coverImageUrl}')` : '' }"></div>
                    <div class="title">{{ l.title }}</div>
                    <div class="price">{{ formatPrice(l.price) }}</div>
                  </button>

                  <!-- Always offer to switch back to the original anchor in case the user clicked away -->
                  <button
                    v-if="composeAnchor && composeSelectedId !== composeAnchor.id"
                    type="button"
                    class="compose-other-card is-pinned"
                    @click="selectAnchor(composeAnchor.id)"
                  >
                    <div class="thumb"
                         :style="{ backgroundImage: composeAnchor.imageUrls?.[0] ? `url('${composeAnchor.imageUrls[0]}')` : '' }"></div>
                    <div class="title">{{ composeAnchor.title }}</div>
                    <div class="price">{{ formatPrice(composeAnchor.price) }}</div>
                  </button>
                </div>
                <div v-if="composeSelectedId !== composeAnchor?.id" class="soft" style="margin-top: 6px">
                  {{ t('chat.switched', { title: composeSelectedListing?.title }) }}
                </div>
              </div>
              <div v-else class="soft" style="margin-top: 4px">{{ t('chat.noOtherListings') }}</div>

              <!-- The message itself -->
              <div class="compose-section">
                <div class="soft" style="margin-bottom: 8px">{{ t('chat.composeHint') }}</div>
                <textarea
                  class="textarea"
                  v-model="composeNote"
                  :placeholder="t('chat.placeholder')"
                  maxlength="2000"
                  rows="5"
                ></textarea>
              </div>

              <div class="row" style="justify-content: flex-end">
                <button
                  class="btn btn-primary"
                  type="button"
                  :disabled="sending || !composeNote.trim() || !composeSelectedListing"
                  @click="startConversation"
                >
                  {{ t('chat.startConversation') }}
                </button>
              </div>
            </div>
          </template>
        </div>

        <!-- ===== EXISTING THREAD VIEW ===== -->
        <div class="chat-thread" v-else-if="active">
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

<style scoped>
.compose-body {
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}
.compose-section { display: flex; flex-direction: column; }
.compose-anchor {
  display: flex; align-items: center; gap: 14px;
  border: 1px solid var(--border, #e6e2da);
  background: var(--card-bg, #fff);
  border-radius: 14px;
  padding: 12px 14px;
}
.compose-anchor .thumb {
  width: 64px; height: 64px; border-radius: 10px;
  background-size: cover; background-position: center;
  background-color: #efeae0;
  flex-shrink: 0;
}
.compose-other-row {
  display: flex; gap: 12px; overflow-x: auto; padding-bottom: 6px;
  scrollbar-width: thin;
}
.compose-other-card {
  flex: 0 0 130px;
  display: flex; flex-direction: column; gap: 4px;
  background: var(--card-bg, #fff);
  border: 1.5px solid var(--border, #e6e2da);
  border-radius: 12px;
  padding: 8px;
  cursor: pointer;
  text-align: left;
  transition: border-color 120ms, transform 120ms;
}
.compose-other-card:hover { border-color: var(--brand, #2c6f47); transform: translateY(-1px); }
.compose-other-card.is-active { border-color: var(--brand, #2c6f47); box-shadow: 0 0 0 3px rgba(44, 111, 71, 0.15); }
.compose-other-card.is-pinned { border-style: dashed; }
.compose-other-card .thumb {
  width: 100%; aspect-ratio: 1; border-radius: 8px;
  background-size: cover; background-position: center;
  background-color: #efeae0;
}
.compose-other-card .title {
  font-size: 12px; line-height: 1.35;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.compose-other-card .price { font-size: 13px; font-weight: 600; }
</style>
