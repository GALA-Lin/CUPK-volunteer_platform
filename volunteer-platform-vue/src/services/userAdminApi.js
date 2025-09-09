// src/services/userAdminApi.js
import apiClient from '@/api/axios.js';

export const getUsers = (page = 1, pageSize = 10) => {
    return apiClient.get('/api/admin/users', { params: { page, pageSize } });
};

export const createUser = (userData) => {
    return apiClient.post('/api/admin/users', userData);
};

export const updateUser = (id, userData) => {
    return apiClient.put(`/api/admin/users/${id}`, userData);
};

export const deleteUser = (id) => {
    return apiClient.delete(`/api/admin/users/${id}`);
};

/** * 更新用户密码
 * @param {number} id - 用户ID
 * @param {string} newPassword - 新密码
 */

export const updateUserPassword = (id, newPassword) => {
    return apiClient.put(`/api/admin/users/${id}/password`, { newPassword });
};
/**
 * 【新增】获取所有用户的简要列表
 * 这个函数将被 NewsManagement.vue 调用，用于填充发布人下拉框
 * @returns {Promise<any>}
 */
export const getAllUsers = () => {
    return apiClient.get('/api/admin/users/all');
};