<template>
  <div class="container user-center">
    <!-- 统计看板 -->
    <el-row :gutter="16" class="stats-dashboard">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="发布技巧" :value="stats.publishCount">
            <template #suffix>篇</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="总浏览量" :value="stats.totalViews">
            <template #suffix>次</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="获得点赞" :value="stats.totalLikes">
            <template #suffix>个</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="我的收藏" :value="stats.collectCount" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="我的点赞" :value="stats.likeCount" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="我的评论" :value="stats.commentCount" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容区 -->
    <el-row :gutter="24" class="main-content">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="profile">
            <el-avatar :size="80" :src="userInfo?.avatar">{{ userInfo?.nickname?.[0] }}</el-avatar>
            <h3>{{ userInfo?.nickname }}</h3>
            <p>{{ userInfo?.email }}</p>
            <div class="profile-actions">
              <el-button type="primary" link @click="editVisible = true">编辑资料</el-button>
              <el-button type="primary" link @click="passwordVisible = true">修改密码</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-tabs v-model="activeTab" @tab-change="loadCurrentTabData">
          <el-tab-pane label="我的发布" name="publish">
            <el-empty v-if="myTips.length === 0 && !loadingTips" description="还没有发布任何技巧" />
            <div v-else>
              <div class="tip-list-with-actions">
                <div v-for="tip in myTips" :key="tip.id" class="tip-item">
                  <TipCard :tip="tip" />
                  <el-button 
                    type="danger" 
                    text 
                    size="small"
                    @click="handleDeleteTip(tip.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              <el-pagination
                v-if="tipsTotal > 0"
                v-model:current-page="tipsPage"
                v-model:page-size="tipsPageSize"
                :total="tipsTotal"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleTipsSizeChange"
                @current-change="handleTipsPageChange"
                style="margin-top: 16px; justify-content: center;"
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="我的收藏" name="collect">
            <el-empty v-if="myCollect.length === 0 && !loadingCollects" description="还没有收藏任何技巧" />
            <div v-else>
              <div class="tip-list-with-actions">
                <div v-for="tip in myCollect" :key="tip.id" class="tip-item">
                  <TipCard :tip="tip" />
                  <el-button 
                    type="danger" 
                    text 
                    size="small"
                    @click="handleUncollect(tip.id)"
                  >
                    取消收藏
                  </el-button>
                </div>
              </div>
              <el-pagination
                v-if="collectsTotal > 0"
                v-model:current-page="collectsPage"
                v-model:page-size="collectsPageSize"
                :total="collectsTotal"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleCollectsSizeChange"
                @current-change="handleCollectsPageChange"
                style="margin-top: 16px; justify-content: center;"
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="我的点赞" name="like">
            <el-empty v-if="myLikes.length === 0 && !loadingLikes" description="还没有点赞任何技巧" />
            <div v-else>
              <div class="tip-list-with-actions">
                <div v-for="tip in myLikes" :key="tip.id" class="tip-item">
                  <TipCard :tip="tip" />
                  <el-button 
                    type="danger" 
                    text 
                    size="small"
                    @click="handleUnlike(tip.id)"
                  >
                    取消点赞
                  </el-button>
                </div>
              </div>
              <el-pagination
                v-if="likesTotal > 0"
                v-model:current-page="likesPage"
                v-model:page-size="likesPageSize"
                :total="likesTotal"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleLikesSizeChange"
                @current-change="handleLikesPageChange"
                style="margin-top: 16px; justify-content: center;"
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="我的评论" name="comment">
            <el-empty v-if="myComments.length === 0 && !loadingComments" description="暂无评论" />
            <div v-else>
              <el-timeline>
                <el-timeline-item 
                  v-for="item in myComments" 
                  :key="item.id" 
                  :timestamp="formatTime(item.createTime)"
                >
                  <el-card>
                    <template #header>
                      <div class="comment-header">
                        <span>评论于：<el-link type="primary" @click="goToTip(item.tipId)">{{ item.tipTitle }}</el-link></span>
                        <el-button 
                          type="danger" 
                          text 
                          size="small"
                          @click="handleDeleteComment(item.tipId, item.id)"
                        >
                          删除
                        </el-button>
                      </div>
                    </template>
                    <p>{{ item.content }}</p>
                    <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                      <span>👍 {{ item.likeCount }}</span>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
              <el-pagination
                v-if="commentsTotal > 0"
                v-model:current-page="commentsPage"
                v-model:page-size="commentsPageSize"
                :total="commentsTotal"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleCommentsSizeChange"
                @current-change="handleCommentsPageChange"
                style="margin-top: 16px; justify-content: center;"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- 编辑资料弹窗 -->
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

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password 
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password 
            placeholder="请输入新密码"
          />
          <div class="password-strength" v-if="passwordForm.newPassword">
            密码强度：<span :class="passwordStrengthClass">
              {{ passwordStrengthText }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password 
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import TipCard from "@/components/TipCard.vue";
import { fetchUserInfo, updateUserInfo, changePassword, fetchMyTips, fetchMyCollects, fetchMyLikes, fetchMyComments } from "@/api/user";
import { uploadAvatar } from "@/api/upload";
import { likeTip, collectTip, deleteTip } from "@/api/healthTip";
import { deleteComment } from "@/api/comment";
import { useUserStore } from "@/store/user";

const router = useRouter();
const userStore = useUserStore();

const userInfo = ref(null);
const stats = reactive({ 
  publishCount: 0, 
  totalViews: 0, 
  totalLikes: 0, 
  collectCount: 0, 
  likeCount: 0, 
  commentCount: 0 
});
const activeTab = ref("publish");
const myTips = ref([]);
const myCollect = ref([]);
const myLikes = ref([]);
const myComments = ref([]);

// 分页状态
const tipsPage = ref(1);
const tipsPageSize = ref(10);
const tipsTotal = ref(0);
const loadingTips = ref(false);

const collectsPage = ref(1);
const collectsPageSize = ref(10);
const collectsTotal = ref(0);
const loadingCollects = ref(false);

const likesPage = ref(1);
const likesPageSize = ref(10);
const likesTotal = ref(0);
const loadingLikes = ref(false);

const commentsPage = ref(1);
const commentsPageSize = ref(10);
const commentsTotal = ref(0);
const loadingComments = ref(false);

const editVisible = ref(false);
const editForm = reactive({ nickname: "", email: "", avatar: "" });
const uploading = ref(false);

const passwordVisible = ref(false);
const changingPassword = ref(false);
const passwordFormRef = ref(null);
const passwordForm = reactive({ 
  oldPassword: "", 
  newPassword: "", 
  confirmPassword: "" 
});

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
};

const loadUserInfo = async () => {
  const data = await fetchUserInfo();
  userInfo.value = data.profile;
  Object.assign(stats, data.stats);
  Object.assign(editForm, {
    nickname: userInfo.value.nickname,
    email: userInfo.value.email,
    avatar: userInfo.value.avatar
  });
  // 加载当前Tab的数据
  loadCurrentTabData();
};

const loadCurrentTabData = () => {
  switch (activeTab.value) {
    case "publish":
      loadMyTips();
      break;
    case "collect":
      loadMyCollects();
      break;
    case "like":
      loadMyLikes();
      break;
    case "comment":
      loadMyComments();
      break;
  }
};

const loadMyTips = async () => {
  loadingTips.value = true;
  try {
    const data = await fetchMyTips(tipsPage.value, tipsPageSize.value);
    myTips.value = data?.records || [];
    tipsTotal.value = data?.total || 0;
  } finally {
    loadingTips.value = false;
  }
};

const loadMyCollects = async () => {
  loadingCollects.value = true;
  try {
    const data = await fetchMyCollects(collectsPage.value, collectsPageSize.value);
    myCollect.value = data?.records || [];
    collectsTotal.value = data?.total || 0;
  } finally {
    loadingCollects.value = false;
  }
};

const loadMyLikes = async () => {
  loadingLikes.value = true;
  try {
    const data = await fetchMyLikes(likesPage.value, likesPageSize.value);
    myLikes.value = data?.records || [];
    likesTotal.value = data?.total || 0;
  } finally {
    loadingLikes.value = false;
  }
};

const loadMyComments = async () => {
  loadingComments.value = true;
  try {
    const data = await fetchMyComments(commentsPage.value, commentsPageSize.value);
    myComments.value = data?.records || [];
    commentsTotal.value = data?.total || 0;
  } finally {
    loadingComments.value = false;
  }
};

const handleTipsPageChange = (page) => {
  tipsPage.value = page;
  loadMyTips();
};

const handleTipsSizeChange = (size) => {
  tipsPageSize.value = size;
  tipsPage.value = 1;
  loadMyTips();
};

const handleCollectsPageChange = (page) => {
  collectsPage.value = page;
  loadMyCollects();
};

const handleCollectsSizeChange = (size) => {
  collectsPageSize.value = size;
  collectsPage.value = 1;
  loadMyCollects();
};

const handleLikesPageChange = (page) => {
  likesPage.value = page;
  loadMyLikes();
};

const handleLikesSizeChange = (size) => {
  likesPageSize.value = size;
  likesPage.value = 1;
  loadMyLikes();
};

const handleCommentsPageChange = (page) => {
  commentsPage.value = page;
  loadMyComments();
};

const handleCommentsSizeChange = (size) => {
  commentsPageSize.value = size;
  commentsPage.value = 1;
  loadMyComments();
};

const saveProfile = async () => {
  await updateUserInfo(editForm);
  ElMessage.success("保存成功");
  editVisible.value = false;
  await loadUserInfo();
  // 同步更新 store 中的用户信息，使头部头像同步更新
  if (userInfo.value) {
    userStore.setUser(userStore.token, userInfo.value);
  }
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

// 取消收藏（使用toggle模式）
const handleUncollect = async (tipId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const result = await collectTip(tipId); // toggle模式，已收藏则取消
    if (result.active) {
      ElMessage.warning('操作异常，请刷新页面重试');
    } else {
    ElMessage.success('已取消收藏');
    // 从列表中移除
    myCollect.value = myCollect.value.filter(tip => tip.id !== tipId);
    stats.collectCount--;
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后再试');
    }
  }
};

// 取消点赞（使用toggle模式）
const handleUnlike = async (tipId) => {
  try {
    await ElMessageBox.confirm('确定要取消点赞吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const result = await likeTip(tipId); // toggle模式，已点赞则取消
    if (result.active) {
      ElMessage.warning('操作异常，请刷新页面重试');
    } else {
    ElMessage.success('已取消点赞');
    // 从列表中移除
    myLikes.value = myLikes.value.filter(tip => tip.id !== tipId);
    stats.likeCount--;
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后再试');
    }
  }
};

// 删除发布
const handleDeleteTip = async (tipId) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定要删除这个技巧吗？', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await deleteTip(tipId);
    ElMessage.success('删除成功');
    
    // 从列表中移除
    myTips.value = myTips.value.filter(tip => tip.id !== tipId);
    stats.publishCount--;
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后再试');
    }
  }
};

// 删除评论
const handleDeleteComment = async (tipId, commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await deleteComment(tipId, commentId);
    ElMessage.success('评论已删除');
    
    // 从列表中移除
    myComments.value = myComments.value.filter(comment => comment.id !== commentId);
    stats.commentCount--;
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后再试');
    }
  }
};

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return;
  
  try {
    await passwordFormRef.value.validate();
    
    changingPassword.value = true;
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    ElMessage.success('密码修改成功，请重新登录');
    passwordVisible.value = false;
    
    // 重置表单
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    
    // 3秒后跳转到登录页
    setTimeout(() => {
      router.push('/login');
    }, 3000);
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败');
  } finally {
    changingPassword.value = false;
  }
};

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = passwordForm.newPassword;
  if (!pwd) return 0;
  
  let strength = 0;
  if (pwd.length >= 6) strength++;
  if (pwd.length >= 10) strength++;
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++;
  if (/\d/.test(pwd)) strength++;
  if (/[^a-zA-Z\d]/.test(pwd)) strength++;
  
  return strength;
});

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value;
  if (strength <= 1) return '弱';
  if (strength <= 3) return '中';
  return '强';
});

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value;
  if (strength <= 1) return 'strength-weak';
  if (strength <= 3) return 'strength-medium';
  return 'strength-strong';
});

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.user-center {
  margin-top: 24px;
}

/* 统计看板 */
.stats-dashboard {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-card:hover {
  transform: translateY(-4px);
  transition: all 0.3s ease;
}

/* 主内容区 */
.main-content {
  margin-top: 24px;
}

.profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.profile-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

/* 列表操作按钮 */
.tip-list-with-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  position: relative;
}

.tip-item .el-button {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

/* 头像上传 */
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

/* 密码强度 */
.password-strength {
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}

.strength-weak {
  color: #f56c6c;
  font-weight: 600;
}

.strength-medium {
  color: #e6a23c;
  font-weight: 600;
}

.strength-strong {
  color: #67c23a;
  font-weight: 600;
}

/* 评论头部 */
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
