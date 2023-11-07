<template>
    <div>
        <VCardTitle style="display: inline;">全部分类</VCardTitle>
        <VCardSubtitle style="display: inline;">所有视频分类</VCardSubtitle>
        <VDivider />
        <v-chip-group class="ma-2">
            <v-chip v-for="(item, index) in classifyDataList" :key="index" :size="'large'" closable
                :close-icon="item.used ? 'mdi-close-circle' : 'mdi-plus-circle'"
                @click:close="closeEvent(item.id, !item.used)">
                <template #prepend>
                    <VAvatar :image="item.image" :icon="item.icon || 'mdi-file-document-alert-outline'" start></VAvatar>
                </template>
                {{ item.name }}
            </v-chip>
        </v-chip-group>
    </div>
</template>
<script setup>
import { computed } from 'vue';

const props = defineProps({
    classifyData: {
        type: Array,
        default: []
    },
    used: {
        type: Array,
        default: [

        ]
    },
    closeEvent: {
        type: Function,
        default: () => { }
    }
})
const classifyDataList = computed(() => {
    return props.used.reduce((acc, cur) => {
        const target = acc.find(e => e.id === cur.id);
        if (target) {
            Object.assign(target, cur);
            target.used = true
        } else {
            acc.push(cur);
        }
        return acc;
    }, props.classifyData)
})
</script>