<template>
    <v-navigation-drawer
        >
          <v-list>
            <v-list-item
              prepend-avatar="/logo.png"
              title="七牛云-小虎"
              subtitle="xiaohu@qiniu.com"
            ></v-list-item>
          </v-list>
  
          <v-divider></v-divider>
  
          <v-list density="compact" nav>
            <v-list-item prepend-icon="mdi-home" title="首页" value="home"></v-list-item>
            <v-list-item prepend-icon="mdi-video" title="推荐视频(高级)" value="video"></v-list-item>
            <v-list-item prepend-icon="mdi-label-multiple" title="视频分类" value="fire"></v-list-item>
            <v-list-item prepend-icon="mdi-account-multiple" title="粉丝/关注(高级)" value="shared"></v-list-item>
            <v-list-item prepend-icon="mdi-star" title="收藏夹(高级)" value="starred"></v-list-item>
            
          </v-list>
        </v-navigation-drawer>
  </template>