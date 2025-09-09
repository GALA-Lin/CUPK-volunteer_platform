import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios.js';

export const useUserStore = defineStore('user', () => {
    const router = useRouter();

    // --- State (状态) ---
    const token = ref(localStorage.getItem('token') || null);
    const currentUser = ref(JSON.parse(localStorage.getItem('user')) || null);

    // 在 store 初始化时，如果 localStroage 中存在 token，
    // 就立即将其设置到 axios 的默认请求头中。
    // 这确保了即使用户刷新页面，后续的API请求也能携带正确的认证信息。
    if (token.value) {
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
    }

    // --- Getters (计算属性) ---
    const isLoggedIn = computed(() => !!token.value);
    const isAdmin = computed(() => currentUser.value?.role === 'admin' || currentUser.value?.role === 'super_admin');

    // --- Actions (方法) ---
    async function fetchCurrentUser() {
        if (!token.value) return;
        try {
            currentUser.value = await apiClient.get('/api/users/me');
            localStorage.setItem('user', JSON.stringify(currentUser.value));
        } catch (error) {
            console.error('获取用户信息失败:', error);
            await logout();
        }
    }

    async function login(credentials) {
        const responseData = await apiClient.post('/api/auth/login', credentials);
        token.value = responseData.token;
        localStorage.setItem('token', responseData.token);

        // 登录成功后，更新axios的默认请求头
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${responseData.token}`;

        await fetchCurrentUser();
        await router.push('/profile');
    }

    async function updateCurrentUser(profileUpdateDTO) {
        try {
            const response = await apiClient.put('/api/users/me', profileUpdateDTO);
            currentUser.value = response.data.data;
            localStorage.setItem('user', JSON.stringify(currentUser.value));
            alert('信息更新成功！');
        } catch (error) {
            console.error('更新用户信息失败:', error);
            alert('更新失败，请重试。');
            throw error;
        }
    }

    async function adminLogin(credentials) {
        const responseData = await apiClient.post('/api/admin/auth/login', credentials);
        token.value = responseData.token;
        localStorage.setItem('token', responseData.token);
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${responseData.token}`;
        await fetchCurrentUser();
    }

    async function logout() {
        try {
            if (token.value) {
                await apiClient.post('/api/auth/logout');
            }
        } catch (error) {
            console.error("调用登出接口失败，但将继续执行本地登出:", error);
        } finally {
            token.value = null;
            currentUser.value = null;
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            delete apiClient.defaults.headers.common['Authorization'];
            await router.push('/login');
        }
    }

    return {
        token,
        currentUser,
        isLoggedIn,
        isAdmin,
        adminLogin,
        login,
        logout,
        fetchCurrentUser,
        updateCurrentUser,
    };
});