<!-- src/App.vue -->
<template>
  <el-container class="app-container">
    <!-- ========= 头部 ========= -->
    <el-header class="app-header">
      <div class="header-left">
        <span class="app-title">中央空调管理系统</span>
      </div>

      <el-menu
        :default-active="$route.path"
        mode="horizontal"
        router
        class="nav-menu"
      >
        <el-menu-item index="/">
          <i class="el-icon-s-tools"></i>
          控制面板
        </el-menu-item>
        <el-menu-item index="/checkin">
          <i class="el-icon-user-solid"></i>
          入住
        </el-menu-item>
        <el-menu-item index="/checkout">
          <i class="el-icon-document"></i>
          结账
        </el-menu-item>
        <el-menu-item index="/monitor">
          <i class="el-icon-monitor"></i>
          监控
        </el-menu-item>
      </el-menu>
    </el-header>

    <!-- ========= 主体内容 ========= -->
    <el-main class="app-main">
      <router-view />
    </el-main>

    <el-footer class="app-footer">
      © 2025 Central-AC 管理系统 · 版权所有
    </el-footer>
  </el-container>
</template>

<script setup lang="ts">
</script>

<style scoped>
.app-container {
  height: 100vh;
}

/* ===== 头部样式 ===== */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: linear-gradient(90deg, #1f2d3d, #2e3e50);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

/* Logo + 标题 区域 */
.header-left {
  display: flex;
  align-items: center;
}
.logo {
  height: 36px;
  margin-right: 12px;
}
.app-title {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  user-select: none;
}

/* 导航菜单 区域 */
.nav-menu {
  flex: 1;               /* 让菜单拉伸占满中间剩余空间 */
  background: transparent; 
  border-bottom: none;   /* 去掉默认下边框 */
  margin-left: 40px;     /* 左侧留白，让 Logo 区更突出 */
}

.nav-menu .el-menu-item {
  padding: 0 20px !important;
  color: #e0e6ed;        /* 菜单字体偏亮 */
  font-size: 16px;
  transition: color 0.2s;
}
.nav-menu .el-menu-item:hover {
  color: #ffd04b;        /* 滑过时黄色高亮 */
}
.nav-menu .el-menu-item.is-active {
  color: #ffd04b;        /* 激活路由时黄色高亮 */
  border-bottom: 3px solid #ffd04b; /* 底部小黄线 */
}

/* 右侧用户菜单 区域 */
.header-right {
  display: flex;
  align-items: center;
}
.drop-trigger {
  color: #e0e6ed;
  user-select: none;
}
.user-name {
  margin: 0 6px;
  font-size: 15px;
}

/* ===== 主体内容 ===== */
.app-main {
  padding: 24px;
  /* 淡雅灰背景，让卡片 / 表单等更突出 */
  background-color: #f5f7fa;
  overflow-y: auto;
}

/* ===== 页脚 ===== */
.app-footer {
  text-align: center;
  padding: 12px 0;
  font-size: 13px;
  color: #909399;
  background-color: #ffffff;
  box-shadow: 0 -1px 8px rgba(0, 0, 0, 0.05);
}
</style>
