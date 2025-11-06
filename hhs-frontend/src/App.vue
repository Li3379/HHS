<template>
  <div id="app">
    <AppHeader />
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from "@/components/AppHeader.vue";
</script>

<style scoped>
#app {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main {
  padding: 24px 0;
}
</style>
