<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const year = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <div class="container">
      <div class="footer-top">
        <div><RouterLink :to="{ name: 'home' }" class="brand"><span class="brand-mark">R</span><span>ReNova</span></RouterLink><p class="footer-tagline">{{ t('brand.tagline') }}</p></div>
        <nav class="footer-links" aria-label="ReNova">
          <RouterLink :to="{ name: 'browse' }">{{ t('common.browse') }}</RouterLink>
          <RouterLink :to="{ name: 'post-listing' }">{{ t('common.sell') }}</RouterLink>
          <RouterLink :to="{ name: 'orders' }">{{ t('common.orders') }}</RouterLink>
        </nav>
      </div>
      <div class="footer-bottom"><span>© {{ year }} ReNova · {{ t('marketplaceUi.footerCopyright') }}</span><span>{{ t('marketplaceUi.footerNote') }}</span></div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer { padding: 56px 0 32px; background: var(--bg); border-top: 1px solid var(--border); }
.footer-top { display: flex; justify-content: space-between; gap: 24px; align-items: center; padding-bottom: 25px; }
.footer-tagline { color: var(--text-muted); font-size: 13px; margin: 10px 0 0; }
.footer-links { display: flex; gap: 28px; font-size: 13px; font-weight: 400; color: var(--text-muted); }
.footer-links a { padding: 8px 0; }
.footer-links a:hover { color: var(--primary-strong); text-decoration: underline; text-underline-offset: 4px; }
.footer-bottom { border-top: 1px solid var(--border); padding-top: 18px; display: flex; justify-content: space-between; gap: 20px; color: var(--text-muted); font-size: 11px; }
@media (max-width: 760px) {
  .footer-top { align-items: flex-start; flex-direction: column; gap: 16px; }
  .footer-links { gap: 24px; }
  .footer-bottom { flex-direction: column; gap: 7px; }
}
</style>
