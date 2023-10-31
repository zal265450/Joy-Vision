<template>
  <v-app full-height v-if="videoInfo">
    <v-navigation-drawer app v-model="drawer" location="right" :width="350">
      <v-list :items="sayItems" item-props lines="three">
        <template v-slot:subtitle="{ subtitle }">
          <div v-html="subtitle"></div>
        </template>
      </v-list>

      <template v-slot:append>
        <div class="pa-2 mb-6">
          <v-text-field placeholder="文明交流，创建美好网络" clearable :variant="'filled'" />
          <v-btn block>发布</v-btn>
        </div>
      </template>
    </v-navigation-drawer>
    <v-main>
      <v-card :height="videoHeight" :width="videoWidth" rounded="0">
        <video ref="video" class="video-js vjs-default-skin" controls>
          <source :src="props.videoInfo.url" />
        </video>
        <v-card class="pa-1" elevation="0" style="display: flex; flex-direction: column;
    gap: 10px;position: absolute; background-color: transparent; right: 20px; bottom: 65px;z-index: 99999;">
          <v-avatar class="elevation-2" image="/logo.png"></v-avatar>
          <v-btn size="40" color="bg" icon @click="drawer = !drawer">
            <v-icon :size="20">mdi-message</v-icon>
          </v-btn>
          <v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-heart</v-icon>
          </v-btn><v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-star</v-icon>
          </v-btn>
          <v-btn size="40" color="bg" icon>
            <v-icon :size="20">mdi-near-me</v-icon>
          </v-btn>
        </v-card>
      </v-card>
    </v-main>
  </v-app>
</template>
<script setup>
import { computed, getCurrentInstance, onMounted, ref } from 'vue';

const props = defineProps({
  videoInfo: {
    type: Object,
    default: null
  }
})
const sayItems = ref([
  { type: 'subheader', title: '评论区' },
  {
    prependAvatar: 'https://cdn.vuetifyjs.com/images/lists/1.jpg',
    title: 'Brunch this weekend?',
    subtitle: `<span class="text-primary">Ali Connors</span> &mdash; I'll be in your neighborhood doing errands this weekend. Do you want to hang out?`,
  },
  { type: 'divider', inset: true },
  {
    prependAvatar: 'https://cdn.vuetifyjs.com/images/lists/2.jpg',
    title: 'Summer BBQ',
    subtitle: `<span class="text-primary">to Alex, Scott, Jennifer</span> &mdash; Wish I could come, but I'm out of town this weekend.`,
  },
  { type: 'divider', inset: true },
  {
    prependAvatar: 'https://cdn.vuetifyjs.com/images/lists/3.jpg',
    title: 'Oui oui',
    subtitle: '<span class="text-primary">Sandra Adams</span> &mdash; Do you have Paris recommendations? Have you ever been?',
  },
  { type: 'divider', inset: true },
  {
    prependAvatar: 'https://cdn.vuetifyjs.com/images/lists/4.jpg',
    title: 'Birthday gift',
    subtitle: '<span class="text-primary">Trevor Hansen</span> &mdash; Have any ideas about what we should get Heidi for her birthday?',
  },
  { type: 'divider', inset: true },
  {
    prependAvatar: 'https://cdn.vuetifyjs.com/images/lists/5.jpg',
    title: 'Recipe to try',
    subtitle: '<span class="text-primary">Britta Holt</span> &mdash; We should eat this: Grate, Squash, Corn, and tomatillo Tacos.',
  },
])
const drawer = ref(false)
const instance = getCurrentInstance().proxy
const video = ref()
const windowHeight = ref(document.body.clientHeight)
const windowWidth = ref(document.body.clientWidth)
onMounted(() => {
  window.onresize = () => { }
  window.onresize = () => {
    windowHeight.value = document.body.clientHeight
    windowWidth.value = document.body.clientWidth
  }
  instance.$video(video.value, {
    fill: true, userActions: {
      hotkeys: (event) => {

      }
    }
  })
})
const videoHeight = computed(() => {
  return windowHeight.value
})
const videoWidth = computed(() => {
  return windowWidth.value - (drawer.value ? 350 : 0)
})
</script>   