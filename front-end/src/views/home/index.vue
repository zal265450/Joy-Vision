<template>
  <v-container style="height: 500px;">
    <v-btn-toggle :disabled="isLoading" v-model="currentClassify" borderless color="#5865f2" class="mb-1"
      style="border-width: 1px;">
      <v-btn value="1">
        <span class="hidden-sm-and-down">热门视频</span>
        <v-icon end>
          mdi-fire
        </v-icon>
      </v-btn>

      <v-btn value="2">
        <span class="hidden-sm-and-down">体育频道</span>

        <v-icon end>
          mdi-walk
        </v-icon>
      </v-btn>
      <v-btn to="/classify">
        <v-icon>
          mdi-plus
        </v-icon>
      </v-btn>
    </v-btn-toggle>

    <v-row dense>
      <v-divider class="ma-2" />
      <v-col v-for="(video, index) in videoList" :key="index" :cols="3">
        <v-card hover ripple :elevation="0" style="border-color: rgba(37,38,50);" rounded="lg"
          @click="playVideo(video)">
          <v-img :src="video.cover || '/not-found.png'" class="align-end"
            gradient="to bottom, rgba(0,0,0,.1), rgba(0,0,0,.5)" height="300px" cover>
            <v-card-text class="text-white"><v-icon>mdi-heart</v-icon> 200w</v-card-text>
          </v-img>

          <v-card-actions>
            <!-- <v-card-title>{{ video.title }}</v-card-title> -->
            <span style="max-height: 20px;color: white;" class="ml-1 overflow-hidden">{{ video.title }}</span>
            <v-card-subtitle>{{ video.userName || "无" }}</v-card-subtitle>
            <v-spacer></v-spacer>
            <v-btn size="small" color="red" variant="tonal" icon="mdi-heart"></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    <v-dialog v-model="videoDialog" height="100%" fullscreen transition="dialog-bottom-transition">
      <v-card v-if="currentVideo">
        <Video :video-info="currentVideo" :close-video="()=>playVideo(null)" />
      </v-card>
    </v-dialog>
  </v-container>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue';
import { apiVideoByClassfiy } from '../../apis/video';
import Video from '../../components/video/index.vue';
const videoDialog = ref(false)
const isLoading = ref(false)
const videoList = ref([])
const currentClassify = ref(1)
const currentVideo = ref(null)
// 获取分类视频
const getCurrentClassifyVideo = (newV) => {
  isLoading.value = true
  videoList.value = []
  apiVideoByClassfiy(newV).then(({ data }) => {
    videoList.value = data.data
    isLoading.value = false
  })
}
watch(currentClassify, getCurrentClassifyVideo, {
  immediate: true
})
const playVideo = (video) => {
  videoDialog.value = false
  currentVideo.value = video
  videoDialog.value = video ? true : false
  console.log(videoDialog.value, video)
}
onMounted(() => {
  getCurrentClassifyVideo(currentClassify.value)
})
</script>
<style scoped></style>