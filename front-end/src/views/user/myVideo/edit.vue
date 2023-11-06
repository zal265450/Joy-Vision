<template>
    <div>
        <v-text-field variant="filled" label="视频标题" v-model="currentVideo.title" clearable></v-text-field>

        <v-textarea variant="filled" label="视频描述" :rows="3" v-model="currentVideo.description" clearable></v-textarea>

        <v-autocomplete v-model="currentVideo.typeId" :items="allClassifyList" chips closable-chips
            color="blue-grey-lighten-2" item-title="name" item-value="id" label="视频分类" no-data-text="无视频分类">
            <template v-slot:chip="{ props, item }">
                <v-chip v-bind="props" :prepend-icon="item.raw.icon || 'mdi-file-document-alert-outline'"
                    :text="item.raw.name"></v-chip>
            </template>

            <template v-slot:item="{ props, item }">
                <v-list-item v-bind="props" :prepend-icon="item?.raw?.icon || 'mdi-file-document-alert-outline'"
                    :title="item?.raw?.name" :subtitle="item?.raw?.description || '无相关描述信息'"></v-list-item>
            </template>
        </v-autocomplete>
        <v-combobox v-model="currentVideo.labelNames" label="视频标签" multiple chips closable-chips></v-combobox>
        <v-divider></v-divider>

        <v-card-actions>
            <v-btn color="warning" class="font-weight-bold" :variant="'tonal'" @click="clearUp()">取消</v-btn>
            <v-spacer></v-spacer>
            <v-btn color="success" class="font-weight-bold" :variant="'tonal'" @click="pushVideo()">
                发布视频
            </v-btn>
        </v-card-actions>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { apiClassifyGetAll } from '../../../apis/classify';
import { apiVideoPush } from '../../../apis/user/videoManger';

const allClassifyList = ref([])
const { currentVideo, save, clear } = defineProps({
    currentVideo: {
        type: Object,
        default: {}
    },
    clear: {
        type: Function,
        default: () => { }
    },
    save: {
        type: Function,
        default: () => { }
    }
})
const clearUp = () => {
    clear()
}
onMounted(() => {
    apiClassifyGetAll().then(({ data }) => {
        if (!data.state) {
            allClassifyList.value = []
            return;
        }
        allClassifyList.value = data.data
    })
})
const pushVideo = () => {
    apiVideoPush(currentVideo).then(({ data }) => {
        save(data)
    })
}
</script>