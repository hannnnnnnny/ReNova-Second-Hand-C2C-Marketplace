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
    { num: activeCount, label: t('profile.statListings'), tone: 'green', tilt: -2 },
    { num: ratingLabel, label: t('profile.statRating'), tone: 'gold', tilt: 1.5, stars: true },
    { num: soldCount, label: t('profile.statSold'), tone: 'coral', tilt: -1 },
    { num: profile.value.ratingCount, label: t('profile.statReviews'), tone: 'cream', tilt: 2 }
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
          :class="`stat-tone-${s.tone}`"
          :style="{ '--tilt': `${s.tilt}deg` }"
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
            v-for="(r, i) in reviews" :key="r.id"
            class="review-pullquote"
            :style="{ '--tilt': `${(i % 2 === 0 ? -0.6 : 0.8)}deg` }"
          >
            <div class="review-mark" aria-hidden="true">“</div>
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
/* ====================================================================
   Editorial profile — Anton-Skvortsov-flavored composition layered on
   the existing Animal-Crossing palette. Big display type, asymmetric
   hero, sticker-style stat tiles with subtle tilt, pull-quote reviews.
   Stays usable on a 375px viewport.
   ==================================================================== */

.profile-hero {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 28px;
  align-items: end;
  padding: 36px 36px 28px;
  margin-bottom: 28px;
  background:
    radial-gradient(circle at 100% 0%, rgba(255, 255, 255, 0.5) 0%, transparent 45%),
    linear-gradient(135deg, var(--bg-elevated) 0%, var(--primary-soft) 100%);
  border: 2px solid var(--border-strong);
  border-radius: var(--radius-lg);
  box-shadow: 0 6px 0 var(--border-strong), var(--shadow);
  overflow: hidden;
}
.profile-hero::before {
  /* A big faint number-tile glyph in the background, magazine-style */
  content: "★";
  position: absolute; top: -36px; right: 20px;
  font-family: var(--font-display);
  font-size: 220px; font-weight: 800;
  color: rgba(74, 58, 34, 0.05);
  line-height: 1; pointer-events: none;
}

.profile-hero-meta { min-width: 0; }
.profile-hero-label {
  font-family: var(--font-body);
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--primary-strong);
  margin-bottom: 6px;
}
.profile-hero-name {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: clamp(2.4rem, 7vw, 4.4rem);
  line-height: 0.95;
  letter-spacing: -0.02em;
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
  background: var(--bg-elevated);
  border: 2px solid var(--border-strong);
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 12.5px; font-weight: 700;
  color: var(--text);
  box-shadow: 0 2px 0 var(--border-strong);
}

.profile-hero-quote {
  position: relative;
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 500;
  font-size: clamp(1rem, 1.6vw, 1.2rem);
  line-height: 1.45;
  max-width: 620px;
  color: var(--text);
  padding-left: 14px;
  border-left: 4px solid var(--primary);
  margin: 0;
}
.profile-hero-quote .quote-mark {
  font-family: var(--font-display);
  font-size: 1.6em;
  font-weight: 800;
  margin-right: 4px;
  color: var(--primary-strong);
  vertical-align: -0.2em;
}

.profile-hero-avatar {
  flex-shrink: 0;
  transform: rotate(-4deg);
  position: relative;
}
.profile-hero-avatar :deep(.avatar-lg) {
  width: 128px; height: 128px;
  font-size: 44px;
  border-width: 4px;
  box-shadow: 0 6px 0 var(--border-strong);
}
/* a little pinned-paper "tape" accent under the avatar */
.profile-hero-avatar::after {
  content: "";
  position: absolute;
  inset: auto 50% -10px 50%;
  transform: translateX(-50%) rotate(2deg);
  width: 56px; height: 14px;
  background: rgba(224, 122, 95, 0.55);
  border-radius: 4px;
  box-shadow: var(--shadow-sm);
}

/* ---- Sticker stat strip ---- */
.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 40px;
}
.stat-tile {
  border-radius: var(--radius);
  border: 2px solid var(--border-strong);
  padding: 18px 16px 16px;
  background: var(--bg-elevated);
  box-shadow: 0 4px 0 var(--border-strong);
  transform: rotate(var(--tilt, 0deg));
  transition: transform 160ms ease;
  text-align: left;
  min-height: 110px;
  display: flex; flex-direction: column; justify-content: space-between;
}
.stat-tile:hover { transform: rotate(0deg) translateY(-2px); }
.stat-num {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: clamp(2rem, 4vw, 2.6rem);
  line-height: 1;
  letter-spacing: -0.03em;
  color: var(--text);
}
.stat-label {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-top: 10px;
}
.stat-tone-green { background: var(--primary-soft); border-color: var(--primary-strong); box-shadow: 0 4px 0 var(--primary-strong); }
.stat-tone-green .stat-num { color: var(--primary-strong); }
.stat-tone-coral { background: var(--accent-soft); border-color: #b85731; box-shadow: 0 4px 0 #b85731; }
.stat-tone-coral .stat-num { color: #a14322; }
.stat-tone-gold { background: #fbe9c0; border-color: #b58217; box-shadow: 0 4px 0 #b58217; }
.stat-tone-gold .stat-num { color: #8a6315; }
.stat-tone-cream { background: var(--bg-elevated); }

/* ---- Section rules (replaces tabs) ---- */
.profile-section { margin-bottom: 48px; }
.section-rule {
  display: flex; align-items: baseline; gap: 14px;
  margin: 0 0 22px;
  padding-bottom: 12px;
  border-bottom: 3px dashed var(--border-strong);
}
.section-label {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: clamp(1.2rem, 2.2vw, 1.6rem);
  letter-spacing: -0.01em;
  color: var(--text);
}
.section-count {
  font-family: var(--font-display);
  font-weight: 800;
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
  border: 2px solid var(--border-strong);
  border-radius: var(--radius-lg);
  padding: 24px 22px 20px;
  box-shadow: 0 4px 0 var(--border-strong);
  transform: rotate(var(--tilt, 0deg));
  transition: transform 160ms ease;
  display: flex; flex-direction: column; gap: 12px;
}
.review-pullquote:hover { transform: rotate(0deg) translateY(-2px); }
.review-mark {
  position: absolute; top: -18px; left: 18px;
  width: 44px; height: 44px;
  background: var(--primary);
  color: #fff;
  border: 2px solid var(--primary-strong);
  border-radius: 50%;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 32px;
  display: flex; align-items: center; justify-content: center;
  line-height: 1; padding-bottom: 6px;
  box-shadow: 0 3px 0 var(--primary-strong);
}
.review-text {
  font-family: var(--font-display);
  font-size: 1.05rem;
  font-style: italic;
  line-height: 1.5;
  color: var(--text);
  margin: 6px 0 0;
}
.review-meta {
  display: flex; flex-direction: column; gap: 8px;
  padding-top: 10px;
  border-top: 2px dashed var(--border);
}
.review-author { display: flex; align-items: center; gap: 10px; }
.review-author .soft { font-size: 12px; }
.review-author a { color: var(--primary-strong); font-weight: 700; }

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
  .profile-hero::before { font-size: 140px; top: -10px; right: 4px; }
  .profile-hero-avatar {
    order: -1;
    transform: rotate(-3deg);
    align-self: flex-start;
  }
  .profile-hero-avatar :deep(.avatar-lg) { width: 96px; height: 96px; font-size: 34px; }
  .profile-hero-name { font-size: clamp(2.1rem, 9vw, 2.8rem); margin-bottom: 14px; }
  .profile-hero-quote { font-size: 0.98rem; }

  .stat-strip {
    grid-template-columns: repeat(2, 1fr);
    margin-bottom: 32px;
  }
  .stat-tile {
    min-height: 96px;
    transform: rotate(calc(var(--tilt, 0deg) * 0.5));
  }
  .stat-num { font-size: 1.9rem; }

  .reviews-grid { grid-template-columns: 1fr; gap: 18px; }
  .review-pullquote { transform: rotate(0deg); padding: 22px 20px 18px; }
  .review-mark { width: 40px; height: 40px; font-size: 28px; left: 14px; top: -16px; }
}
</style>
