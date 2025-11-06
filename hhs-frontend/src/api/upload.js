import request from "@/utils/request";

export function uploadAvatar(file) {
  const formData = new FormData();
  formData.append("file", file);
  
  return request.post("/upload/avatar", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
}

