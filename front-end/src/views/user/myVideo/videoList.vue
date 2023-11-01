<template>
    <v-card elevation="0">
        <v-list lines="three">
            <template v-for="(item) in videoList">
                <v-list-item class="pa-0">
                    <template v-slot:prepend>
                        <v-img width="200" height="100" class="mr-4" :src="item.cover" cover></v-img>
                    </template>

                    <v-list-item-title class="font-weight-bold" v-text="item.title"></v-list-item-title>

                    <v-row style="height: 100px;">
                        <v-col cols="4">
                            <div v-text="item.description"></div>
                        </v-col>
                        <v-col cols="4">
                            <v-chip-group>
                                <v-chip>
                                Chip
                            </v-chip>
                            <v-chip>
                                Chip
                            </v-chip>
                            <v-chip>
                                Chip
                            </v-chip>
                            </v-chip-group>
                        </v-col>
                        <v-col cols="4" class="justify-end">
                            <v-btn-group :variant="'outlined'" style="background-color: #36393f;">
                                <v-btn>编辑</v-btn>
                                <v-btn>删除</v-btn>
                                <v-btn>下架</v-btn>
                            </v-btn-group>
                        </v-col>

                    </v-row>
                </v-list-item>
                <v-divider class="ma-2" />
                <div class="mt-2 mb-2"></div>
            </template>

        </v-list>
    </v-card>
</template>
<script setup>
import { ref } from 'vue';
import { apiGetVideoByUser } from '../../../apis/video';
const videoList = ref([])
const getVideo = () => {
    apiGetVideoByUser().then(({ data }) => {
        if (!data.state) {
            return;
        }
        videoList.value = data.data.records
    })
}

getVideo()
</script>