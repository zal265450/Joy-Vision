<template>
  <v-card>
    <v-card-title inset class="float-left">粉丝</v-card-title>
    <div class="float-none"></div>
    <v-tabs v-model="currentType" align-tabs="end" @update:model-value="getLike">
      <v-tab value="follows">关注</v-tab>
      <v-tab value="fans">粉丝</v-tab>
    </v-tabs>
    <v-divider />
    <v-list lines="two">
      <template v-for="item in currentItems">
        <v-list-item :title="item.nickName" :subtitle="item.description || '这个人很懒，没有任何描述'">
          <template v-slot:prepend>
            <v-avatar :color="item.sex ? 'blue' : 'pink'">
              <v-icon color="white">{{ item.sex ? 'mdi-human-male' : 'mdi-human-female' }}</v-icon>
            </v-avatar>
          </template>

          <template v-slot:append>
            <v-btn color="grey-lighten-1" variant="text">{{ currentType=='fans'?'互相关注':'取消关注' }}</v-btn>
          </template>
        </v-list-item>
        <v-divider />
      </template>

    </v-list>
    <VCard v-if="currentItems.length == 0" height="300px" class="ma-4" :variant="'tonal'"
      style="text-align: center;line-height: 300px;">
      好像没有什么内容呢
    </VCard>
  </v-card>
</template>
<script setup>
import { ref } from 'vue';
import { apiGetLike } from '../../../apis/user/like';
const currentType = ref("fans")
const currentItems = ref([])
/**
 * 获取关注/粉丝
 */
const getLike = () => {
  currentItems.value = []
  apiGetLike(currentType.value).then(({ data }) => {
    if (!data.state) {
      return;
    }
    currentItems.value = data.data
  })
}

</script>