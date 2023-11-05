<template>
    <Video :video-list="videoInfo"/>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { apiGetFollowVideo } from '../../apis/video.js';
import Video from '../../components/video/index.vue';
const videoInfo = ref(null)
const videoList = ref([])
onMounted(()=>{
    apiGetFollowVideo().then(({data})=>{
        if(!data.state) {
            return;
        }
        videoList.value = data.data
    })
})
</script>