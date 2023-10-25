<template>
    <v-app-bar color="blue" name="app-bar" :elevation="0" title="Luck jouney">
      <div style="text-align: center;width: 80%;margin-top: 20px;">
        <v-autocomplete
        :items="items"
        
        style="width: 300px;margin: 0 auto; text-align: center;"
        append-inner-icon="mdi-microphone"
        auto-select-first
        class="flex-full-width"
        density="comfortable"
        item-props
        menu-icon=""
        placeholder="搜索视频/用户"
        prepend-inner-icon="mdi-magnify"
        rounded
        theme="light"
      ></v-autocomplete>
      </div>
      <v-btn :elevation="2">登录</v-btn>
      </v-app-bar>
</template>
<script setup>
</script>