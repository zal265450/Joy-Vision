<template>
    <v-layout ref="app" class="rounded rounded-md">
  <defaultHeader/>
      <navigation/>
  
      <v-main class="d-flex align-center justify-center" style="min-height: 100%;">
        <router-view/>
      </v-main>
  
      <v-footer
        app
        name="footer"
      >
       <!-- 脚内容, 暂无 -->
      </v-footer>
    </v-layout>
</template>
<script setup>

import defaultHeader from './header.vue';
import navigation from './navigation.vue';
</script>