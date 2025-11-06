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
            <el-empty v-if="myComments.length === 0" description="暂无评论" />
            <el-timeline v-else>
              <el-timeline-item 
                v-for="item in myComments" 
                :key="item.id" 
                :timestamp="formatTime(item.createTime)"
              >
                <el-card>
                  <p>{{ item.content }}</p>
                  <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                    评论于：<el-link type="primary" @click="goToTip(item.tipId)">{{ item.tipTitle }}</el-link>
                    <span style="margin-left: 16px;">👍 {{ item.likeCount }}</span>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="editVisible" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="editForm.avatar || userInfo?.avatar">
              {{ editForm.nickname?.[0] || userInfo?.nickname?.[0] }}
            </el-avatar>
            <el-upload
              class="upload-btn"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/jpeg,image/png,image/jpg,image/gif"
              :auto-upload="true"
              :limit="1"
            >
              <el-button type="primary" :loading="uploading" size="default">
                {{ uploading ? '上传中...' : '选择图片' }}
              </el-button>
            </el-upload>
            <div class="upload-tips">支持 jpg、png 格式，大小不超过 2MB</div>
          </div>
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
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import TipCard from "@/components/TipCard.vue";
import { fetchUserInfo, updateUserInfo } from "@/api/user";
import { uploadAvatar } from "@/api/upload";

const router = useRouter();

const userInfo = ref(null);
const stats = reactive({ publishCount: 0, likeCount: 0, collectCount: 0 });
const activeTab = ref("publish");
const myTips = ref([]);
const myCollect = ref([]);
const myComments = ref([]);

const editVisible = ref(false);
const editForm = reactive({ nickname: "", email: "", avatar: "" });
const uploading = ref(false);

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

const beforeAvatarUpload = (file) => {
  console.log('准备上传文件:', file.name, '类型:', file.type, '大小:', file.size);
  
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleAvatarUpload = async ({ file }) => {
  console.log('开始上传头像:', file);
  uploading.value = true;
  try {
    const url = await uploadAvatar(file);
    console.log('上传成功，返回URL:', url);
    editForm.avatar = url;
    ElMessage.success('头像上传成功');
  } catch (error) {
    console.error('头像上传失败:', error);
    const errorMsg = error?.response?.data?.message || error.message || '未知错误';
    ElMessage.error('头像上传失败：' + errorMsg);
  } finally {
    uploading.value = false;
  }
};

const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  return date.toLocaleString("zh-CN");
};

const goToTip = (tipId) => {
  router.push(`/tips/${tipId}`);
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

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-btn {
  width: 100%;
  display: flex;
  justify-content: center;
}

.upload-tips {
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
