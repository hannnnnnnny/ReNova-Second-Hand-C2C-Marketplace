<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { userApi, reviewApi } from '../api/endpoints'
import { useToastStore } from '../stores/toast'
import { apiError } from '../api/client'
import { formatDate, formatRelative } from '../utils/format'
import Avatar from '../components/Avatar.vue'
import Stars from '../components/Stars.vue'
import ListingCard from '../components/ListingCard.vue'

const { t, locale } = useI18n()
const route = useRoute()
const toast = useToastStore()
const profile = ref(null)
const listings = ref([])
const reviews = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const id = route.params.id
    const [p, l, r] = await Promise.all([
      userApi.publicProfile(id),
      userApi.publicListings(id, { page: 0, size: 24 }),
      reviewApi.forUser(id)
    ])
    profile.value = p
    listings.value = l.content || []
    reviews.value = r
  } catch (err) { toast.error(apiError(err)) } finally { loading.value = false }
}

// derived counts shown as huge sticker stats
const stats = computed(() => {
  if (!profile.value) return []
  const activeCount = listings.value.filter((x) => x.status === 'ACTIVE').length
  const soldCount = listings.value.filter((x) => x.status === 'SOLD').length
  const ratingLabel = profile.value.ratingCount > 0
    ? profile.value.averageRating.toFixed(1)
    : '—'
  return [
    { num: activeCount, label: t('profile.statListings') },
    { num: ratingLabel, label: t('profile.statRating'), stars: true },
    { num: soldCount, label: t('profile.statSold') },
    { num: profile.value.ratingCount, label: t('profile.statReviews') }
  ]
})

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <main class="page profile-page">
    <div class="container" v-if="profile">
      <!-- ========== Editorial hero ========== -->
      <section class="profile-hero">
        <div class="profile-hero-meta">
          <div class="profile-hero-label">{{ t('profile.kicker') }}</div>
          <h1 class="profile-hero-name">{{ profile.displayName }}</h1>
          <div class="profile-hero-chips">
            <span v-if="profile.location" class="chip">📍 {{ profile.location }}</span>
            <span class="chip">🌱 {{ t('common.member') }} {{ formatDate(profile.memberSince, locale) }}</span>
          </div>
          <p v-if="profile.bio" class="profile-hero-quote">
            <span class="quote-mark" aria-hidden="true">“</span>{{ profile.bio }}
          </p>
        </div>
        <div class="profile-hero-avatar" aria-hidden="true">
          <Avatar :user="profile" size="lg" />
        </div>
      </section>

      <!-- ========== Sticker stats strip ========== -->
      <section class="stat-strip" aria-label="At a glance">
        <div
          v-for="(s, i) in stats" :key="i"
          class="stat-tile"
        >
          <div class="stat-num">{{ s.num }}</div>
          <div class="stat-label">{{ s.label }}</div>
          <Stars v-if="s.stars" :rating="profile.averageRating || 0" :size="13" />
        </div>
      </section>

      <!-- ========== Currently selling ========== -->
      <section class="profile-section">
        <header class="section-rule">
          <span class="section-label">{{ t('profile.currentlySelling') }}</span>
          <span class="section-count">{{ listings.length }}</span>
        </header>
        <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
        <div v-else-if="listings.length === 0" class="empty-state">{{ t('profile.noListings') }}</div>
        <div v-else class="grid grid-listings">
          <ListingCard v-for="l in listings" :key="l.id" :listing="l" />
        </div>
      </section>

      <!-- ========== What buyers said (pull-quote review cards) ========== -->
      <section class="profile-section">
        <header class="section-rule">
          <span class="section-label">{{ t('profile.whatBuyersSaid') }}</span>
          <span class="section-count">{{ reviews.length }}</span>
        </header>
        <div v-if="loading" class="muted">{{ t('common.loading') }}</div>
        <div v-else-if="reviews.length === 0" class="empty-state">{{ t('common.noReviews') }}</div>
        <div v-else class="reviews-grid">
          <article
            v-for="r in reviews" :key="r.id"
            class="review-pullquote"
          >
            <p class="review-text">{{ r.comment || t('profile.noComment') }}</p>
            <footer class="review-meta">
              <Stars :rating="r.rating" :size="14" />
              <div class="review-author">
                <Avatar :user="r.reviewer" />
                <div>
                  <div class="bold">{{ r.reviewer.displayName }}</div>
                  <div class="soft">
                    {{ t('offer.forListing') }}
                    <RouterLink :to="{ name: 'order-detail', params: { id: r.orderId } }">{{ r.listingTitle }}</RouterLink>
                    · {{ formatRelative(r.createdAt, locale) }}
                  </div>
                </div>
              </div>
            </footer>
          </article>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
/* Profile: large display name, flat stat tiles and quiet review cards.
   Stays usable on a 375px viewport. */

.profile-hero {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 28px;
  align-items: end;
  padding: 48px;
  margin-bottom: 32px;
  background: var(--bg-elevated);
  border: 0;
  border-radius: 24px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.profile-hero-meta { min-width: 0; }
.profile-hero-label {
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 17px;
  letter-spacing: -0.01em;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.profile-hero-name {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: clamp(2.4rem, 7vw, 4rem);
  line-height: 1.02;
  letter-spacing: -0.03em;
  margin: 0 0 18px;
  color: var(--text);
  word-break: break-word;
}

.profile-hero-chips {
  display: flex; flex-wrap: wrap; gap: 8px;
  margin-bottom: 18px;
}
.chip {
  display: inline-flex; align-items: center; gap: 4px;
  background: var(--bg-muted);
  border: 0;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 14px; font-weight: 400;
  color: var(--text);
}

.profile-hero-quote {
  position: relative;
  font-family: var(--font-body);
  font-weight: 400;
  font-size: clamp(1.05rem, 1.6vw, 1.3rem);
  line-height: 1.45;
  max-width: 620px;
  color: var(--text-muted);
  margin: 0;
}
.profile-hero-quote .quote-mark {
  font-family: var(--font-display);
  font-size: 1.4em;
  font-weight: 600;
  margin-right: 2px;
  color: var(--border-strong);
  vertical-align: -0.2em;
}

.profile-hero-avatar {
  flex-shrink: 0;
  position: relative;
}
.profile-hero-avatar :deep(.avatar-lg) {
  width: 128px; height: 128px;
  font-size: 44px;
}

/* ---- Stat strip ---- */
.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 40px;
}
.stat-tile {
  border-radius: var(--radius-lg);
  border: 0;
  padding: 24px;
  background: var(--bg-elevated);
  box-shadow: var(--shadow-sm);
  text-align: left;
  min-height: 110px;
  display: flex; flex-direction: column; justify-content: space-between;
}
.stat-num {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: clamp(2rem, 4vw, 2.6rem);
  line-height: 1;
  letter-spacing: -0.03em;
  color: var(--text);
}
.stat-label {
  font-size: 14px;
  font-weight: 400;
  color: var(--text-muted);
  margin-top: 10px;
}

/* ---- Section rules (replaces tabs) ---- */
.profile-section { margin-bottom: 72px; }
.section-rule {
  display: flex; align-items: baseline; gap: 14px;
  margin: 0 0 28px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}
.section-label {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: clamp(1.4rem, 2.6vw, 2rem);
  letter-spacing: -0.02em;
  color: var(--text);
}
.section-count {
  font-family: var(--font-display);
  font-weight: 400;
  font-size: 1.1rem;
  color: var(--text-soft);
  margin-left: auto;
}

/* ---- Pull-quote reviews ---- */
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 22px;
}
.review-pullquote {
  position: relative;
  background: var(--bg-elevated);
  border: 0;
  border-radius: var(--radius-lg);
  padding: 28px;
  box-shadow: var(--shadow-sm);
  display: flex; flex-direction: column; gap: 12px;
}
.review-text {
  font-family: var(--font-body);
  font-size: 1.05rem;
  line-height: 1.5;
  color: var(--text);
  margin: 6px 0 0;
}
.review-meta {
  display: flex; flex-direction: column; gap: 8px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
}
.review-author { display: flex; align-items: center; gap: 10px; }
.review-author .soft { font-size: 12px; }
.review-author a { color: var(--primary-strong); font-weight: 400; }
.review-author a:hover { text-decoration: underline; }

/* ============================================
   Mobile profile layout
   ============================================ */
@media (max-width: 760px) {
  .profile-hero {
    grid-template-columns: 1fr;
    padding: 24px 22px 22px;
    gap: 20px;
    align-items: start;
  }
  .profile-hero-avatar {
    order: -1;
    align-self: flex-start;
  }
  .profile-hero-avatar :deep(.avatar-lg) { width: 96px; height: 96px; font-size: 34px; }
  .profile-hero-name { font-size: clamp(2.1rem, 9vw, 2.8rem); margin-bottom: 14px; }
  .profile-hero-quote { font-size: 0.98rem; }

  .stat-strip {
    grid-template-columns: repeat(2, 1fr);
    margin-bottom: 32px;
  }
  .stat-tile { min-height: 96px; padding: 20px; }
  .stat-num { font-size: 1.9rem; }

  .reviews-grid { grid-template-columns: 1fr; gap: 18px; }
  .review-pullquote { padding: 24px 20px; }
}
</style>
