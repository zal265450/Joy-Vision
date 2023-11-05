<template>
  <v-container style="height: 500px;">
    <v-tabs v-model="currentClassify" center-active>
      <v-tab value="0">热门视频</v-tab>
      <v-tab :value="item.id" v-for="(item) in userClassifys" 
        :text="item.name"></v-tab>
        <v-tab to="/classify" v-if="userStore.token">
        <v-icon>
          mdi-plus
        </v-icon>
      </v-tab>
    </v-tabs>
    <!-- <v-btn-toggle v-if="false" :disabled="isLoading" v-model="currentClassify" mandatory borderless color="#5865f2"
      class="mb-1" style="border-width: 1px;">

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
      
    </v-btn-toggle> -->
    <VideoListVue :video-list="videoList" />
    <v-dialog :model-value="dialog" fullscreen transition="dialog-bottom-transition">
      <v-card v-if="dialog">
        <Video :video-info="searchVideoInfo" :close-video="() => searchVideoInfo = null" />
      </v-card>
    </v-dialog>
  </v-container>
</template>
<script setup>
import { computed, onMounted, onUpdated, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { apiGetClassifyByUser } from '../../apis/classify';
import { apiGetVideoById, apiSearchVideo, apiVideoByClassfiy } from '../../apis/video';
import Video from '../../components/video/index.vue';
import VideoListVue from '../../components/video/list.vue';
import { useUserStore } from '../../stores';
const userStore = useUserStore()
const userClassifys = ref([])
const isLoading = ref(true)
const videoList = ref([])
const currentClassify = ref(0)
const route = useRoute()
const searchVideoInfo = ref(null)
const dialog = computed(() => searchVideoInfo.value ? true : false)
// 获取分类视频
const getCurrentClassifyVideo = (newV) => {
  if (route.meta.isSearch) return;
  isLoading.value = true
  videoList.value = []
  apiVideoByClassfiy(newV).then(({ data }) => {
    videoList.value = data.data
    isLoading.value = false
  })
}
watch(currentClassify, getCurrentClassifyVideo)
const initView =
  () => {
    if (route.meta.isClassify) {
      currentClassify.value = route.params.classify
    } else if (route.meta.isSearch) {
      // 搜索
      apiSearchVideo(route.params.key).then(({ data }) => {
        isLoading.value = false
        if (!data.state) {
          return;
        }
        videoList.value = data.data.records
      })
      return;
    } else {
      currentClassify.value = 0
      apiGetClassifyByUser().then(({ data }) => {
        userClassifys.value = data.data
      })
      getCurrentClassifyVideo()
    }
    if (route.query.play) {
      apiGetVideoById(route.query.play).then(({ data }) => {
        if (!data.state) {
          return;
        }
        searchVideoInfo.value = data.data
      })
    }
  }

onUpdated(initView)
onMounted(initView)
</script>
<style scoped></style>