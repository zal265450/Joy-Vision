<template>
    <!-- 上传视频 -->
    <v-card>
        <VCard>
            <VRow>
                <VCol cols="6">
                    <VCard border="dashed" min-height="'300px'" class="ma-4" :variant="'outlined'"
                        style="text-align: center;">
                        <VList density="compact" style="margin: 0 auto;">
                            <VListItem>
                                <VIcon color="blue" :size="50">mdi-upload</VIcon>
                            </VListItem>
                            <VListItem>
                                <VCardSubtitle>拖到此处可上传</VCardSubtitle>
                            </VListItem>
                            <VListItem>
                                <VBtn color="primary" @click="videoFileRef.click()">上传视频</VBtn>
                            </VListItem>
                            <VListItem>
                                <v-chip class="ma-2 font-weight-bold" color="green" label>
                                    <v-icon start :icon="'mdi-file-cloud-outline'"></v-icon>
                                    审核队列: 快速
                                </v-chip>
                            </VListItem>
                        </VList>
                        <div style="display: none;">
                            <form>
                                <input v-on:change="uploadVideo" ref="videoFileRef" type="file" accept="video/*" />
                            </form>
                        </div>
                    </VCard>
                </VCol>
                <VCol cols="6">
                    <v-list :density="'compact'" lines="three" class="mt-2">
                        <VListItemTitle>
                            <VIcon>mdi-menu-down</VIcon>用户须知
                        </VListItemTitle>
                        <v-list-item v-for="(item, i) in items" :key="i" link>
                            <template v-slot:prepend>
                                <v-avatar class="me-4 mt-2" rounded="0">
                                    <v-img :src="item.image" cover></v-img>
                                </v-avatar>
                            </template>

                            <v-list-item-title class="text-uppercase font-weight-regular text-caption"
                                v-text="item.category"></v-list-item-title>
                            <div v-text="item.title"></div>
                        </v-list-item>
                    </v-list>
                </VCol>
            </VRow>
        </VCard>
        <VCard v-if="uploadList.length > 0">
            <v-item-group mandatory>
                <VCardTitle>基本信息</VCardTitle>
                <VDivider></VDivider>
                <v-container>
                    <v-row>
                        <v-col v-for="(uploadItem, index) in uploadList" :key="index" cols="12" md="3">
                            <v-item v-slot="{ isSelected, toggle }">
                                <v-card :color="isSelected ? 'primary' : 'white'" dark height="100"
                                    @click="() => { toggle(); currentVideoIndex= index }">
                                    <v-scroll-y-transition>
                                        <div>
                                            <v-card-title>{{ uploadItem.title }}</v-card-title>
                                            <VCardText class="pb-0">
                                                <v-progress-linear color="orange" striped :model-value="uploadItem.progress"
                                                    height="25">
                                                    <strong>{{ Math.ceil(uploadItem.progress) }}%</strong>
                                                </v-progress-linear>
                                            </VCardText>
                                        </div>
                                    </v-scroll-y-transition>
                                </v-card>
                            </v-item>
                        </v-col>
                    </v-row>
                </v-container>
            </v-item-group>
            <v-card-text v-if="currentVideo">
                <v-text-field variant="filled" label="视频标题" v-model="currentVideo.title" clearable></v-text-field>

                <v-textarea variant="filled" label="视频描述" :rows="3" v-model="currentVideo.description"
                    clearable></v-textarea>

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
                <v-divider></v-divider>

                <v-card-actions>
                    <v-btn color="warning" class="font-weight-bold" :variant="'tonal'" @click="clearUp()">取消上传</v-btn>
                    <v-spacer></v-spacer>
                    <v-btn color="success" class="font-weight-bold" :variant="'tonal'" @click="pushVideo()">
                        发布视频
                    </v-btn>
                </v-card-actions>
            </v-card-text>


        </VCard>
    </v-card>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue';
import { apiClassifyGetAll } from '../../../apis/classify';
import { apiVideoPush, apiVideoUpload } from '../../../apis/video';

const items = ref([
    {
        image: 'https://cdn.vuetifyjs.com/docs/images/chips/globe.png',
        title: '视频格式支持：mp4、avi、aav、cawd',
        category: '视频',

    },
    {
        image: 'https://cdn.vuetifyjs.com/docs/images/chips/cpu.png',
        title: '发布视频后会进行视频审核。(七牛云服务自动审核)',
        category: '审核',
    },
    {
        image: 'https://cdn.vuetifyjs.com/docs/images/chips/rocket.png',
        title: '您的视频将会被存储在七牛云的对象存储服务中。',
        category: '存储',
    }
])
const allClassifyList = ref([])
const currentVideoIndex = ref(null)
const videoFileRef = ref()
const uploadList = ref([])
const currentVideo = computed(()=> currentVideoIndex.value>-1?uploadList.value[currentVideoIndex.value]:null)
onMounted(() => {
    apiClassifyGetAll().then(({ data }) => {
        if (!data.state) {
            allClassifyList.value = []
            return;
        }
        allClassifyList.value = data.data
    })
})
const clearUp = () => {
    console.log(currentVideo.value)
    if (currentVideo.value.status <1) {
        currentVideo.value.subscription.unsubscribe()
    }
    uploadList.value.splice(currentVideoIndex.value, 1);
}
const pushVideo = ()=>{
    apiVideoPush(currentVideo.value).then(({data})=>{
        if(data.state) {
            clearUp();
        }
    })
}
const uploadVideo = () => {
    let curFile = videoFileRef.value.files[0]
    console.log(curFile)
    const curUploadInfo = {
        progress: 0,
        status: 0,
        msg: "",
        result: "",
        title: curFile.name,
        description: "",
        url: "",
        cover: "",
        file: curFile
    }
    uploadList.value.push(curUploadInfo)
    curUploadInfo.subscription = apiVideoUpload(curFile, {
        next: (e) => {
            curUploadInfo.progress = e.total.percent
            uploadList.value = Object.assign([], uploadList.value)
        }, error: (e) => {
            curUploadInfo.status = -1
            curUploadInfo.msg = "上传失败：" + e
            uploadList.value = Object.assign([], uploadList.value)
        },
        complete: (e) => {
            curUploadInfo.result = e
            curUploadInfo.status = 1
            curUploadInfo.url = e.key
            curUploadInfo.cover = `http://oss.luckjourney.liuscraft.top/${e.key}?vframe/jpg/offset/1`
            uploadList.value = Object.assign([], uploadList.value)
        }
    })
    videoFileRef.value.value = ""
}
</script>