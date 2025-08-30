<template>
  <div class="auth-page-container">
    <div class="background-shapes"></div>
    <main class="auth-main-content">
      <div class="auth-card-wrapper">
        <div class="auth-showcase-panel">
          <div class="showcase-content">
            <div class="logo">
              <img src="@/assets/new-logo.ico" class="h-8" alt="油炬智愿 Logo" />
              <h1>油炬智愿</h1>
            </div>
            <p class="showcase-quote">
              一个专为中国石油大学师生打造的，志愿服务平台。
            </p>
          </div>
          <div class="showcase-footer">
            <p>© 2025 油炬智愿. All Rights Reserved.</p>
          </div>
        </div>

        <div class="auth-form-panel">
          <div class="top-navigation">
            <router-link to="/" class="nav-button">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
              <span>返回首页</span>
            </router-link>
            <button @click="goBack" class="nav-button">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 8 8 12 12 16"></polyline><line x1="16" y1="12" x2="8" y2="12"></line></svg>
              <span>返回上一页</span>
            </button>
          </div>

          <div class="form-header">
            <h2 class="form-title">{{ title }}</h2>
            <p class="form-subtitle">{{ subtitle }}</p>
          </div>

          <div v-if="!isAdminMode" class="auth-tabs">
            <button
                :class="{ active: currentTab === 'login' }"
                @click="switchTab('login')"
            >
              登录
            </button>
            <button
                :class="{ active: currentTab === 'register' }"
                @click="switchTab('register')"
            >
              注册
            </button>
          </div>

          <div class="form-container">
            <transition name="form-slide" mode="out-in">
              <div v-if="currentTab === 'login'" key="login" class="auth-form">
                <div class="form-group">
                  <label for="login-username">用户名</label>
                  <input type="text" id="login-username" :placeholder="usernamePlaceholder" v-model="loginForm.username" />
                </div>
                <div class="form-group">
                  <label for="login-password">密码</label>
                  <input type="password" id="login-password" placeholder="请输入您的密码" v-model="loginForm.password" />
                </div>
                <div v-if="!isAdminMode" class="form-options">
                  <div class="checkbox-group">
                    <input type="checkbox" id="remember-me" v-model="loginForm.rememberMe" />
                    <label for="remember-me">记住我</label>
                  </div>
                  <a href="#" class="form-link">忘记密码?</a>
                </div>
                <p v-if="errorMessage" class="error-message">
                  {{ errorMessage }}
                </p>
                <button class="submit-button" @click="handleLogin">
                  <span v-if="isAdminMode">登 录</span>
                  <span v-else>安全登录</span>
                </button>
                <div v-if="!isAdminMode" class="social-login-divider">
                  <span>或使用其他方式登录</span>
                </div>
                <div v-if="!isAdminMode" class="social-buttons">
                  <button class="social-button gitee">
                    <img src="https://gitee.com/favicon.ico" alt="Gitee 登录"/>
                    <span>Gitee</span>
                  </button>
                  <button class="social-button github">
                    <img src="https://github.githubassets.com/assets/pinned-octocat-093da3e6fa40.svg" alt="Github 登录" />
                    <span>GitHub</span>
                  </button>
                </div>
              </div>

              <div v-else-if="currentTab === 'register' && !isAdminMode" key="register" class="auth-form is-register">
                <div class="form-group">
                  <label for="register-username">用户名</label>
                  <input type="text" id="register-username" placeholder="建议使用学号" v-model="registerForm.username" />
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label for="register-realname">真实姓名</label>
                    <input type="text" id="register-realname" placeholder="请输入您的姓名" v-model="registerForm.realName" />
                  </div>
                  <div class="form-group">
                    <label for="register-studentid">学号</label>
                    <input type="text" id="register-studentid" placeholder="请输入您的学号" v-model="registerForm.studentId" />
                  </div>
                </div>
                <div class="form-group">
                  <label for="register-email">邮箱</label>
                  <input type="email" id="register-email" placeholder="用于接收重要通知" v-model="registerForm.email" />
                </div>
                <div class="form-group">
                  <label for="register-password">设置密码</label>
                  <input type="password" id="register-password" placeholder="至少8位，包含大小写字母、数字和特殊字符中的至少三种" v-model="registerForm.password" />
                </div>
                <div class="form-group">
                  <label for="register-confirm-password">确认密码</label>
                  <input type="password" id="register-confirm-password" placeholder="请再次输入密码" v-model="registerForm.confirmPassword" />
                </div>
                <div class="terms-agreement">
                  <input type="checkbox" id="terms" v-model="registerForm.terms" />
                  <label for="terms">我已阅读并同意<a href="#" class="form-link">服务条款</a>和<a href="#" class="form-link">隐私政策</a></label>
                </div>
                <p v-if="errorMessage" class="error-message">
                  {{ errorMessage }}
                </p>
                <button class="submit-button" @click="handleRegister" :disabled="!registerForm.terms">
                  立即注册
                </button>
              </div>
            </transition>
          </div>

          <div class="switch-mode-footer">
            <router-link v-if="!isAdminMode" to="/admin/login" class="form-link subtle">
              进入后台管理
            </router-link>
            <router-link v-else to="/login" class="form-link subtle">
              返回普通用户登录
            </router-link>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore.js";
import apiClient from "@/api/axios.js";

const props = defineProps({
  mode: {
    type: String,
    default: 'user',
    validator: (value) => ['user', 'admin'].includes(value)
  }
});

const router = useRouter();
const userStore = useUserStore();
const currentTab = ref("login");
const errorMessage = ref("");
const isAdminMode = computed(() => props.mode === 'admin');
const goBack = () => router.go(-1);

const title = computed(() => {
  if (isAdminMode.value) return '后台管理系统';
  return currentTab.value === 'login' ? '欢迎回来!' : '创建您的账户';
});
const subtitle = computed(() => {
  if (isAdminMode.value) return '仅限授权管理员登录';
  return currentTab.value === 'login' ? '很高兴再次见到您。' : '开启您的志愿服务之旅。';
});
const usernamePlaceholder = computed(() => isAdminMode.value ? '请输入管理员用户名' : '请输入您的用户名/学号');

const loginForm = ref({ username: "", password: "", rememberMe: false });
const registerForm = ref({ username: "", realName: "", studentId: "", email: "", password: "", confirmPassword: "", terms: false });

const handleLogin = async () => {
  errorMessage.value = "";
  try {
    const loginPayload = { username: loginForm.value.username, password: loginForm.value.password };
    if (isAdminMode.value) {
      await userStore.adminLogin(loginPayload);
      await router.push("/admin/activities");
    } else {
      await userStore.login(loginPayload);
      await router.push("/");
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || `登录认证失败，请检查您的凭据。`;
  }
};

const handleRegister = async () => {
  errorMessage.value = "";
  const { password, confirmPassword } = registerForm.value;
  if (password !== confirmPassword) {
    errorMessage.value = "两次输入的密码不一致！";
    return;
  }
  if (!registerForm.value.username || !registerForm.value.realName || !registerForm.value.studentId || !registerForm.value.email || !registerForm.value.password) {
    errorMessage.value = "所有字段均为必填项。";
    return;
  }
  try {
    await apiClient.post("/api/auth/register", {
      username: registerForm.value.username,
      realName: registerForm.value.realName,
      studentId: registerForm.value.studentId,
      email: registerForm.value.email,
      password: registerForm.value.password,
    });
    alert("注册成功！现在您可以登录了。");
    switchTab("login");
    Object.keys(registerForm.value).forEach(key => {
      registerForm.value[key] = typeof registerForm.value[key] === 'boolean' ? false : '';
    });
  } catch(error) {
    errorMessage.value = error.response?.data?.message || '注册失败，请稍后重试。';
  }
};

const switchTab = (tab) => {
  currentTab.value = tab;
  errorMessage.value = '';
}
</script>

<style scoped>
/* ===== 全局样式与背景 ===== */
.auth-page-container {
  font-family: var(--font-sans);
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  background-color: var(--color-background);
}

.background-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
      radial-gradient(circle at 15% 85%, hsla(217, 91%, 60%, 0.1), transparent 30%),
      radial-gradient(circle at 85% 20%, hsla(142, 69%, 52%, 0.1), transparent 30%);
  z-index: 1;
}

/* ===== 主卡片布局 ===== */
.auth-main-content {
  position: relative;
  z-index: 2;
  width: 100%;
  display: flex;
  justify-content: center;
}

.auth-card-wrapper {
  width: 100%;
  max-width: 1000px;
  min-height: 680px;
  display: grid;
  grid-template-columns: 1fr;
  background-color: var(--color-surface);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

/* ===== 左侧展示面板 ===== */
.auth-showcase-panel {
  display: none;
  background: linear-gradient(145deg, #1e3a8a 0%, #3b82f6 100%);
  color: white;
  padding: 3rem;
  flex-direction: column;
  justify-content: space-between;
}

.showcase-content .logo {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.showcase-content .logo img {
  width: 50px;
  height: 50px;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.2));
}

.showcase-content .logo h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
}

.showcase-quote {
  font-size: 1.25rem;
  line-height: 1.7;
  opacity: 0.9;
  max-width: 400px;
}

.showcase-footer {
  font-size: 0.875rem;
  opacity: 0.6;
}

/* ===== 右侧表单面板 ===== */
.auth-form-panel {
  display: flex;
  flex-direction: column;
  padding: 2rem;
  position: relative;
}

.top-navigation {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  display: flex;
  gap: 1rem;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: none;
  border: 1px solid transparent;
  padding: 0.3rem 0.6rem;
  border-radius: 6px;
  cursor: pointer;
  color: var(--color-text-muted);
  font-size: 0.85rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}
.nav-button:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text-body);
}


.form-header {
  text-align: left;
  margin-bottom: 1.5rem;
  margin-top: 3rem;
}

.form-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--color-text-heading);
  margin: 0 0 0.25rem 0;
}

.form-subtitle {
  font-size: 0.95rem;
  color: var(--color-text-muted);
  margin: 0;
}

/* ===== 登录/注册切换标签 ===== */
.auth-tabs {
  display: flex;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.auth-tabs button {
  padding: 0.75rem 0.25rem;
  margin-right: 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  position: relative;
  transition: color 0.3s ease;
}

.auth-tabs button::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-primary);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.auth-tabs button.active {
  color: var(--color-primary);
}

.auth-tabs button.active::after {
  transform: scaleX(1);
}

/* ===== 表单容器与动画 ===== */
.form-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.auth-form.is-register {
  gap: 0.8rem;
}

/* ===== 表单组与输入框 ===== */
.form-row { display: flex; gap: 0.75rem; }
.form-row .form-group { flex: 1; }

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-body);
  margin-bottom: 0.3rem;
}

.form-group input[type="text"],
.form-group input[type="password"],
.form-group input[type="email"] {
  width: 100%;
  padding: 0.75rem 1rem;
  font-size: 0.95rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-background-soft);
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
  background-color: var(--color-surface);
}

/* ===== 表单选项与链接 ===== */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}
.checkbox-group, .terms-agreement {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.checkbox-group label, .terms-agreement label {
  color: var(--color-text-body);
  margin: 0;
  font-weight: 400;
  font-size: 0.85rem;
  line-height: 1.5;
}
.checkbox-group input, .terms-agreement input {
  width: 1rem;
  height: 1rem;
  border-radius: 4px;
  flex-shrink: 0;
}
.form-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.form-link:hover {
  color: var(--color-primary-hover);
  text-decoration: underline;
}

/* ===== 提交按钮 ===== */
.submit-button {
  width: 100%;
  padding: 0.85rem;
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(to right, #3b82f6, #2563eb);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-top: 0.75rem;
}
.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 20px -5px rgba(59, 130, 246, 0.4);
}
.submit-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* ===== 第三方登录 ===== */
.social-login-divider { display: flex; align-items: center; text-align: center; color: #9ca3af; font-size: 0.75rem; margin: 0.25rem 0; }
.social-login-divider::before, .social-login-divider::after { content: ''; flex: 1; border-bottom: 1px solid var(--color-border); }
.social-login-divider:not(:empty)::before { margin-right: .5em; }
.social-login-divider:not(:empty)::after { margin-left: .5em; }
.social-buttons { display: flex; gap: 1rem; }
.social-button { flex: 1; display: flex; align-items: center; justify-content: center; gap: 0.75rem; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--color-border); background-color: var(--color-surface); cursor: pointer; font-size: 0.9rem; font-weight: 500; transition: all 0.2s; }
.social-button:hover { border-color: #9ca3af; background-color: var(--color-background-soft); }
.social-button img { width: 20px; height: 20px; }

/* ===== 底部与错误提示 ===== */
.switch-mode-footer { text-align: center; margin-top: auto; padding-top: 1.5rem; border-top: 1px solid var(--color-border); }
.form-link.subtle { font-size: 0.9rem; color: var(--color-text-muted); }
.form-link.subtle:hover { color: var(--color-text-heading); }
.error-message { color: #c81e1e; background-color: #fee2e2; border-radius: 8px; padding: 0.75rem 1rem; font-size: 0.9rem; font-weight: 500; text-align: center; margin: 0.25rem 0; }

/* ===== 动画效果 ===== */
.form-slide-enter-active, .form-slide-leave-active { transition: opacity 0.2s, transform 0.2s ease-out; }
.form-slide-enter-from { opacity: 0; transform: translateX(20px); }
.form-slide-leave-to { opacity: 0; transform: translateX(-20px); }

/* ===== 响应式设计 (桌面端) ===== */
@media (min-width: 1024px) {
  .auth-form-panel {
    padding: 2.5rem 3.5rem;
  }
  .auth-card-wrapper {
    grid-template-columns: 4fr 5fr;
  }
  .auth-showcase-panel {
    display: flex;
  }
}
@media (max-width: 480px) {
  .top-navigation { position: static; margin-bottom: 1rem; justify-content: space-between; }
  .form-header { margin-top: 1rem; }
}
</style>