<template>
  <el-card class="tip-card" shadow="hover">
    <div class="tip-card__header">
      <h3 class="title">{{ tip.title }}</h3>
      <el-tag size="small">{{ tip.category }}</el-tag>
    </div>
    <p class="summary">{{ tip.summary }}</p>
    <div class="tip-card__footer">
      <div class="meta">
        <span>作者：{{ tip.author }}</span>
        <span>发布时间：{{ tip.publishTime }}</span>
      </div>
      <div class="actions">
        <span class="icon">
          <el-icon :size="16"><View /></el-icon>
          {{ tip.viewCount }}
        </span>
        <span class="icon">
          <el-icon :size="16"><StarFilled /></el-icon>
          {{ tip.collectCount }}
        </span>
        <span class="icon">
          <el-icon :size="16"><Pointer /></el-icon>
          {{ tip.likeCount }}
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { View, StarFilled, Pointer } from "@element-plus/icons-vue";

const props = defineProps({
  tip: {
    type: Object,
    required: true
  }
});
</script>

<style scoped>
.tip-card {
  margin-bottom: 16px;
  cursor: pointer;
}

.tip-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.summary {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.tip-card__footer {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.icon {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  color: #909399;
}
</style>
