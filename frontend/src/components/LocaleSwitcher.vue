<script setup>
import { useI18n } from 'vue-i18n'
import { AVAILABLE_LOCALES, setLocale } from '../i18n'

const { locale } = useI18n()
function choose(code) { setLocale(code) }
</script>

<template>
  <div class="locale-switch" role="group" aria-label="Language">
    <button v-for="l in AVAILABLE_LOCALES" :key="l.code" :class="{ 'is-active': locale === l.code }" @click="choose(l.code)" type="button">
      {{ l.code === 'en' ? 'EN' : '中文' }}
    </button>
  </div>
</template>
