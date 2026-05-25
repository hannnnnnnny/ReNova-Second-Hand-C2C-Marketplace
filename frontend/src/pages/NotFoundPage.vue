<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>

<template>
  <main class="page">
    <div class="container text-center">
      <h1>404</h1>
      <p class="muted">{{ t('common.empty') }}</p>
      <RouterLink :to="{ name: 'home' }" class="btn btn-primary" style="margin-top: 16px;">{{ t('common.back') }}</RouterLink>
    </div>
  </main>
</template>
