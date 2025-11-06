import request from "@/utils/request";

export function fetchComments(tipId) {
  return request.get(`/tips/${tipId}/comments`);
}

export function createComment(tipId, data) {
  return request.post(`/tips/${tipId}/comments`, data);
}

export function likeComment(id) {
  return request.post(`/comments/${id}/like`);
}
