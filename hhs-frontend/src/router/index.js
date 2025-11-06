import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Home.vue")
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue")
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/Register.vue")
  },
  {
    path: "/publish",
    name: "TipPublish",
    component: () => import("@/views/TipPublish.vue")
  },
  {
    path: "/tips/:id",
    name: "TipDetail",
    component: () => import("@/views/TipDetail.vue"),
    props: true
  },
  {
    path: "/ai-advisor",
    name: "AIAdvisor",
    component: () => import("@/views/AIAdvisor.vue")
  },
  {
    path: "/user",
    name: "UserCenter",
    component: () => import("@/views/UserCenter.vue")
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
