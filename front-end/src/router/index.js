import {
    createRouter,
    createWebHashHistory
} from "vue-router"
import Home from '../views/home/index.vue'

const routes = [
    {
        path:'/',
        component:Home
    },
    {
        path:'/classify',
        component:()=> import('../views/classify/index.vue')
    },
    {
        path:'/video',
        component:()=> import('../views/video/index.vue')
    },
    {
        path:'/my',
        component:()=> import('../views/myVideo/index.vue')
    }
]

const router = createRouter({
    history:createWebHashHistory(),
    routes:routes
})


export default router