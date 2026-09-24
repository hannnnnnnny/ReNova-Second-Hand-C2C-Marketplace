<script setup>
import AppHeader from '../components/AppHeader.vue'
import AppFooter from '../components/AppFooter.vue'
import ToastContainer from '../components/ToastContainer.vue'
</script>

<template>
  <div>
    <AppHeader />
    <div id="main-content" tabindex="-1"><RouterView /></div>
    <AppFooter />
    <ToastContainer />
  </div>
</template>
