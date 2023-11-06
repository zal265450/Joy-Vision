<template>
    <v-card style="height: 100%;" elevation="2" color="background">
        <Video :video-list="videoList" v-if="videoList.length > 0" />
        <v-card v-else class="mx-auto mt-15" elevation="5" max-width="500" >
            <div class="py-12 text-center">
                <v-icon class="mb-6" icon="mdi-eye" size="128"></v-icon>

                <div class="text-h4 font-weight-bold">您关注的人没有视频哦~</div>
            </div>

            <v-divider></v-divider>

            <div class="pa-4 text-end">
                <v-btn class="text-none" color="medium-emphasis" min-width="92" rounded variant="outlined"
                    to="/">
                    去看推荐
                </v-btn>
            </div>
        </v-card>
    </v-card>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { apiGetFollowVideo } from '../../apis/video.js';
import Video from '../../components/video/index.vue';
const videoList = ref([])
onMounted(() => {
    apiGetFollowVideo().then(({ data }) => {
        if (!data.state) {
            return;
        }
        videoList.value = data.data
    })
})
</script>