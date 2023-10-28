<template>
    <div>
        <VCardTitle style="display: inline;">全部分类</VCardTitle>
        <VCardSubtitle style="display: inline;">所有视频分类</VCardSubtitle>
        <VDivider />
        <v-row dense class="ma-2">
            <v-col>
                <v-chip :size="'large'" closable >
                    <template #prepend>
                        <VAvatar :image="'/logo.png'" icon="mdi-walk" start></VAvatar>
                    </template>
                    体育频道
                </v-chip>
            </v-col>
            <v-col>
                <v-chip :size="'large'" closable close-icon="mdi-plus-circle">
                    <template #prepend>
                        <VAvatar :image="'/logo.png'" icon="mdi-walk" start></VAvatar>
                    </template>
                    二次元
                </v-chip>
            </v-col>
            <v-col>
                <v-chip :size="'large'" closable >
                    <template #prepend>
                        <VAvatar :image="'/logo.png'" icon="mdi-walk" start></VAvatar>
                    </template>
                    穿越火线
                </v-chip>
            </v-col>
        </v-row>
    </div>
</template>
<script setup>
</script>