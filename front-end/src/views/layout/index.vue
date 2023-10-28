<template>
  <v-app class="rounded rounded-md">
    <defaultHeader />
    <navigation />
    <v-main class="align-center justify-center" style="min-height: 100%;">
      <router-view />
    </v-main>
<!-- 
    <v-footer app name="footer">
    </v-footer> -->
  </v-app>
</template>
<script setup>

import defaultHeader from './header.vue';
import navigation from './navigation.vue';
</script>