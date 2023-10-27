<template>
  <v-app-bar name="app-bar" :elevation="0" color="#252632">
    <v-app-bar-title>幸运日</v-app-bar-title>
    <v-card-subtitle>LUCK JOUNEY</v-card-subtitle>
    <div style="text-align: center;width: 80%;" class="mt-2">
      <v-autocomplete :items="items" style="width: 400px;margin: 0 auto; text-align: center;" auto-select-first
        class="flex-full-width" density="comfortable" item-props menu-icon="" placeholder="搜索视频/用户 (高级)"
        prepend-inner-icon="mdi-magnify" rounded hide-details theme="light"></v-autocomplete>
    </div>
    <v-btn variant="elevated">登录</v-btn>
  </v-app-bar>
</template>
<script setup>
</script>
<style scoped>
::v-deep(.v-field__outline) {
  --v-field-border-width: 0px
}
</style>