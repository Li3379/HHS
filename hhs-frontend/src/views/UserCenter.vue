<template>
  <div class="container user-center">
    <el-row :gutter="24">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="profile">
            <el-avatar :size="80" :src="userInfo?.avatar">{{ userInfo?.nickname?.[0] }}</el-avatar>
            <h3>{{ userInfo?.nickname }}</h3>
            <p>{{ userInfo?.email }}</p>
            <el-button type="primary" link @click="editVisible = true">编辑资料</el-button>
          </div>
        </el-card>

        <el-card shadow="hover" class="stats-card">
          <h4>个人数据</h4>
          <ul>
            <li>发布技巧：{{ stats.publishCount }}</li>
            <li>收到点赞：{{ stats.likeCount }}</li>
            <li>收藏技巧：{{ stats.collectCount }}</li>
          </ul>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="我的发布" name="publish">
            <TipCard v-for="tip in myTips" :key="tip.id" :tip="tip" />
          </el-tab-pane>
          <el-tab-pane label="我的收藏" name="collect">
            <TipCard v-for="tip in myCollect" :key="tip.id" :tip="tip" />
          </el-tab-pane>
          <el-tab-pane label="我的评论" name="comment">
            <el-timeline>
              <el-timeline-item v-for="item in myComments" :key="item.id" :timestamp="item.time">
                {{ item.content }}
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="editVisible" title="编辑资料" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="头像链接">
          <el-input v-model="editForm.avatar" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import TipCard from "@/components/TipCard.vue";
import { fetchUserInfo, updateUserInfo } from "@/api/user";

const userInfo = ref(null);
const stats = reactive({ publishCount: 0, likeCount: 0, collectCount: 0 });
const activeTab = ref("publish");
const myTips = ref([]);
const myCollect = ref([]);
const myComments = ref([]);

const editVisible = ref(false);
const editForm = reactive({ nickname: "", email: "", avatar: "" });

const loadUserInfo = async () => {
  const data = await fetchUserInfo();
  userInfo.value = data.profile;
  Object.assign(stats, data.stats);
  myTips.value = data.publishList || [];
  myCollect.value = data.collectList || [];
  myComments.value = data.commentList || [];
  Object.assign(editForm, {
    nickname: userInfo.value.nickname,
    email: userInfo.value.email,
    avatar: userInfo.value.avatar
  });
};

const saveProfile = async () => {
  await updateUserInfo(editForm);
  ElMessage.success("保存成功");
  editVisible.value = false;
  loadUserInfo();
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.user-center {
  margin-top: 24px;
}

.profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.stats-card {
  margin-top: 16px;
}

.stats-card ul {
  list-style: none;
  padding: 0;
  margin-top: 12px;
  line-height: 1.8;
  color: #606266;
}
</style>
