import { createApp } from 'vue'
import 'vuetify/styles'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import router from './router'

// 创建应用
const luckJourneyApp = createApp(App)
luckJourneyApp.use(router)
luckJourneyApp.use(vuetify)
luckJourneyApp.mount('#app')

