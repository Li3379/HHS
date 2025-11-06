import request from "@/utils/request";

export function askHealthAdvisor(data) {
  return request.post("/ai/advisor", data);
}

export function classifyContent(data) {
  return request.post("/ai/classify", data);
}
