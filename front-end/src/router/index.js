import { createRouter, createWebHashHistory } from "vue-router";
import Home from "../views/home/index.vue";

const routes = [
  {
    path: "/",
    component: Home,
  },
  {
    path: "/classify",
    component: () => import("../views/classify/index.vue"),
  },
  {
    path: "/video",
    component: () => import("../views/video/index.vue"),
  },
  {
    path: "/user",
    component: () => import("../views/user/index.vue"),
    redirect: "/user/video",
    children: [
      {
        path: "video",
        component: () => import("../views/user/myVideo/index.vue"),
      },
      {
        path: "favorites",
        component: () => import("../views/user/favorites/index.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes,
});

export default router;
