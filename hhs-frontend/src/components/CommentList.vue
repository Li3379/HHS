<template>
  <div class="comment-list">
    <div v-if="comments.length === 0" class="empty">暂无评论，快来抢沙发~</div>
    <div v-else>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <span class="author">{{ comment.author }}</span>
          <span class="time">{{ comment.time }}</span>
        </div>
        <div class="content">{{ comment.content }}</div>
        <div class="comment-actions">
          <span class="action" @click="() => onLike(comment.id)">👍 {{ comment.likeCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
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
  justify-content: space-between;
  color: #606266;
  font-size: 14px;
}

.author {
  font-weight: 600;
}

.time {
  color: #909399;
}

.content {
  color: #303133;
  line-height: 1.6;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.action {
  cursor: pointer;
  color: #409eff;
}
</style>
