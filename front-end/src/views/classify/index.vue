<template>
    <VContainer>
        <!-- 用户订阅的分类，没有登录则不显示 -->
        <MyClassify/>
        <!-- 系统分类 -->
        <AllClassify class="mt-8"/>
    </VContainer>
</template>
<script setup>
import AllClassify from './all.vue';
import MyClassify from './my.vue';

</script>