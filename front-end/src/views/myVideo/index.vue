<template>
    <VContainer class="mt-2">
        <v-card>
            <v-tabs v-model="tab" color="#7bbfea">
                        <v-tab v-for="item in items" :key="item">
                            {{ item }}
                        </v-tab>
                    </v-tabs>
            <v-window v-model="tab" class="ma-2">
                <!-- 发布稿件(视频) -->
                <v-window-item value="0">
                    <UploadVideo></UploadVideo>
                </v-window-item>
            </v-window>
        </v-card>
    </VContainer>
</template>
<script setup>
import { ref } from 'vue';
import UploadVideo from './upVideo.vue';
const tab = ref(0)
const items = ref([
    '上传稿件', '稿件管理', '异常稿件'
])
</script>