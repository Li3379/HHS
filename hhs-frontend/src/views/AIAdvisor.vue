<template>
  <div class="container advisor">
    <el-card class="chat-card">
      <div class="chat-header">
        <h2>AI 健康顾问</h2>
        <p>提问任何健康相关问题，AI将结合最佳实践给出建议</p>
      </div>
      <div class="chat-window" ref="chatWindow">
        <div v-for="item in messages" :key="item.id" :class="['message', item.role]">
          <div class="avatar">{{ item.role === 'user' ? '我' : 'AI' }}</div>
          <div class="bubble">
            <p v-for="(line, index) in formatContent(item.content)" :key="index">{{ line }}</p>
            <span class="time">{{ item.time }}</span>
          </div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="question"
          type="textarea"
          :rows="3"
          placeholder="输入你的问题，例如：如何在办公室保持健康？"
          @keydown.enter.prevent="handleAsk"
        />
        <div class="actions">
          <el-button type="primary" :loading="loading" @click="handleAsk">发送</el-button>
          <el-button @click="handleReset">清空对话</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from "vue";
import { ElMessage } from "element-plus";
import dayjs from "dayjs";
import { askHealthAdvisor } from "@/api/ai";

const chatWindow = ref();
const question = ref("");
const loading = ref(false);
const messages = reactive([]);

const scrollToBottom = () => {
  nextTick(() => {
    const el = chatWindow.value;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  });
};

const handleAsk = async () => {
  if (!question.value.trim()) {
    ElMessage.warning("请输入问题");
    return;
  }
  const userMessage = {
    id: Date.now(),
    role: "user",
    content: question.value,
    time: dayjs().format("HH:mm")
  };
  messages.push(userMessage);
  loading.value = true;
  const currentQuestion = question.value;
  question.value = "";
  try {
    const answer = await askHealthAdvisor({ question: currentQuestion });
    messages.push({
      id: Date.now() + 1,
      role: "assistant",
      content: answer,
      time: dayjs().format("HH:mm")
    });
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

const handleReset = () => {
  messages.splice(0, messages.length);
};

const formatContent = (text = "") => text.split("\n");
</script>

<style scoped>
.advisor {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.chat-card {
  width: 900px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-header p {
  color: #606266;
  margin-top: 8px;
}

.chat-window {
  height: 420px;
  background-color: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message.user .avatar {
  background-color: #67c23a;
}

.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #303133;
  line-height: 1.6;
}

.message.user .bubble {
  background: #e8f5f1;
}

.time {
  align-self: flex-end;
  color: #909399;
  font-size: 12px;
}

.chat-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
