<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { userApi, reviewApi, listingApi } from '../api/endpoints'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatDate, formatRelative } from '../utils/format'
import Avatar from '../components/Avatar.vue'
import Stars from '../components/Stars.vue'
import ListingCard from '../components/ListingCard.vue'

const { t, locale } = useI18n()
const auth = useAuthStore()
const toast = useToastStore()

const me = ref(null)
const listings = ref([])
const reviews = ref([])
const editing = ref(false)
const saving = ref(false)
const form = ref({ displayName: '', avatarUrl: '', bio: '', location: '' })

async function load() {
  try {
    me.value = await userApi.me()
    form.value = {
      displayName: me.value.displayName || '',
      avatarUrl: me.value.avatarUrl || '',
      bio: '',
      location: me.value.location || ''
    }
    const [profile, l, r] = await Promise.all([
      userApi.publicProfile(me.value.id),
      listingApi.mine({ page: 0, size: 24 }),
      reviewApi.forUser(me.value.id)
    ])
    form.value.bio = profile.bio || ''
    listings.value = l.content || []
    reviews.value = r
  } catch (err) { toast.error(apiError(err)) }
}

async function save() {
  saving.value = true
  try {
    const updated = await userApi.updateMe(form.value)
    auth.setUser(updated)
    me.value = updated
    toast.success(t('common.save'))
    editing.value = false
    // also re-fetch public profile so bio reflects
    const pub = await userApi.publicProfile(updated.id)
    form.value.bio = pub.bio || ''
  } catch (err) { toast.error(apiError(err)) } finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <main class="page">
    <div class="container" v-if="me">
      <div class="panel" style="margin-bottom: 24px">
        <div class="row" style="gap: 20px">
          <Avatar :user="me" size="lg" />
          <div class="grow">
            <div class="row" style="justify-content:space-between">
              <h1>{{ me.displayName }}</h1>
              <button class="btn btn-outline btn-sm" @click="editing = !editing" type="button">{{ editing ? t('common.cancel') : t('profile.editProfile') }}</button>
            </div>
            <div class="muted">{{ me.email }}</div>
            <div v-if="me.location" class="muted">📍 {{ me.location }}</div>
          </div>
        </div>

        <div v-if="editing" class="divider"></div>
        <form v-if="editing" @submit.prevent="save">
          <div class="form-grid">
            <div class="field">
              <label class="label">{{ t('profile.displayName') }}</label>
              <input class="input" v-model="form.displayName" required maxlength="80" />
            </div>
            <div class="field">
              <label class="label">{{ t('profile.location') }}</label>
              <input class="input" v-model="form.location" maxlength="120" />
            </div>
          </div>
          <div class="field">
            <label class="label">{{ t('profile.avatarUrl') }}</label>
            <input class="input" v-model="form.avatarUrl" maxlength="500" placeholder="https://..." />
          </div>
          <div class="field">
            <label class="label">{{ t('profile.bio') }}</label>
            <textarea class="textarea" v-model="form.bio" maxlength="600"></textarea>
          </div>
          <button class="btn btn-primary" :disabled="saving" type="submit">{{ saving ? t('common.saving') : t('profile.save') }}</button>
        </form>
      </div>

      <section class="section">
        <h2 style="margin-bottom: 16px">{{ t('profile.listings') }}</h2>
        <div v-if="listings.length === 0" class="empty-state">{{ t('profile.noListings') }}</div>
        <div v-else class="grid grid-listings">
          <ListingCard v-for="l in listings" :key="l.id" :listing="l" />
        </div>
      </section>

      <section class="section">
        <h2 style="margin-bottom: 16px">{{ t('profile.reviewsReceived') }}</h2>
        <div v-if="reviews.length === 0" class="empty-state">{{ t('common.noReviews') }}</div>
        <div v-else class="stack">
          <div v-for="r in reviews" :key="r.id" class="card">
            <div class="row" style="justify-content:space-between; margin-bottom: 8px">
              <div class="row">
                <Avatar :user="r.reviewer" />
                <div>
                  <div class="bold">{{ r.reviewer.displayName }}</div>
                  <Stars :rating="r.rating" :size="14" />
                </div>
              </div>
              <div class="soft">{{ formatRelative(r.createdAt, locale) }}</div>
            </div>
            <p style="margin: 0">{{ r.comment || '—' }}</p>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
