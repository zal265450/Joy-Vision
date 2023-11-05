<template>
    <v-container :fluid="true" style="height: 500px;">
        <v-card>
            <v-img :height="220" aspect-ratio="16/9" cover src="https://cdn.vuetifyjs.com/images/parallax/material.jpg">

                <v-list
                    style="position: absolute;left: 0;bottom: 0; width: 100%; background-color: rgba(1,1,1,0.5); color: white;">
                    <v-list-item :title="userStore.info.nickName" :subtitle="userStore.$state.info.description">
                        <template #prepend>
                            <v-avatar :image="userStore.info.avatar||'/logo.png'" size="50"/>
                        </template>
                    </v-list-item>
                </v-list>
            </v-img>
            <v-tabs v-model="tab" color="#7bbfea">
                <v-tab value="one" to="/user/video">创作中心</v-tab>
                <v-tab value="two" to="/user/favorites">收藏夹</v-tab>
                <v-tab value="3" to="/user/history">历史记录</v-tab>
                <v-tab value="4" to="/user/like">关注/粉丝</v-tab>
                <v-spacer></v-spacer>
                <v-btn class="ma-2" variant="text">编辑信息</v-btn>
            </v-tabs>
        </v-card>
        <router-view class="mt-2" />

    </v-container>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '../../stores';
const tab = ref()
const userStore = useUserStore()
const route = useRoute()
onMounted(()=>{
    userStore.$patch({
        lookId: route.query.userId||userStore.info.id
    })
})
</script>