<template>
    <v-card color="background" elevation="0" class="mb-2">
        <v-row dense class="mb-2">
            <v-divider class="ma-2" />
            <v-col v-for="(video, index) in videoList" :key="index" xs="12" sm="6" md="4" lg="3" xl="2">
                <VideoCard @click="playVideo(video)" :video-info="video" />
            </v-col>
            <v-col cols="12" v-if="!videoList || videoList.length == 0">
                <VCard height="300px" class="ma-4" :variant="'tonal'" style="text-align: center;line-height: 300px;">
                    {{ noDataMsg }}
                </VCard>

            </v-col>
        </v-row>
        <v-dialog v-model="videoDialog" fullscreen transition="dialog-bottom-transition">
            <v-card v-if="currentVideo">
                <Video :video-info="currentVideo" :close-video="() => playVideo(null)" />
            </v-card>
        </v-dialog>
    </v-card>
</template>
<script setup>
import { ref } from 'vue';
import VideoCard from './card.vue';
import Video from './index.vue';
const { videoList, noDataMsg } = defineProps({
    videoList: {
        type: Object,
        default: []
    },
    noDataMsg: {
        type: String,
        default: "未找到相关视频"
    }
})
const currentVideo = ref(null)
const videoDialog = ref(false)
const playVideo = (video) => {
    videoDialog.value = false
    currentVideo.value = video
    videoDialog.value = video ? true : false
}
</script>