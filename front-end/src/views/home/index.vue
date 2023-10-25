<template>
  <v-container style="height: 500px;">
    <v-row dense>
      <v-col v-for="card in cards" :key="card.title" :cols="card.flex">
        <v-card :elevation="0" :variant="'outlined'" style="border-color: rgba(69, 69, 69, 0.237);border-radius: 5%;">
          <v-img :src="card.src" class="align-end" gradient="to bottom, rgba(0,0,0,.1), rgba(0,0,0,.5)" height="300px"
            cover>
            <v-card-title class="text-white"><v-icon >mdi-heart</v-icon> 200w</v-card-title>
          </v-img>

          <v-card-actions>
            <v-card-title >{{ card.title }}</v-card-title>
            <v-spacer></v-spacer>

            <v-btn size="small" color="surface-variant" variant="text" icon="mdi-heart"></v-btn>

            <v-btn size="small" color="surface-variant" variant="text" icon="mdi-bookmark"></v-btn>

            <v-btn size="small" color="surface-variant" variant="text" icon="mdi-share-variant"></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
export default {
  data: () => ({
    cards: [
      { title: 'Favorite road trips', src: 'https://cdn.vuetifyjs.com/images/cards/road.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
      { title: 'Best airlines', src: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg', flex: 3 },
    ],
  }),
  methods: {
  },
}
</script>