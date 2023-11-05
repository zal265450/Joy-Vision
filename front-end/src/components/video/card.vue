<template>
    <v-card v-if="props.videoInfo" hover ripple :elevation="0" style="border-color: rgba(37,38,50);border-" rounded="lg">
        
        <v-img :src="props.videoInfo.cover || '/not-found.png'" class="align-end"
            gradient="to bottom, rgba(0,0,0,.1), rgba(0,0,0,.5)" height="300px" cover>
            <!-- <v-avatar :image="props.videoInfo.user.head||'/logo.png'" style="position:absolute;top:10px;right:10px"></v-avatar> -->
            <v-card-text class="text-white pa-0" v-if="!overlay">
                <v-card-actions class="ml-1 mr-1 pa-0">
                    <v-chip class="ma-2" color="red" text-color="white" prepend-icon="mdi-heart">
                        {{ props.videoInfo.startCount }} 点赞
                    </v-chip>
                    <v-spacer />
                    <v-btn :variant="'tonal'" :density="'comfortable'">{{ props.videoInfo.duration }}</v-btn>
                </v-card-actions>
            </v-card-text>
        </v-img>

        <v-card-actions>
            <!-- <v-card-title>{{ video.title }}</v-card-title> -->
            <span style="max-height: 20px;color: white;" class="ml-1 overflow-hidden">{{ props.videoInfo.title }}</span>
            <!-- <v-card-subtitle>{{ props.videoInfo.userName || "无" }}</v-card-subtitle> -->
            <v-spacer></v-spacer>
            <v-btn size="small" color="white" variant="tonal" v-if="props.videoInfo.user">@{{ props.videoInfo.user.nickName
            }}</v-btn>
        </v-card-actions>
        <v-overlay scrim="black" :model-value="overlay" contained persistent width="100%">
            <v-card color="rgba(1,1,1,0.5)" height="350px">
                <v-card-title class="pb-0">播放中 </v-card-title>
                <v-chip-group class="ml-2 mr-2">
                    <v-chip v-for="item in props.videoInfo.labelNames.split(',')">{{ item }}</v-chip>
                </v-chip-group>
                <v-card-subtitle>
                    <v-row>
                        <v-col>
                            {{ props.videoInfo.historyCount }} 播放
                        </v-col>
                        <v-col>
                            {{ props.videoInfo.startCount }} 点赞
                        </v-col>
                        <v-col>
                            {{ props.videoInfo.favoritesCount||0 }} 收藏
                        </v-col>
                    </v-row>
                </v-card-subtitle>

                <v-card-actions class="pb-0 pt-0">
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
                            {{ props.videoInfo.description || "作者很懒，没有给一点描述" }}
                        </v-card-text>
                    </div>
                </v-expand-transition>
            </v-card>

        </v-overlay>
    </v-card>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
const showDescription = ref(false)
const props = defineProps({
    videoInfo: {
        type: Object,
        default: null
    },
    overlay: {
        type: Boolean,
        default: false
    }
})

onMounted(() => {
    showDescription.value = props.overlay
})

watch(() => props.overlay, (e) => {
    showDescription.value = e
})
</script>
