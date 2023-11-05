<template>
    <v-card elevation="0">
        <v-list lines="three" class="mr-2 ml-2">
            <template v-for="(item) in videoList">
                <v-list-item class="pa-0" max-height="110px" style="overflow: hidden;">
                    <template v-slot:prepend>
                        <v-img width="200" height="100" class="mr-4" :src="item.cover" cover>

                        </v-img>
                    </template>

                    <v-list-item-title class="font-weight-bold" v-text="item.title"></v-list-item-title>

                    <div v-text="item.description" style="line-height: 25px;overflow: hidden;"></div>
                    <v-chip-group>
                        <v-chip v-for="item in item.labelNames.split(',')">
                            {{item}}
                        </v-chip>
                    </v-chip-group>
                    <template #append>
                        <v-btn-group :variant="'outlined'">
                            <v-btn color="blue">编辑</v-btn>
                            <v-btn color="red">删除</v-btn>
                        </v-btn-group>
                    </template>
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

const removeVideo =(id)=>{
    apiRemoveVideo
}
</script>