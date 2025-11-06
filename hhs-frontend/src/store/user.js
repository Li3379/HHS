import { defineStore } from "pinia";
import { getToken, setToken, removeToken } from "@/utils/auth";

const USER_INFO_KEY = "HHS_USER_INFO";

const getUserInfo = () => {
  const info = window.localStorage.getItem(USER_INFO_KEY);
  return info ? JSON.parse(info) : null;
};

const setUserInfo = (info) => {
  window.localStorage.setItem(USER_INFO_KEY, JSON.stringify(info));
};

const removeUserInfo = () => {
  window.localStorage.removeItem(USER_INFO_KEY);
};

export const useUserStore = defineStore("user", {
  state: () => ({
    token: getToken(),
    info: getUserInfo()
  }),
  actions: {
    setUser(token, info) {
      this.token = token;
      this.info = info;
      setToken(token);
      setUserInfo(info);
    },
    clearUser() {
      this.token = "";
      this.info = null;
      removeToken();
      removeUserInfo();
    }
  }
});
