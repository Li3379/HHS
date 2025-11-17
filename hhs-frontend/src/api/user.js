import request from "@/utils/request";

export function login(data) {
  return request.post("/auth/login", data);
}

export function register(data) {
  return request.post("/auth/register", data);
}

export function fetchUserInfo() {
  return request.get("/user/profile");
}

export function updateUserInfo(data) {
  return request.put("/user/profile", data);
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request.put("/user/password", data);
}

/**
 * 获取我的发布（分页）
 */
export function fetchMyTips(page = 1, size = 10) {
  return request.get("/user/tips", {
    params: { page, size }
  });
}

/**
 * 获取我的收藏（分页）
 */
export function fetchMyCollects(page = 1, size = 10) {
  return request.get("/user/collects", {
    params: { page, size }
  });
}

/**
 * 获取我的点赞（分页）
 */
export function fetchMyLikes(page = 1, size = 10) {
  return request.get("/user/likes", {
    params: { page, size }
  });
}

/**
 * 获取我的评论（分页）
 */
export function fetchMyComments(page = 1, size = 10) {
  return request.get("/user/comments", {
    params: { page, size }
  });
}
