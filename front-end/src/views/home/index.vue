<template>
  <v-container style="height: 500px;">
    <v-btn-toggle
          v-model="currentLabel"
          borderless
          color="#5865f2"
          class="mb-1"
        >
          <v-btn value="left">
            <span class="hidden-sm-and-down">热门视频</span>

            <v-icon end>
              mdi-fire
            </v-icon>
          </v-btn>

          <v-btn value="center">
            <span class="hidden-sm-and-down">体育频道</span>

            <v-icon end>
              mdi-walk
            </v-icon>
          </v-btn>
          <v-btn value="justify">
            <v-icon>
              mdi-plus
            </v-icon>
          </v-btn>
        </v-btn-toggle>
        
    <v-row dense >
      <v-divider class="ma-2"/>
      <v-col v-for="card in cards" :key="card.title" :cols="card.flex">
        <v-card ripple="3" :elevation="0" :variant="'outlined'" style="border-color: rgba(69, 69, 69, 0.237);" rounded="lg">
          <v-img :src="card.src" class="align-end" gradient="to bottom, rgba(0,0,0,.1), rgba(0,0,0,.5)" height="300px"
            cover>
            <v-card-text class="text-white"><v-icon >mdi-heart</v-icon> 200w</v-card-text>
          </v-img>

          <v-card-actions style="background-color: #36393f;">
            <span style="max-height: 20px;color: white;" class="ml-1 overflow-hidden">{{ card.title }}</span>
            <v-spacer></v-spacer>
            <v-btn size="small" color="red" variant="tonal"  icon="mdi-heart"></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
export default {
  data: () => ({
    currentLabel: 0,
    cards: [
      { title: 'Favorite road trips', src: 'https://cdn.vuetifyjs.com/images/cards/road.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
      { title: 'Best airlineesfsfsfsfawdwauidhiawudguwagduis', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
    ],
  }),
  methods: {
  },
}
</script>