<template>
    <div>
        <VCardTitle style="display: inline;">订阅分类</VCardTitle>
        <VCardSubtitle style="display: inline;">您所喜欢的分类</VCardSubtitle>
        <VDivider />
        <v-row dense class="ma-2">
            <v-col :cols="3" v-for="(item, index) in classifyData" :key="index">
                <v-chip :size="'large'" closable>
                    <template #prepend>
                        <VAvatar :image="item.image" :icon="item.icon || 'mdi-file-document-alert-outline'" start></VAvatar>
                    </template>
                    {{ item.name }}
                </v-chip>
            </v-col>
        </v-row>
    </div>
</template>
<script setup>
const props = defineProps({
    classifyData: {
        type: Array,
        default: []
    },
})
</script>