<template>
  <v-navigation-drawer color="#252632">
    <v-list>
      <v-list-item prepend-avatar="/logo.png" title="七牛云-小虎" subtitle="xiaohu@qiniu.com"></v-list-item>
    </v-list>


    <v-list density="compact" nav>
      <v-list-item prepend-icon="mdi-home" title="首页" to="/"></v-list-item>
      <!-- <v-list-item prepend-icon="mdi-video" title="推荐视频(高级)" to="/video"></v-list-item> -->
      <v-list-item prepend-icon="mdi-label-multiple" title="视频分类" to="/classify"></v-list-item>
      <v-list-item prepend-icon="mdi-account-multiple" title="粉丝/关注(高级)" to="/friends"></v-list-item>
      <v-list-item prepend-icon="mdi-star" title="收藏夹(高级)" to="/favorites"></v-list-item>
      <v-list-item prepend-icon="mdi-upload" title="创作中心" to="/my"></v-list-item>

    </v-list>
  </v-navigation-drawer>
</template>
<style lang="scss" scoped>
.v-navigation-drawer{
  border: none !important;

}
</style>
