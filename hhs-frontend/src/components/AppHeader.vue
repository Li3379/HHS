<template>
  <header class="app-header">
    <div class="container header-content">
      <div class="logo" @click="goHome">
        <span class="logo-title">健捕</span>
        <span class="logo-subtitle">Health Hack System</span>
      </div>
      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <a @click="handleNavClick('/ai-advisor')" class="nav-link">AI健康顾问</a>
        <a @click="handleNavClick('/publish')" class="nav-link">发布技巧</a>
        <a @click="handleNavClick('/user')" class="nav-link">个人中心</a>
      </nav>
      <div class="actions">
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar">
                {{ userInfo?.nickname?.[0] }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <span class="guest-label">访客模式</span>
          <el-button type="primary" @click="handleLogin">登录 / 注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import { ArrowDown, User, SwitchButton } from "@element-plus/icons-vue";
import { useUserStore } from "@/store/user";

const router = useRouter();
const userStore = useUserStore();

const isLoggedIn = computed(() => !!userStore.token);
const userInfo = computed(() => userStore.info);

const goHome = () => {
  router.push("/");
};

const handleLogin = () => {
  router.push("/login");
};

const handleNavClick = async (path) => {
  if (!isLoggedIn.value) {
    // 访客点击受限功能，弹出登录引导
    try {
      await ElMessageBox.confirm(
        "该功能需要登录使用，登录后可使用完整功能",
        "需要登录",
        {
          confirmButtonText: "立即登录",
          cancelButtonText: "稍后",
          type: "info"
        }
      );
      router.push({ path: "/login", query: { redirect: path } });
    } catch (e) {
      // 用户取消
    }
  } else {
    // 已登录用户，直接跳转
    router.push(path);
  }
};

const handleCommand = (command) => {
  if (command === "profile") {
    router.push("/user");
  } else if (command === "logout") {
    handleLogout();
  }
};

const handleLogout = () => {
  ElMessageBox.confirm(
    "确定要退出登录吗？",
    "提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }
  ).then(() => {
    userStore.clearUser();
    ElMessage.success("已退出登录");
    router.push("/");
  }).catch(() => {
    // 用户取消
  });
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
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s;
}

.nav a:hover {
  color: #409eff;
}

.nav a.router-link-active {
  color: #409eff;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.guest-label {
  margin-right: 12px;
  font-size: 13px;
  color: #909399;
}

.username {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
