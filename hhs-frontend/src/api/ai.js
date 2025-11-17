import request from "@/utils/request";

/**
 * AI智能分类
 * @param {Object} data - {title, content}
 */
export function classifyContent(data) {
  return request.post("/ai/classify", data);
}

/**
 * AI对话
 * @param {Object} data - {sessionId, question}
 */
export function chatWithAI(data) {
  return request.post("/ai/chat", data);
}

/**
 * 获取对话历史
 * @param {string} sessionId - 会话ID
 */
export function getChatHistory(sessionId) {
  return request.get("/ai/chat/history", { params: { sessionId } });
}

/**
 * 清空对话历史
 * @param {string} sessionId - 会话ID
 */
export function clearChatHistory(sessionId) {
  return request.delete(`/ai/chat/history/${sessionId}`);
}

/**
 * @deprecated 已废弃，使用chatWithAI代替
 */
export function askHealthAdvisor(data) {
  return chatWithAI(data);
}
