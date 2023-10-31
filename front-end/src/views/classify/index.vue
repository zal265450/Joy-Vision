<template>
    <VContainer>
        <!-- 用户订阅的分类，没有登录则不显示 -->
        <MyClassify :classifyData="myClassifyList"/>
        <!-- 系统分类 -->
        <AllClassify class="mt-8" :classifyData="allClassifyList" :used="myClassifyList"/>
    </VContainer>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { apiClassifyGetAll } from '../../apis/classify';
import AllClassify from './all.vue';
import MyClassify from './my.vue';
const allClassifyList = ref([])
const myClassifyList = ref([{
            "id": 5,
            "isDeleted": false,
            "gmtCreated": null,
            "gmtUpdated": null,
            "name": "宠物",
            "description": null,
            "open": false
        }])
onMounted(()=>{
     apiClassifyGetAll().then(({data})=>{
        if(!data.state) {
            allClassifyList.value = []
            return;
        }
        allClassifyList.value = data.data
     })
})
</script>