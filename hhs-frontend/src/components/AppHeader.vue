<template>
  <header class="app-header">
    <div class="container header-content">
      <div class="logo" @click="goHome">
        <span class="logo-title">HHS</span>
        <span class="logo-subtitle">Health Hack System</span>
      </div>
      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/publish">发布技巧</RouterLink>
        <RouterLink to="/ai-advisor">AI健康顾问</RouterLink>
        <RouterLink to="/user">个人中心</RouterLink>
      </nav>
      <div class="actions">
        <el-button type="primary" @click="handleLogin">登录 / 注册</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const goHome = () => {
  router.push("/");
};

const handleLogin = () => {
  router.push("/login");
};
</script>

<style scoped>
.app-header {
  background-color: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.header-content {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.logo-title {
  font-size: 20px;
  font-weight: 600;
}

.logo-subtitle {
  font-size: 12px;
  color: #909399;
}

.nav {
  display: flex;
  gap: 24px;
  font-size: 15px;
}

.nav a {
  color: #303133;
}

.nav a.router-link-active {
  color: #409eff;
  font-weight: 600;
}
</style>
