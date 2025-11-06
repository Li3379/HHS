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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchTipDetail, likeTip, collectTip } from "@/api/healthTip";
import { fetchComments, createComment, likeComment } from "@/api/comment";
import CommentList from "@/components/CommentList.vue";

const route = useRoute();
const tipId = route.params.id;

const loading = ref(false);
const detail = ref(null);
const comments = ref([]);
const commentForm = ref({ content: "" });

const loadDetail = async () => {
  loading.value = true;
  try {
    detail.value = await fetchTipDetail(tipId);
  } finally {
    loading.value = false;
  }
};

const loadComments = async () => {
  comments.value = await fetchComments(tipId);
};

const handleLike = async () => {
  await likeTip(tipId);
  ElMessage.success("点赞成功");
  loadDetail();
};

const handleCollect = async () => {
  await collectTip(tipId);
  ElMessage.success("收藏成功");
  loadDetail();
};

const handleComment = async () => {
  if (!commentForm.value.content) {
    ElMessage.warning("请输入评论内容");
    return;
  }
  await createComment(tipId, commentForm.value);
  commentForm.value.content = "";
  ElMessage.success("评论成功");
  loadComments();
};

const handleLikeComment = async (id) => {
  await likeComment(id);
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
