import request from "@/utils/request";

export function fetchTips(params) {
  return request.get("/tips", {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  });
}

export function fetchTipDetail(id) {
  return request.get(`/tips/${id}`);
}

export function createTip(data) {
  return request.post("/tips", data);
}

export function likeTip(id) {
  return request.post(`/tips/${id}/like`);
}

export function collectTip(id) {
  return request.post(`/tips/${id}/collect`);
}

export function deleteTip(id) {
  return request.delete(`/tips/${id}`);
}
