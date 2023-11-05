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
        <v-card class="pa-2" elevation="0" style="display: flex; flex-direction: column;
    gap: 12px;position: absolute; background-color: transparent; right: 25px; bottom: 25px;z-index: 99999;">
          <v-badge color="red" icon="mdi-plus" location="bottom" @click="()=>{}">
            <v-avatar class="elevation-2" image="/logo.png"></v-avatar>
          </v-badge>
          <v-btn size="40" color="blue" icon @click="openRgihtD()">
            <v-icon :size="20">mdi-more</v-icon>
          </v-btn>
          <v-badge color="red" :content="currentVideo.startCount" location="bottom">
            <v-btn size="40" :color="'pink'" icon @click="starVideo()">
              <v-icon :size="20">mdi-heart</v-icon>
            </v-btn>
          </v-badge>

          <FavoriteCom :video-id="currentVideo.id" :callback="favoriteCallBack">
            <template #default="{ props }">
              <v-badge color="red" :content="currentVideo.favoritesCount" location="bottom">
                <v-btn v-bind="props" size="40" color="warning" icon>
                  <v-icon :size="20">mdi-star</v-icon>
                </v-btn>
              </v-badge>
            </template>
          </FavoriteCom>
          <v-btn size="40" color="success" icon @click="copyUrl()">
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
import { computed, getCurrentInstance, onMounted, onUnmounted, ref, watch } from 'vue';
import { apiAddHistory, apiGetVideoBySimilar, apiSetUserVideoModel, apiStarVideo } from '../../apis/video';
import FavoriteCom from '../../components/favorite/index.vue';
import VideoCard from '../../components/video/card.vue';
import strUtils from '../../utils/strUtil';
const props = defineProps({
  videoInfo: {
    type: Object,
    default: null
  },
  videoList: {
    type: Array,
    default: []
  },
  nextVideo: {
    type: Function,
    default: () => {

    }
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
if (props.videoList && props.videoList.length > 0) {
  similarList.value = props.videoList
}
const currentIndex = ref(0)
const currentVideo = computed(() => {
  return currentIndex.value >= 0 ? similarList.value[currentIndex.value] : props.videoInfo
})
const openRgihtD = () => {
  drawer.value = !drawer.value
  video.value.focus()
}
const favoriteCallBack = (e) => {
  if (e == "已收藏") {
    currentVideo.value.favoritesCount++
  } else {
    currentVideo.value.favoritesCount--
  }
  snackbar.value = {
    show: true,
    text: e
  }
}
const isAddHistory = ref(true)
const isLikeVideo = ref(false)
const windowKeyEvent = (event) => {
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
const copyUrl = () => {

  snackbar.value = {
    text: strUtils.copyContent(location.host + "/#/?play=" + currentVideo.value.id) ? "视频地址复制成功" : "视频地址复制失败",
    show: true
  }
}
onUnmounted(() => {
  window.removeEventListener("keydown", windowKeyEvent)
})
const firstInitVideo = () => {
  if (videoPlayer.value || !currentVideo.value) return;
  videoPlayer.value = instance.$video(video.value, {
    notSupportedMessage: "暂不支持该视频类型",
    fill: true,
    autoplay: true
  })
  window.addEventListener("keydown", windowKeyEvent)
  videoPlayer.value.on("timeupdate", function () {
    // 播放三秒后添加历史记录
    if (this.currentTime() >= 3 && isAddHistory.value) {
      isAddHistory.value = false
      apiAddHistory(currentVideo.value.id)
    }
    let duration = this.duration()
    let score = this.currentTime() >= (duration / 5)
    if (score) {
      if (!isLikeVideo.value)
        apiSetUserVideoModel(currentVideo.value.id, currentVideo.value.labelNames, 1)
      isLikeVideo.value = true;
    } else isLikeVideo.value = false

  })
  videoPlayer.value.play()
  apiGetVideoBySimilar(props.videoInfo.labelNames, props.videoInfo.id).then(({ data }) => {
    similarList.value = similarList.value.concat(data.data)
  })
}
onMounted(() => {
  window.onresize = () => { }
  window.onresize = () => {
    windowHeight.value = document.body.clientHeight
    windowWidth.value = document.body.clientWidth
  }
  firstInitVideo()
})
const starVideo = () => {

  apiStarVideo(currentVideo.value.id).then(({ data }) => {
    snackbar.value = {
      show: true,
      text: data.message
    }
    if (!data.state) {
      return;
    }
    if (data.message == "已点赞") {
      currentVideo.value.startCount++
    } else {
      currentVideo.value.startCount--
    }


  })
}
const playVideo = (n) => {
  if (n) {
    firstInitVideo()
    isAddHistory.value = true
    // videoPlayer.value.reset()
    setTimeout(() => {
      videoPlayer.value.src([
        {
          src: n.url,
          type: n.videoType,
          poster: n.cover
        }
      ])
      videoPlayer.value.load()
      videoPlayer.value.play()
      apiSetUserVideoModel(n.id, n.labelNames, -0.5)
    }, 10)
  }
}
watch(currentVideo, playVideo)
const videoHeight = computed(() => {
  return windowHeight.value
})
const videoWidth = computed(() => {
  return windowWidth.value - (drawer.value ? 350 : 0)
})
</script>   