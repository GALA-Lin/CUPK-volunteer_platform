<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="profile-card">
        <h1 class="card-title">用户中心</h1>
        <p class="card-subtitle">查看或更新你的个人信息</p>

        <div v-if="!userStore.currentUser && !error" class="loading-skeleton">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="userStore.currentUser" class="profile-form">
          <div class="avatar-section">
            <img :src="userStore.currentUser.avatarUrl || defaultAvatar" alt="Avatar" class="avatar">
            <h2 class="username">{{ userStore.currentUser.username }}</h2>
            <p class="user-role">{{ userStore.currentUser.role }}</p>
          </div>

<!--          <div class="service-hours-display">-->
<!--            <div class="hours-value">{{ formattedTotalHours }}</div>-->
<!--            <div class="hours-label">累计志愿时长 (小时)</div>-->
<!--          </div>-->
          <div class="service-hours-display">
            <div class="hours-label">累计志愿时长 (小时)
              <el-button
                @click="refreshUserData"
                size="small"
                type="primary"
                :icon="Refresh"
                circle
                style="margin-left: 10px"
                title="刷新数据"
            ></el-button></div>

            <div class="hours-value">{{ userStore.currentUser.totalServiceHours }}</div>

          </div>

          <el-form :model="editForm" label-position="top" :disabled="!isEditing">
            <el-form-item label="真实姓名">
              <el-input v-model="editForm.realName" />
            </el-form-item>
            <el-form-item label="学号">
              <div
                  class="fake-input"
                  @click="showStudentIdAlert"
              >
                {{ userStore.currentUser.studentId || 'N/A' }}
              </div>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="editForm.phoneNumber" />
            </el-form-item>
          </el-form>

          <div class="actions">
            <el-button v-if="!isEditing" @click="startEditing" type="primary" round>编辑信息</el-button>
            <template v-if="isEditing">
              <el-button @click="handleSave" type="success" round>保存更改</el-button>
              <el-button @click="cancelEditing" round>取消</el-button>
            </template>
          </div>

          <div class="logout-section">
            <el-button @click="handleLogout" type="danger" plain round>退出登录</el-button>
          </div>
        </div>
        <div v-else class="loading">正在加载用户信息...</div>
      </div>

      <div class="details-tabs">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="时长明细" name="records">
            <div v-if="loadingRecords">加载中...</div>
            <el-timeline v-else-if="serviceRecords.length > 0">
              <el-timeline-item
                  v-for="record in serviceRecords"
                  :key="record.activityId"
                  :timestamp="formatDateTime(record.recordedAt)"
                  placement="top"
              >
                <el-card>
                  <h4>{{ record.activityTitle }}</h4>
                  <p class="hours-badge">+ {{ record.serviceHours?.toFixed(2) }} 小时</p>
                  <p class="remarks" v-if="record.remarks">{{ record.remarks }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无时长记录"></el-empty>
          </el-tab-pane>

          <el-tab-pane label="我报名的活动" name="enrollments">
            <div v-if="loadingEnrollments">加载中...</div>
            <div v-else-if="enrolledActivities.length > 0" class="enrolled-list">
              <div v-for="activity in enrolledActivities" :key="activity.activityId" class="activity-item">
                <div class="activity-info">
                  <p class="activity-title">{{ activity.title }}</p>
                  <p class="activity-time">时间: {{ formatDateTime(activity.startTime) }}</p>
                </div>
                <el-tag :type="getStatusType(activity.status)">{{ activity.status }}</el-tag>
              </div>
            </div>
            <el-empty v-else description="没有报名的活动"></el-empty>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useUserStore } from '@/stores/userStore.js';
import { getServiceRecords, getEnrolledActivities } from '@/services/profileApi.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from "vue-router";
import {Refresh} from "@element-plus/icons-vue";

const userStore = useUserStore();
const router = useRouter();
const isEditing = ref(false);
const defaultAvatar = 'https://ui-avatars.com/api/?name=User&background=random';
const error = ref(null);

const activeTab = ref('records');
const serviceRecords = ref([]);
const enrolledActivities = ref([]);
const loadingRecords = ref(false);
const loadingEnrollments = ref(false);

const editForm = ref({
  realName: '',
  email: '',
  phoneNumber: ''
});
const showStudentIdAlert = () => {
  ElMessage.warning('学号不可修改，如需修改请联系管理员');
};

watch(() => userStore.currentUser, (newProfile) => {
  if (newProfile) {
    editForm.value.realName = newProfile.realName;
    editForm.value.email = newProfile.email;
    editForm.value.phoneNumber = newProfile.phoneNumber;
  }
}, { immediate: true, deep: true });

const formattedTotalHours = computed(() => {
  const hours = userStore.currentUser?.totalServiceHours;
  if (typeof hours === 'number' || typeof hours === 'string') {
    return parseFloat(hours).toFixed(2);
  }
  return '0.00';
});

const fetchServiceRecords = async () => {
  loadingRecords.value = true;
  try {
    serviceRecords.value = await getServiceRecords();
  } catch (err) {
    ElMessage.error('获取时长明细失败');
  } finally {
    loadingRecords.value = false;
  }
};

const fetchEnrolledActivities = async () => {
  loadingEnrollments.value = true;
  try {
    enrolledActivities.value = await getEnrolledActivities();
  } catch (err) {
    ElMessage.error('获取报名活动失败');
  } finally {
    loadingEnrollments.value = false;
  }
};

onMounted(async () => {
  try {
    if (!userStore.currentUser) {
      await userStore.fetchCurrentUser();
    }
    await fetchServiceRecords();
    await fetchEnrolledActivities();
  } catch (err) {
    error.value = '无法加载用户信息，请重新登录。';
  }
});
const refreshUserData = async () => {
  try {
    await userStore.fetchCurrentUser();
    // Refresh both service records and enrolled activities
    await Promise.all([
      fetchServiceRecords(),
      fetchEnrolledActivities()
    ]);
  } catch (error) {
    console.error('Failed to refresh user data:', error);
  }
};

const startEditing = () => { isEditing.value = true; };

const cancelEditing = () => {
  isEditing.value = false;
  if (userStore.currentUser) {
    editForm.value.realName = userStore.currentUser.realName;
    editForm.value.email = userStore.currentUser.email;
    editForm.value.phoneNumber = userStore.currentUser.phoneNumber;
  }
};

const handleSave = async () => {
  try {
    await userStore.updateCurrentUser(editForm.value);
    isEditing.value = false;
    ElMessage.success('信息更新成功！');
  } catch (err) {
    // store中已处理提示
  }
};


const handleLogout = () => {
  ElMessageBox.confirm('您确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(async () => {
        await userStore.logout();
        ElMessage.success('您已成功退出');
      })
      .catch(() => {});
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString('zh-CN');
}

const getStatusType = (status) => {
  const statusMap = {
    recruiting: 'success',
    ongoing: 'primary',
    finished: 'info',
    canceled: 'warning',
  };
  return statusMap[status] || 'info';
}

</script>

<style scoped>
.profile-page{min-height:calc(100vh - 120px);padding:40px;background-color:#f4f7f9}.profile-container{max-width:1200px;margin:0 auto;display:grid;grid-template-columns:350px 1fr;gap:30px}.profile-card{background-color:#fff;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,.08);padding:30px}.card-title{font-size:24px;text-align:center;color:#333}.card-subtitle{text-align:center;color:#777;margin-bottom:25px}.avatar-section{text-align:center;margin-bottom:20px}.avatar{width:100px;height:100px;border-radius:50%;border:4px solid #fff;box-shadow:0 2px 8px rgba(0,0,0,.1);margin-bottom:10px}.username{font-size:20px;font-weight:600}.user-role{font-size:14px;color:#999}.service-hours-display{text-align:center;margin:20px 0;padding:15px;background-color:#f0faff;border-radius:8px}.hours-value{font-size:32px;font-weight:700;color:#0056b3}.hours-label{font-size:14px;color:#0056b3}.actions{margin-top:25px;display:flex;justify-content:center;gap:15px}.logout-section{text-align:center;margin-top:25px;padding-top:20px;border-top:1px solid #f0f0f0}.details-tabs{background-color:#fff;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,.08);padding:20px 30px}.hours-badge{color:#409eff;font-weight:700}.remarks{color:#909399;font-size:14px;margin-top:8px}.enrolled-list{display:flex;flex-direction:column;gap:15px}.activity-item{display:flex;justify-content:space-between;align-items:center;padding:15px;border:1px solid #e9ecef;border-radius:8px}.activity-title{font-weight:500}.activity-time{font-size:14px;color:#6c757d}
.fake-input {
  width: 100%;
  padding: 8px 11px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f5f5;
  color: #c0c4cc;
  cursor: not-allowed;
  font-size: 14px;
  line-height: 1.5;
}
</style>