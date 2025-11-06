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
