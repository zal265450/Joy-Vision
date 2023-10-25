import {
    createRouter,
    createWebHashHistory
} from "vue-router"
import Home from '../views/home/index.vue'


const routes = [
    {
        path:'/',
        component:Home
    }
]

const router = createRouter({
    history:createWebHashHistory(),
    routes:routes
})


export default router