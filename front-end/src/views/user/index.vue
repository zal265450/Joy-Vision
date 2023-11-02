<template>
    <v-container :fluid="true"  style="height: 500px;">
        <v-card>
            <v-img :height="220" aspect-ratio="16/9" cover
                src="https://cdn.vuetifyjs.com/images/parallax/material.jpg"></v-img>
            <v-tabs v-model="tab" color="#7bbfea">
                <v-tab value="one" to="/user/video">创作中心</v-tab>
                <v-tab value="two" to="/user/favorites">收藏夹</v-tab>
                <v-tab value="3" to="/user/history">历史记录</v-tab>
                <v-tab value="4" to="/user/like">关注/粉丝</v-tab>
                <v-spacer></v-spacer>
                <v-btn class="ma-1" variant="text">wdawd</v-btn>
            </v-tabs>
        </v-card>
        <router-view class="mt-2" />

    </v-container>
</template>

<script setup>
import { ref } from 'vue';
const tab = ref()
</script>