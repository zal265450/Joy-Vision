<template>
  <v-container style="height: 500px;">
    <v-btn-toggle v-if="!route.meta.isClassify" :disabled="isLoading" v-model="currentClassify" mandatory borderless
      color="#5865f2" class="mb-1" style="border-width: 1px;">

      <v-btn value="0">
        <span class="hidden-sm-and-down">热门视频</span>
        <v-icon end>
          mdi-fire
        </v-icon>
      </v-btn>
      <v-btn :value="item.id" v-for="(item) in userClassifys">
        <span class="hidden-sm-and-down">{{ item.name }}</span>
        <v-icon end :icon="item.icon || 'mdi-file-document-alert-outline'">
        </v-icon>
      </v-btn>
      <v-btn to="/classify">
        <v-icon>
          mdi-plus
        </v-icon>
      </v-btn>
    </v-btn-toggle>
    <VideoListVue :video-list="videoList" />

  </v-container>
</template>
<script setup>
import { onMounted, onUpdated, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { apiGetClassifyByUser } from '../../apis/classify';
import { apiVideoByClassfiy } from '../../apis/video';
import VideoListVue from '../../components/video/list.vue';
const userClassifys = ref([])
const isLoading = ref(false)
const videoList = ref([])
const currentClassify = ref(0)
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
const route = useRoute()
const initView =
  () => {
    if (route.meta.isClassify) {
      currentClassify.value = route.path.split("/").pop()
      return;
    } else {
      currentClassify.value = 0
      apiGetClassifyByUser().then(({ data }) => {
        userClassifys.value = data.data
      })
    }

    getCurrentClassifyVideo(currentClassify.value)
  }

onUpdated(initView)
onMounted(initView)
</script>
<style scoped></style>