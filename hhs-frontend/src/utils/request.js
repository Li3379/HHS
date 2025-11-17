import axios from 'axios';
import { getToken, removeToken } from './auth';
import { ElMessage } from 'element-plus';
import router from '@/router';

/**
 * 创建 axios 实例
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 请求拦截器
 * 在请求发送前统一添加 Token（仅非公开接口）
 */
request.interceptors.request.use(
  config => {
    // 定义公开接口模式（不需要token的接口）
    const publicPatterns = [
      /^\/auth\//,                    // 认证接口
      /^\/tips(\?|\/|$)/,            // 小贴士列表和详情
      /^\/users\/\d+/,               // 用户公开信息
      /\/comments(\?|\/|$)/,         // 评论列表
    ];
    
    // 检查当前请求是否为公开接口
    const isPublicAPI = publicPatterns.some(pattern => 
      pattern.test(config.url)
    );
    
    // 只有非公开接口才添加token
    if (!isPublicAPI) {
      const token = getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 响应拦截器
 * 统一处理响应和错误
 */
request.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 如果返回的状态码不是 200，说明业务出错
    if (res.code !== 200) {
      // 401 在访客模式下仅提示
      if (res.code === 401) {
        ElMessage.warning(res.message || '请先登录');
      } else {
        ElMessage.error(res.message || '请求失败');
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }
    
    // 返回数据部分
    return res.data;
  },
  error => {
    console.error('响应错误:', error);
    
    // 处理 HTTP 错误状态码
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // 公开页面（首页、详情页等）不强制跳转，只提示
          const currentPath = router.currentRoute.value.path;
          const publicPaths = ['/', '/tips'];
          const isPublicPage = publicPaths.some(path => currentPath === path || currentPath.startsWith(path + '/'));
          
          if (isPublicPage) {
            // 访客在公开页面，只提示不跳转
            ElMessage.warning('该操作需要登录');
          } else {
            // 受保护页面，跳转登录
            ElMessage.warning('未登录或登录已过期，请先登录');
            removeToken();
            router.push({ path: '/login', query: { redirect: currentPath } });
          }
          break;
        case 403:
          ElMessage.error('没有权限访问该资源');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
          ElMessage.error(data?.message || '服务器内部错误');
          break;
        default:
          ElMessage.error(data?.message || '网络请求失败');
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      ElMessage.error('网络连接失败，请检查网络');
    } else {
      // 其他错误
      ElMessage.error(error.message || '请求失败');
    }
    
    return Promise.reject(error);
  }
);

export default request;

