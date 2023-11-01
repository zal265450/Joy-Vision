<template>
  <v-app full-height v-if="videoInfo">
    <v-navigation-drawer app v-model="drawer" location="right" :width="350">
      <v-card class="mx-auto" max-width="344" elevation="0">
        <v-img :src="props.videoInfo.cover" height="200px" cover></v-img>
        <v-card-title>
          {{ props.videoInfo.title }}
        </v-card-title>

        <v-card-subtitle>
          <v-row>
            <v-col>
              {{ props.videoInfo.historyCount }} 播放
            </v-col>
            <v-col>
              {{ props.videoInfo.historyCount }} 点赞
            </v-col>
            <v-col>
              {{ props.videoInfo.historyCount }} 收藏
            </v-col>
          </v-row>
        </v-card-subtitle>

        <v-card-actions>
          <v-btn color="orange-lighten-2" variant="text">
            描述
          </v-btn>

          <v-spacer></v-spacer>

          <v-btn :icon="showDescription ? 'mdi-chevron-up' : 'mdi-chevron-down'"
            @click="showDescription = !showDescription"></v-btn>
        </v-card-actions>

        <v-expand-transition>
          <div v-show="showDescription">
            <v-divider></v-divider>

            <v-card-text>
              {{ props.videoInfo.description || "作者很懒，没有给一点描述" }}
            </v-card-text>
          </div>
        </v-expand-transition>
      </v-card>
    </v-navigation-drawer>
    <v-main>
      <v-card :height="videoHeight" :width="videoWidth" rounded="0">
        <video ref="video" class="video-js vjs-default-skin" controls :poster="props.videoInfo.cover">
          <source :src="props.videoInfo.url" />
        </video>
        <div style="position: absolute;left: 15px;top: 15px;z-index: 99999;">
          <v-btn size="40" color="bg" icon @click="closeVideo">
            <v-icon :size="20">mdi-close</v-icon>
          </v-btn>
        </div>
        <v-card class="pa-1" elevation="0" style="display: flex; flex-direction: column;
    gap: 10px;position: absolute; background-color: transparent; right: 20px; bottom: 65px;z-index: 99999;">
          <v-avatar class="elevation-2" image="/logo.png"></v-avatar>
          <v-btn size="40" color="bg" icon @click="drawer = !drawer">
            <v-icon :size="20">mdi-more</v-icon>
          </v-btn>
          <v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-heart</v-icon>
          </v-btn><v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-star</v-icon>
          </v-btn>
          <v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-near-me</v-icon>
          </v-btn>
        </v-card>
      </v-card>
    </v-main>
  </v-app>
</template>
<script setup>
import { computed, getCurrentInstance, onMounted, ref } from 'vue';

const props = defineProps({
  videoInfo: {
    type: Object,
    default: null
  },
  closeVideo: {
    type: Function,
    default: ()=>{}
  }
})
const showDescription = ref(true)
const drawer = ref(false)
const instance = getCurrentInstance().proxy
const video = ref()
const windowHeight = ref(document.body.clientHeight)
const windowWidth = ref(document.body.clientWidth)
onMounted(() => {
  window.onresize = () => { }
  window.onresize = () => {
    windowHeight.value = document.body.clientHeight
    windowWidth.value = document.body.clientWidth
  }
  instance.$video(video.value, {
    fill: true, userActions: {
      hotkeys: (event) => {

      }
    }
  })
})
const videoHeight = computed(() => {
  return windowHeight.value
})
const videoWidth = computed(() => {
  return windowWidth.value - (drawer.value ? 350 : 0)
})
</script>   