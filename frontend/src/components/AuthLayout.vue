<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, required: true },
  note: { type: String, required: true }
})
</script>

<template>
  <main class="auth-page">
    <section class="auth-panel" aria-labelledby="auth-title">
      <div class="auth-intro">
        <RouterLink :to="{ name: 'home' }" class="auth-brand" aria-label="ReNova home">
          <span aria-hidden="true">R</span>
          ReNova
        </RouterLink>
        <p class="auth-eyebrow">{{ $t('authUi.eyebrow') }}</p>
        <h1 id="auth-title">{{ title }}</h1>
        <p class="auth-subtitle">{{ subtitle }}</p>
        <p class="auth-note">{{ note }}</p>
      </div>
      <div class="auth-form-card">
        <slot />
      </div>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - var(--topbar-h)); padding: clamp(28px, 6vw, 72px) 20px;
  display: grid; place-items: center; overflow-x: hidden;
}
.auth-panel {
  width: min(100%, 850px); display: grid; grid-template-columns: minmax(0, .9fr) minmax(320px, 1.1fr);
  overflow: hidden; border: 0; border-radius: 24px;
  background: var(--bg-elevated); box-shadow: var(--shadow);
}
.auth-intro { padding: clamp(30px, 5vw, 56px); background: var(--primary-soft); }
.auth-brand { display: inline-flex; align-items: center; gap: 10px; color: var(--text); font-weight: 800; }
.auth-brand span {
  width: 34px; height: 34px; display: grid; place-items: center; border-radius: 50%;
  color: white; background: var(--primary); font-family: var(--font-display);
}
.auth-eyebrow { margin: 44px 0 12px; color: var(--primary-strong); font-size: 12px; font-weight: 800; letter-spacing: .1em; text-transform: uppercase; }
h1 { margin: 0; font-size: clamp(32px, 5vw, 48px); line-height: 1.05; }
.auth-subtitle { margin: 16px 0 0; color: var(--text-soft); font-size: 17px; line-height: 1.55; }
.auth-note { margin: 28px 0 0; padding-top: 24px; border-top: 1px solid var(--border); color: var(--text-soft); font-size: 13px; line-height: 1.6; }
.auth-form-card { min-width: 0; padding: clamp(30px, 5vw, 52px); }
@media (max-width: 700px) {
  .auth-page { padding: 20px 14px 32px; place-items: start center; }
  .auth-panel { grid-template-columns: 1fr; border-radius: 20px; }
  .auth-intro { padding: 26px 24px 24px; }
  .auth-eyebrow { margin-top: 24px; }
  .auth-note { display: none; }
  .auth-form-card { padding: 28px 24px 30px; }
}
</style>
