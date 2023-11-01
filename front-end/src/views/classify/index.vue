<template>
    <VContainer>
        <!-- 用户订阅的分类，没有登录则不显示 -->
        <MyClassify :classifyData="myClassifyList" :close-event="subscribe" />
        <!-- 系统分类 -->
        <AllClassify class="mt-8" :classifyData="allClassifyList" :used="myClassifyList" :close-event="subscribe" />
    </VContainer>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { apiClassifyGetAll, apiClassifySubscribe, apiGetClassifyByUser } from '../../apis/classify';
import AllClassify from './all.vue';
import MyClassify from './my.vue';
const allClassifyList = ref([])
const myClassifyList = ref([])
const refreshData = ()=>{
    apiGetClassifyByUser().then(({data})=>{
        if (!data.state) {
            myClassifyList.value = []
            return;
        }
        myClassifyList.value = data.data
    })
    apiClassifyGetAll().then(({ data }) => {
        if (!data.state) {
            allClassifyList.value = []
            return;
        }
        allClassifyList.value = data.data
    })
}

const subscribe = (id)=>{
    console.log(id)
    apiClassifySubscribe(id).then(({data})=>{
        if(data.state) {
            refreshData()
        }
    })
}
onMounted(() => {
    refreshData()
})
</script>