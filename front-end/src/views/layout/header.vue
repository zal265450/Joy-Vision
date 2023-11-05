<template>
  <v-app-bar name="app-bar" :elevation="0" color="#252632">
    <v-app-bar-title>幸运日</v-app-bar-title>
    <v-card-subtitle>LUCK JOUNEY</v-card-subtitle>
    <div style="text-align: center;width: 80%;" class="mt-2">
      <v-text-field v-model="searchKey" v-on:keyup.enter="search" style="width: 400px;margin: 0 auto; text-align: center;" 
        class="flex-full-width" :density="'comfortable'" item-props menu-icon="" placeholder="搜索视频/用户 (高级)"
        prepend-inner-icon="mdi-magnify" rounded hide-details theme="light" ></v-text-field>
    </div>
    <v-toolbar-items variant="plain" class="pr-2">
    <auth/>
    </v-toolbar-items>
    
  </v-app-bar>
</template>
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import auth from '../auth/index.vue';
const searchKey = ref("")
const router = useRouter()
const search = ()=>{
  router.push({
    path: "/video/search/"+searchKey.value,
  })
}
</script>
<style scoped>
::v-deep(.v-field__outline) {
  --v-field-border-width: 0px
}
</style>