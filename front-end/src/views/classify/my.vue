<template>
    <div>
        <VCardTitle style="display: inline;">订阅分类</VCardTitle>
        <VCardSubtitle style="display: inline;">您所喜欢的分类</VCardSubtitle>
        <VDivider />
        <v-row dense class="ma-2">
            <v-col :cols="3">
                <v-chip :size="'large'" closable prepend-icon="mdi-walk" >
                    体育频道
                </v-chip>
            </v-col>
        </v-row>
    </div>
</template>
<script setup>
</script>