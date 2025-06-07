<!-- src/views/ClientLogin.vue -->
<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="login-header">
        <i class="el-icon-user login-icon"></i>
        <span class="login-title">前台登录</span>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="login-form" @submit.prevent>
        <el-form-item label="经理ID" prop="username">
          <el-input v-model="form.username" placeholder="请输入前台ID" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" />
        </el-form-item>
        <el-form-item class="button-item">
          <el-button type="primary" size="large" round @click="onLogin" :loading="loading">
            登录
          </el-button>
        </el-form-item>
        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon class="login-alert" />
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { ElMessage } from 'element-plus'

// 假设你有一个后端接口 /client/login，用来校验客户用户名/密码
import axios from 'axios'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入前台ID', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const loading = ref(false)
const errorMsg = ref<string | null>(null)

async function onLogin() {
  await formRef.value.validate((valid: boolean) => {
    if (!valid) return
  })
  loading.value = true
  try {
    // 调用后端登录接口（示例路径）
    const res = await axios.post('/login/reception', {
      username: form.username,
      password: form.password,
    })
    if (res.data.status === 1) {
      authStore.loginAsAdmin("reception")
      ElMessage.success('登录成功')
      // 登录成功后，跳回原本想去的页面或 "/"
      const redirectPath = (route.query.redirect as string) || '/'
      router.replace(redirectPath)
    } else {
      errorMsg.value = res.data.message || '登录失败'
    }
  } catch (err: any) {
    errorMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
  padding-top: 40px;
}

.login-card {
  width: 380px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: #ffffff;
  padding: 24px;
  animation: fadeIn 0.4s ease-out;
}

.login-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.login-icon {
  font-size: 28px;
  color: #409eff;
  margin-right: 8px;
}

.login-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.login-form .el-form-item {
  margin-bottom: 18px;
}

.login-form .el-input__inner {
  border-radius: 6px;
  height: 38px;
  font-size: 14px;
}

.button-item {
  text-align: center;
  margin-top: 8px;
}

.login-alert {
  margin-top: 12px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
