<template>
    <v-card v-if="props.videoInfo" hover ripple :elevation="0" style="border-color: rgba(37,38,50);" rounded="lg">
        <v-img :src="props.videoInfo.cover || '/not-found.png'" class="align-end" gradient="to bottom, rgba(0,0,0,.1), rgba(0,0,0,.5)"
            height="300px" cover>
            <v-card-text class="text-white"><v-icon>mdi-heart</v-icon> 200w</v-card-text>
        </v-img>

        <v-card-actions>
            <!-- <v-card-title>{{ video.title }}</v-card-title> -->
            <span style="max-height: 20px;color: white;" class="ml-1 overflow-hidden">{{ props.videoInfo.title }}</span>
            <!-- <v-card-subtitle>{{ props.videoInfo.userName || "无" }}</v-card-subtitle> -->
            <v-spacer></v-spacer>
            <v-btn size="small" color="white" variant="tonal" v-if="props.videoInfo.user">@{{ props.videoInfo.user.nickName }}</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script setup>
const props = defineProps({
    videoInfo: {
        type: Object,
        default: null
    }
})
</script>