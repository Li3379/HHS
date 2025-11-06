import { defineStore } from "pinia";
import { getToken, setToken, removeToken } from "@/utils/auth";

export const useUserStore = defineStore("user", {
  state: () => ({
    token: getToken(),
    info: null
  }),
  actions: {
    setUser(token, info) {
      this.token = token;
      this.info = info;
      setToken(token);
    },
    clearUser() {
      this.token = "";
      this.info = null;
      removeToken();
    }
  }
});
