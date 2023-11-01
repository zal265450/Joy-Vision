<template>
  <v-container style="height: 500px;">
    <v-btn-toggle :disabled="isLoading" v-model="currentClassify" borderless color="#5865f2" class="mb-1"
      style="border-width: 1px;">
      
      <v-btn value="0">
        <span class="hidden-sm-and-down">热门视频</span>
        <v-icon end>
          mdi-fire
        </v-icon>
      </v-btn>
      <v-btn :value="item.id" v-for="(item) in userClassifys">
        <span class="hidden-sm-and-down">{{item.name}}</span>
        <v-icon end :icon="item.icon||'mdi-file-document-alert-outline'">
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
        <VideoCard :video-info="video" @click="playVideo(video)"/>
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
import { apiGetClassifyByUser } from '../../apis/classify';
import { apiVideoByClassfiy } from '../../apis/video';
import VideoCard from '../../components/video/card.vue';
import Video from '../../components/video/index.vue';
const userClassifys = ref([])
const videoDialog = ref(false)
const isLoading = ref(false)
const videoList = ref([])
const currentClassify = ref(0)
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
  apiGetClassifyByUser().then(({data})=>{
    userClassifys.value = data.data
  })
  getCurrentClassifyVideo(currentClassify.value)
})
</script>
<style scoped></style>