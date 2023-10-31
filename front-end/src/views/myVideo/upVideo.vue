<template>
    <!-- 上传视频 -->
    <v-card flat>
        <VCard>
            <VRow>
                <VCol cols="6">
                    <VCard border="dashed" min-height="'300px'" class="ma-4" :variant="'outlined'"
                        style="text-align: center;">
                        <VList density style="margin: 0 auto;">
                            <VListItem>
                                <VIcon color="blue" :size="50">mdi-upload</VIcon>
                            </VListItem>
                            <VListItem>
                                <VCardSubtitle>拖到此处可上传</VCardSubtitle>
                            </VListItem>
                            <VListItem>
                                <VBtn @click="videoFileRef.click()">上传视频</VBtn>
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
                                <input v-on:change="uploadVideo" ref="videoFileRef" type="file"/>
                            </form>
                        </div>
                    </VCard>
                </VCol>
                <VCol cols="6">
                    <v-list density lines="three" class="mt-2">
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

        <VCard>
            <v-item-group mandatory>
                <VCardTitle>基本信息</VCardTitle>
                <VDivider></VDivider>
                <v-container>
                    <v-row>
                        <v-col v-for="n in 3" :key="n" cols="12" md="3">
                            <v-item v-slot="{ isSelected, toggle }">
                                <v-card :color="isSelected ? 'primary' : 'white'" dark height="100" @click="toggle">
                                    <v-scroll-y-transition>
                                        <div>
                                            <v-card-title>视频1</v-card-title>
                                            <VCardText class="pb-0">
                                                <v-progress-linear color="orange" striped v-model="knowledge" height="25">
                                                    <strong>{{ Math.ceil(30) }}%</strong>
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
            <v-card-text>
                <v-text-field variant="filled" label="视频标题" model-value="视频1"></v-text-field>

                <v-textarea variant="filled" label="视频描述"
                    model-value="2013年2月，主演的电影《西游·降魔篇》在全国上映。2014年3月28日，主演的爱情片《我在路上最爱你》上映。2014年，在姜文执导的动作喜剧片《一步之遥》中扮演武七一角。2016年，主演电视剧《少帅》 [5]和《剃刀边缘》 [6]。2017年，凭借导演的电影《陆垚知马俐》获得第31届中国电影金鸡奖导演处女作奖 [7]。2018年9月30日，主演的喜剧动作电影《胖子行动队》上映 [8]。2021年8月20日，主演的电影《测谎人》上映。 [47]"></v-textarea>

                <v-autocomplete v-model="friends" :disabled="isUpdating" :items="people" chips closable-chips
                    color="blue-grey-lighten-2" item-title="name" item-value="name" label="视频分类" no-data-text="无视频分类">
                    <template v-slot:chip="{ props, item }">
                        <v-chip v-bind="props" :prepend-avatar="item.raw.avatar" :text="item.raw.name"></v-chip>
                    </template>

                    <template v-slot:item="{ props, item }">
                        <v-list-item v-bind="props" :prepend-avatar="item?.raw?.avatar" :title="item?.raw?.name"
                            :subtitle="item?.raw?.group"></v-list-item>
                    </template>
                </v-autocomplete>
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="success" class="font-weight-bold" :variant="'tonal'">
                    发布视频
                </v-btn>
            </v-card-actions>
        </VCard>
    </v-card>
</template>
<script setup>
import { ref } from 'vue';
import { apiVideoUpload } from '../../apis/video';
const knowledge = ref(30)
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

const videoFileRef = ref()
const uploadVideo = ()=>{
    console.log(videoFileRef.value.value)
    apiVideoUpload(videoFileRef.value.value, (e)=>{
      console.log("进度:"+e)  
    },(e)=>{
        console.log("失败:"+e)
    },(e)=>{
        console.log("成功:"+e)
    })
}
</script>