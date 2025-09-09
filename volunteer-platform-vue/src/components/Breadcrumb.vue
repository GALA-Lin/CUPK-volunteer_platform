<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || index === levelList.length - 1" class="no-redirect">
          {{ item.meta.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const levelList = ref([]);

const getBreadcrumb = () => {
  // 我们将从路由的 meta.title 生成面包屑
  let matched = route.matched.filter(item => item.meta && item.meta.title);

  // 添加一个固定的“首页”或“看板”面包屑
  const first = matched[0];
  if (!isDashboard(first)) {
    matched = [{ path: 'admin/dashboard', meta: { title: '数据看板' }}].concat(matched);
  }

  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false);
}

const isDashboard = (route) => {
  const name = route && route.name;
  if (!name) {
    return false;
  }
  return name.trim().toLocaleLowerCase() === 'AdminDashboard'.toLocaleLowerCase();
}

const handleLink = (item) => {
  const { redirect, path } = item;
  if (redirect) {
    router.push(redirect);
    return;
  }
  router.push(path);
}

watch(
    () => route.path,
    (path) => {
      if (path.startsWith('/redirect/')) {
        return;
      }
      getBreadcrumb();
    },
    { immediate: true }
);
</script>

<style scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;
}
.no-redirect {
  color: #97a8be;
  cursor: text;
}
</style>