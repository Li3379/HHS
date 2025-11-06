<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <h2 class="title">登录 HHS 账号</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          <el-button type="text" @click="goRegister">没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/user";
import { useUserStore } from "@/store/user";

const router = useRouter();
const userStore = useUserStore();

const formRef = ref();
const loading = ref(false);
const form = reactive({
  username: "",
  password: ""
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      const data = await login(form);
      userStore.setUser(data.token, data.userInfo);
      ElMessage.success("登录成功");
      router.push("/");
    } finally {
      loading.value = false;
    }
  });
};

const goRegister = () => {
  router.push("/register");
};
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  margin-top: 80px;
}

.auth-card {
  width: 400px;
  padding: 24px;
}

.title {
  text-align: center;
  margin-bottom: 24px;
}
</style>
