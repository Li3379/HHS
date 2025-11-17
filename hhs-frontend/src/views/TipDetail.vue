<template>
  <div class="container detail" v-loading="loading">
    <el-card shadow="always" class="detail-card">
      <div class="detail-header">
        <h1>{{ detail?.title }}</h1>
        <div class="meta">
          <el-tag size="small">{{ detail?.category }}</el-tag>
          <span>作者：{{ detail?.author }}</span>
          <span>发布时间：{{ detail?.publishTime }}</span>
        </div>
        <div class="tags">
          <el-tag v-for="tag in detail?.tags" :key="tag" type="info" effect="plain">{{ tag }}</el-tag>
        </div>
        <div class="actions">
          <el-button type="primary" :icon="detail?.liked ? 'Check' : ''" @click="handleLike">
            {{ detail?.liked ? '已点赞' : '点赞' }} ({{ detail?.likeCount || 0 }})
          </el-button>
          <el-button :icon="detail?.collected ? 'Check' : ''" @click="handleCollect">
            {{ detail?.collected ? '已收藏' : '收藏' }} ({{ detail?.collectCount || 0 }})
          </el-button>
        </div>
      </div>
      <div class="content" v-html="detail?.content"></div>
    </el-card>

    <el-card class="comment-card" shadow="never">
      <h3>评论</h3>
      <el-form :model="commentForm" ref="commentFormRef" class="comment-form">
        <el-form-item>
          <el-input v-model="commentForm.content" type="textarea" :rows="4" placeholder="分享你的看法" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleComment">发布评论</el-button>
        </el-form-item>
      </el-form>
      <CommentList :comments="comments" @like="handleLikeComment" />
      <el-pagination
        v-if="commentTotal > 0"
        v-model:current-page="commentPage"
        v-model:page-size="commentPageSize"
        :total="commentTotal"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleCommentSizeChange"
        @current-change="handleCommentPageChange"
        style="margin-top: 16px; justify-content: center;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { fetchTipDetail, likeTip, collectTip } from "@/api/healthTip";
import { fetchComments, createComment, likeComment } from "@/api/comment";
import CommentList from "@/components/CommentList.vue";
import { useUserStore } from "@/store/user";

const route = useRoute();
const router = useRouter();
const tipId = route.params.id;

const loading = ref(false);
const detail = ref(null);
const comments = ref([]);
const commentForm = ref({ content: "" });
const commentPage = ref(1);
const commentPageSize = ref(20);
const commentTotal = ref(0);
const userStore = useUserStore();
const isLoggedIn = computed(() => !!userStore.token);

const loadDetail = async () => {
  loading.value = true;
  try {
    detail.value = await fetchTipDetail(tipId);
  } finally {
    loading.value = false;
  }
};

const loadComments = async () => {
  const data = await fetchComments(tipId, commentPage.value, commentPageSize.value);
  comments.value = data?.records || [];
  commentTotal.value = data?.total || 0;
};

const handleCommentPageChange = (page) => {
  commentPage.value = page;
  loadComments();
};

const handleCommentSizeChange = (size) => {
  commentPageSize.value = size;
  commentPage.value = 1;
  loadComments();
};

const ensureLogin = async () => {
  if (isLoggedIn.value) return true;
  try {
    await ElMessageBox.confirm(
      "登录后即可点赞、收藏、评论，与社区互动",
      "需要登录",
      {
        confirmButtonText: "立即登录",
        cancelButtonText: "稍后",
        type: "info"
      }
    );
    router.push({ path: "/login", query: { redirect: route.fullPath } });
  } catch (e) {
    // 用户取消
  }
  return false;
};

const handleLike = async () => {
  if (!(await ensureLogin())) return;
  const result = await likeTip(tipId);
  if (result.active) {
    ElMessage.success("点赞成功");
  } else {
    ElMessage.success("已取消点赞");
  }
  loadDetail();
};

const handleCollect = async () => {
  if (!(await ensureLogin())) return;
  const result = await collectTip(tipId);
  if (result.active) {
    ElMessage.success("收藏成功");
  } else {
    ElMessage.success("已取消收藏");
  }
  loadDetail();
};

const handleComment = async () => {
  if (!(await ensureLogin())) return;
  if (!commentForm.value.content) {
    ElMessage.warning("请输入评论内容");
    return;
  }
  await createComment(tipId, commentForm.value);
  commentForm.value.content = "";
  ElMessage.success("评论成功");
  loadComments();
};

const handleLikeComment = async (commentId) => {
  if (!(await ensureLogin())) return;
  await likeComment(tipId, commentId);
  loadComments();
};

onMounted(() => {
  loadDetail();
  loadComments();
});
</script>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 24px;
}

.detail-card {
  padding: 24px;
}

.detail-header {
  margin-bottom: 24px;
}

.detail-header h1 {
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.meta {
  display: flex;
  gap: 12px;
  margin: 12px 0;
  color: #909399;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.content {
  line-height: 1.8;
  color: #303133;
}

.comment-card {
  padding: 24px;
}

.comment-form {
  margin-bottom: 16px;
}
</style>
