<template>
  <v-app v-if="videoInfo">
    <v-navigation-drawer permanent app v-model="drawer" location="right" :width="350" style="background-color: #252632;">
      <!-- <v-card class="mx-auto" max-width="344" elevation="0">
        <v-img :src="currentVideo.cover" height="200px" cover></v-img>
        <v-card-title>
          {{ currentVideo.title }}
        </v-card-title>

        <v-card-subtitle>
          <v-row>
            <v-col>
              {{ currentVideo.historyCount }} 播放
            </v-col>
            <v-col>
              {{ currentVideo.historyCount }} 点赞
            </v-col>
            <v-col>
              {{ currentVideo.historyCount }} 收藏
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
              {{ currentVideo.description || "作者很懒，没有给一点描述" }}
            </v-card-text>
          </div>
        </v-expand-transition>
      </v-card> -->
      <div class="pa-4 ">
        <VideoCard :overlay="currentIndex == index" class="mb-4" :video-info="videoItem"
          v-for="(videoItem, index) in similarList" :key="index" @click="currentIndex = index" />
      </div>
    </v-navigation-drawer>
    <v-main>
      <v-card :height="videoHeight" :width="videoWidth" rounded="0">
        <video ref="video" class="video-js vjs-default-skin" controls :poster="currentVideo.cover">
          <source :src="currentVideo.url" :type="currentVideo.videoType" />
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
          <v-btn size="40" color="bg" icon @click="starVideo()">
            <v-icon :size="20">mdi-heart</v-icon>
          </v-btn>
          <FavoriteCom :video-id="currentVideo.id" :callback="favoriteCallBack">
            <template #default="{ props }">
              <v-btn v-bind="props" size="40" color="bg" icon>
                <v-icon :size="20">mdi-star</v-icon>
              </v-btn>
            </template>
          </FavoriteCom>
          <v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-near-me</v-icon>
          </v-btn>
        </v-card>
      </v-card>
      <v-snackbar v-model="snackbar.show" :color="snackbar.color">
        {{ snackbar.text }}

        <template v-slot:actions>
          <v-btn color="blue" variant="text" @click="snackbar.show = false">
            了解
          </v-btn>
        </template>
      </v-snackbar>
    </v-main>
  </v-app>
</template>
<script setup>
import { computed, getCurrentInstance, onMounted, ref, watch } from 'vue';
import { apiGetVideoBySimilar, apiStarVideo } from '../../apis/video';
import FavoriteCom from '../../components/favorite/index.vue';
import VideoCard from '../../components/video/card.vue';
const props = defineProps({
  videoInfo: {
    type: Object,
    default: null
  },
  closeVideo: {
    type: Function,
    default: () => { }
  }
})
const snackbar = ref({
  show: false,
  text: ""
})

const drawer = ref(true)
const instance = getCurrentInstance().proxy
const video = ref()
const windowHeight = ref(document.body.clientHeight)
const windowWidth = ref(document.body.clientWidth)
const videoPlayer = ref()
const similarList = ref([
  props.videoInfo
])
const currentIndex = ref(0)
const currentVideo = computed(() => {
  return currentIndex.value >= 0 ? similarList.value[currentIndex.value] : props.videoInfo
})
const favoriteCallBack = (e) => {
  if(e=="已收藏") {
    currentVideo.value.favoritesCount++
  }else{
    currentVideo.value.favoritesCount--
  }
  snackbar.value = {
    show: true,
    text: e
  }
}
onMounted(() => {
  window.onresize = () => { }
  window.onresize = () => {
    windowHeight.value = document.body.clientHeight
    windowWidth.value = document.body.clientWidth
  }
  videoPlayer.value = instance.$video(video.value, {
    notSupportedMessage: "暂不支持该视频类型",
    fill: true, userActions: {
      hotkeys: (event) => {
        if (event.which == 38) {
          if (currentIndex.value < 1) {
            return;
          }
          currentIndex.value--
        } else if (event.which == 40) {
          if (currentIndex.value >= similarList.value.length - 1) {
            return;
          }
          currentIndex.value++;
        }
      }
    }
  })
  videoPlayer.value.play()
  apiGetVideoBySimilar(props.videoInfo.labelNames).then(({ data }) => {
    similarList.value = similarList.value.concat(data.data)
  })
})
const starVideo = () => {

  apiStarVideo(currentVideo.value.id).then(({ data }) => {
    if (!data.state) {
      return;
    }
    if (data.message == "已点赞") {
      currentVideo.value.startCount++
    } else {
      currentVideo.value.startCount--
    }
    snackbar.value = {
      show: true,
      text: data.message
    }

  })
}
const playVideo = (n) => {
  if (n) {
    videoPlayer.value.pause()
    videoPlayer.value.reset()
    setTimeout(() => {
      videoPlayer.value.src([
        {
          src: n.url,
          type: n.videoType
        }
      ])
      videoPlayer.value.load()
      videoPlayer.value.play()
    },)
  }
}
watch(currentVideo, playVideo, {
  deep: true
})
const videoHeight = computed(() => {
  return windowHeight.value
})
const videoWidth = computed(() => {
  return windowWidth.value - (drawer.value ? 350 : 0)
})
</script>   