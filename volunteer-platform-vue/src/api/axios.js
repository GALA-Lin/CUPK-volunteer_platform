// 文件路径: /volunteer-platform-vue/src/api/axios.js
import axios from 'axios';
import { ElMessage } from 'element-plus';
// 引入 userStore，但在拦截器内部使用以避免循环依赖问题
import { useUserStore } from '@/stores/userStore';

const apiClient = axios.create({
    baseURL: '',
    headers: {
        'Content-Type': 'application/json'
    }
});

// 请求拦截器：自动携带 token
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 响应拦截器，用于处理后端返回的 {code, message, data} 结构
// 添加一个请求拦截器
apiClient.interceptors.request.use(
    (config) => {
        // 在发送请求之前，从 localStorage 中获取 token
        const token = localStorage.getItem('token');
        if (token) {
            // 如果 token 存在，则将其添加到请求的 Authorization 头中
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);


// 添加一个响应拦截器
apiClient.interceptors.response.use(
    response => {
        // 后端统一响应结构为 { code, message, data }
        const res = response.data;

        // code 存在，说明是后端自定义的响应结构
        if (typeof res.code !== 'undefined') {
            const successCodes = [200, 201]; // 定义成功的业务码
            if (successCodes.includes(res.code)) {
                // 如果业务码是成功的，直接返回 data 部分给业务逻辑使用
                return res.data;
            } else {
                // 如果业务码是失败的，显示错误信息
                ElMessage.error(res.message || '操作失败');

                // 如果是未登录或权限不足的错误码，可以触发登出逻辑
                if (res.code === 401 || res.code === 403) {
                    const userStore = useUserStore();
                    userStore.logout();
                }

                // 将失败的Promise继续向下传递，让业务代码的catch块可以捕获
                return Promise.reject(new Error(res.message || 'Error'));
            }
        }

        // 如果 code 不存在，说明可能是直接返回的文件流或其他非标准JSON，直接返回整个 response.data
        return response.data;
    },
    error => {
        // 处理网络错误或非2xx的HTTP状态码
        console.error('API Error:', error.response); // 打印详细错误
        ElMessage.error(error.response?.data?.message || '网络错误，请稍后再试');
        return Promise.reject(error);
    }
);

export default apiClient;