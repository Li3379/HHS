<template>
  <div class="comment-list">
    <div v-if="comments.length === 0" class="empty">暂无评论，快来抢沙发~</div>
    <div v-else>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <el-avatar :size="40" :src="comment.avatar" class="avatar">
            {{ comment.username?.[0] || "游客" }}
          </el-avatar>
          <div class="info">
            <div class="author">{{ comment.username || "匿名用户" }}</div>
            <div class="time">{{ formatTime(comment.createTime) }}</div>
          </div>
        </div>
        <div class="content">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button text type="primary" size="small" @click="() => onLike(comment.id)">
            <el-icon><Pointer /></el-icon>
            赞 {{ comment.likeCount || 0 }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Pointer } from "@element-plus/icons-vue";

const props = defineProps({
  comments: {
    type: Array,
    default: () => []
  }
});

const emits = defineEmits(["like"]);

const onLike = (id) => {
  emits("like", id);
};

const formatTime = (time) => {
  if (!time) return "刚刚";
  const date = new Date(time);
  return date.toLocaleString("zh-CN");
};
</script>

<style scoped>
.comment-list {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty {
  color: #909399;
  text-align: center;
  padding: 24px 0;
}

.comment-item {
  background-color: #f6f7fb;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-header {
  display: flex;
  gap: 12px;
  align-items: center;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author {
  font-weight: 600;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 12px;
}

.avatar {
  background-color: #409eff;
  color: #fff;
}

.content {
  color: #303133;
  line-height: 1.6;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.action {
  cursor: pointer;
  color: #409eff;
}
</style>
