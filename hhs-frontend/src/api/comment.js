import request from "@/utils/request";

export function fetchComments(tipId, page = 1, size = 20) {
  return request.get(`/tips/${tipId}/comments`, {
    params: { page, size }
  });
}

export function createComment(tipId, data) {
  return request.post(`/tips/${tipId}/comments`, data);
}

export function likeComment(tipId, commentId) {
  return request.post(`/tips/${tipId}/comments/${commentId}/like`);
}

export function deleteComment(tipId, commentId) {
  return request.delete(`/tips/${tipId}/comments/${commentId}`);
}
