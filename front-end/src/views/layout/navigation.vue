<template>
  <v-navigation-drawer permanent color="#252632">
    <v-list>
      <v-list-item prepend-avatar="/logo.png" title="七牛云-小虎" subtitle="xiaohu@qiniu.com"></v-list-item>
    </v-list>


    <v-list density="compact" nav>
      <v-list-item prepend-icon="mdi-home" title="首页" to="/"></v-list-item>
      <!-- <v-list-item prepend-icon="mdi-video" title="推荐视频(高级)" to="/video"></v-list-item> -->
      <!-- <v-list-item prepend-icon="mdi-label-multiple" title="视频分类" to="/classify"></v-list-item> -->
      <v-list-item prepend-icon="mdi-upload" title="个人中心" to="/user"></v-list-item>
      <v-list-item :prepend-icon="item.icon || 'mdi-file-document-alert-outline'" :title="item.name" v-for="item in allClassifyList" :to="`/video/${item.id}`"></v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>
<script setup>
import { ref } from 'vue';
import { apiClassifyGetAll } from '../../apis/classify';
const allClassifyList = ref([])
apiClassifyGetAll().then(({ data }) => {
        if (!data.state) {
            allClassifyList.value = []
            return;
        }
        allClassifyList.value = data.data
    })
</script>
<style lang="scss" scoped>
.v-navigation-drawer{
  border: none !important;

}
</style>
