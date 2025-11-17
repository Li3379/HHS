<template>
  <div class="container advisor">
    <el-card class="chat-card">
      <div class="chat-header">
        <div>
          <h2>AI 健康顾问</h2>
          <p>提问任何健康相关问题，AI将结合最佳实践给出建议</p>
        </div>
        <div class="remaining-count">
          今日剩余次数：<span>{{ remainingCount }}</span>
        </div>
      </div>
      <div class="chat-window" ref="chatWindow">
        <div v-if="loadingHistory" class="loading-hint">
          <p>正在加载历史记录...</p>
        </div>
        <div v-else-if="messages.length === 0" class="empty-hint">
          <p>👋 你好！我是AI健康顾问</p>
          <p>有任何健康问题都可以问我哦~</p>
        </div>
        <div v-for="item in messages" :key="item.id" :class="['message', item.role]">
          <div class="avatar">{{ item.role === 'user' ? '我' : 'AI' }}</div>
          <div class="bubble" :class="{ 'thinking': item.thinking }">
            <template v-if="item.thinking">
              <div class="thinking-animation">
                <span>Thinking</span>
                <span class="dots">
                  <span class="dot">.</span>
                  <span class="dot">.</span>
                  <span class="dot">.</span>
                </span>
              </div>
            </template>
            <template v-else>
              <p v-for="(line, index) in formatContent(item.content)" :key="index">{{ line }}</p>
              <span class="time">{{ item.time }}</span>
            </template>
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
          <el-button
            type="primary"
            :loading="loading"
            @click="handleAsk"
          >
            发送
          </el-button>
          <el-button @click="handleReset">清空对话</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from "vue";
import { ElMessage } from "element-plus";
import dayjs from "dayjs";
import { chatWithAI, clearChatHistory, getChatHistory } from "@/api/ai";

const chatWindow = ref();
const question = ref("");
const loading = ref(false);
const loadingHistory = ref(false);
const messages = reactive([]);
const sessionId = ref("");
const remainingCount = ref(20); // 剩余次数

// 从localStorage获取或生成sessionId
const getOrCreateSessionId = () => {
  const stored = localStorage.getItem("ai_session_id");
  const today = dayjs().format("YYYY-MM-DD");
  const storedDate = localStorage.getItem("ai_session_date");
  
  // 如果是新的一天，重新生成sessionId
  if (!stored || storedDate !== today) {
    const newSessionId = "session_" + Date.now();
    localStorage.setItem("ai_session_id", newSessionId);
    localStorage.setItem("ai_session_date", today);
    return newSessionId;
  }
  
  return stored;
};

const scrollToBottom = () => {
  nextTick(() => {
    const el = chatWindow.value;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  });
};

// 加载历史会话记录
const loadChatHistory = async () => {
  // 检查是否是新创建的session（根据创建时间判断）
  const storedDate = localStorage.getItem("ai_session_date");
  const today = dayjs().format("YYYY-MM-DD");
  const isNewSession = storedDate !== today;
  
  // 新session不需要加载历史，直接返回
  if (isNewSession) {
    console.log("新会话，跳过历史加载");
    return;
  }
  
  loadingHistory.value = true;
  try {
    const response = await getChatHistory(sessionId.value);
    if (response && response.history && response.history.length > 0) {
      // 清空现有消息
      messages.splice(0, messages.length);
      // 加载历史消息
      response.history.forEach((item, index) => {
        messages.push({
          id: Date.now() + index,
          role: "user",
          content: item.question,
          time: dayjs(item.createTime).format("HH:mm")
        });
        messages.push({
          id: Date.now() + index + 1,
          role: "assistant",
          content: item.answer,
          time: dayjs(item.createTime).format("HH:mm")
        });
      });
      scrollToBottom();
    }
    // 更新剩余次数
    if (response && typeof response.remainingCount === 'number') {
      remainingCount.value = response.remainingCount;
    }
  } catch (error) {
    console.error("加载历史记录失败:", error);
    // 加载失败不影响使用，只是从空白状态开始
  } finally {
    loadingHistory.value = false;
  }
};

// 初始化
onMounted(async () => {
  // 获取或创建sessionId
  sessionId.value = getOrCreateSessionId();
  // 加载历史记录
  await loadChatHistory();
});

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
  
  // 添加 AI 思考中的占位消息
  const thinkingMessageId = Date.now() + 1;
  const thinkingMessage = {
    id: thinkingMessageId,
    role: "assistant",
    thinking: true,
    content: "",
    time: ""
  };
  messages.push(thinkingMessage);
  scrollToBottom();
  
  loading.value = true;
  const currentQuestion = question.value;
  question.value = "";
  
  try {
    const response = await chatWithAI({ 
      sessionId: sessionId.value,
      question: currentQuestion 
    });
    
    // 找到并替换思考中的消息
    const thinkingIndex = messages.findIndex(msg => msg.id === thinkingMessageId);
    if (thinkingIndex !== -1) {
      messages.splice(thinkingIndex, 1, {
        id: thinkingMessageId,
        role: "assistant",
        thinking: false,
        content: response.answer,
        time: dayjs().format("HH:mm")
      });
    }
    
    remainingCount.value = response.remainingCount;
    if (remainingCount.value <= 5) {
      ElMessage.warning(`今日剩余 ${remainingCount.value} 次咨询机会`);
    }
  } catch (error) {
    // 出错时移除思考中的消息
    const thinkingIndex = messages.findIndex(msg => msg.id === thinkingMessageId);
    if (thinkingIndex !== -1) {
      messages.splice(thinkingIndex, 1);
    }
    ElMessage.error(error.message || "AI服务异常，请稍后再试");
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

const handleReset = async () => {
  try {
    await clearChatHistory(sessionId.value);
    messages.splice(0, messages.length);
    // 生成新的sessionId并保存
    const newSessionId = "session_" + Date.now();
    sessionId.value = newSessionId;
    localStorage.setItem("ai_session_id", newSessionId);
    // 重置剩余次数为满额（登录用户20次）
    remainingCount.value = 20;
    ElMessage.success("对话已清空");
  } catch (error) {
    ElMessage.error("清空失败，请稍后再试");
  }
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

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.chat-header p {
  color: #606266;
  margin-top: 8px;
}

.remaining-count {
  padding: 8px 16px;
  background-color: #f4f4f5;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.remaining-count span {
  color: #409eff;
  font-weight: 600;
  font-size: 18px;
  margin-left: 4px;
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

.loading-hint,
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  text-align: center;
}

.empty-hint p:first-child {
  font-size: 20px;
  margin-bottom: 8px;
}

.empty-hint p:last-child {
  font-size: 14px;
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

/* Thinking 动画样式 */
.bubble.thinking {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  min-width: 120px;
}

.thinking-animation {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

.thinking-animation .dots {
  display: flex;
  gap: 2px;
}

.thinking-animation .dot {
  animation: thinking-blink 1.4s infinite;
  opacity: 0;
}

.thinking-animation .dot:nth-child(1) {
  animation-delay: 0s;
}

.thinking-animation .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-animation .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes thinking-blink {
  0%, 20% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  80%, 100% {
    opacity: 0;
  }
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
