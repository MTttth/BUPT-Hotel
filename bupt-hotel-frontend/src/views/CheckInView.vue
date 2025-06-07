<!-- src/views/CheckInView.vue -->
<template>
  <div class="checkin-container">
    <el-card class="checkin-card" shadow="hover">
      <!-- 标题区 -->
      <div class="checkin-header">
        <i class="el-icon-user-solid checkin-icon"></i>
        <span class="checkin-title">客人入住</span>
      </div>

      <!-- 入住表单 -->
      <el-form ref="formRef" class="checkin-form" @submit.prevent>
        <el-form-item label="房间号" class="form-item">
          <el-select v-model="roomId" placeholder="请选择空闲房间" clearable filterable>
            <el-option v-for="r in emptyRooms" :key="r.id" :label="`房间 #${r.id}`" :value="r.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="客人姓名" class="form-item">
          <el-input v-model="guest" placeholder="请输入客人姓名" prefix-icon="el-icon-user" clearable />
        </el-form-item>

        <el-form-item class="form-item form-button">
          <el-button type="primary" size="large" round @click="submit">
            确认入住
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoomsStore } from '@/stores/useRoomsStore';
import { ElMessage } from 'element-plus';

const store = useRoomsStore();
const roomId = ref<number | null>(null);
const guest = ref<string>('');

const emptyRooms = computed(() => store.rooms.filter(r => !r.occupied));

function submit() {
  if (!roomId.value || !guest.value.trim()) {
    ElMessage.warning('请填写房间号和客人姓名');
    return;
  }
  try {
    store.checkIn(roomId.value, guest.value.trim());
    ElMessage.success(`房间 #${roomId.value} 已成功入住 ${guest.value.trim()}`);
    roomId.value = null;
    guest.value = '';
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
}
</script>

<style scoped>
.checkin-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 40px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px - 40px);
  /* 考虑到头部高度和内边距 */
}

.checkin-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.4s ease-out;
  background-color: #ffffff;
  padding: 24px;
}

.checkin-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.checkin-icon {
  font-size: 28px;
  color: #409eff;
  margin-right: 8px;
}

.checkin-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  user-select: none;
}

.checkin-form .form-item {
  margin-bottom: 18px;
}

.checkin-form .el-form-item__label {
  width: 80px;
  font-weight: 500;
  color: #606266;
}

.checkin-form .el-input__inner,
.checkin-form .el-select .el-input__inner {
  border-radius: 6px;
  height: 38px;
  font-size: 14px;
}

.form-button {
  text-align: center;
  margin-top: 8px;
}

.el-button--primary {
  width: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border: none;
  transition: background 0.3s;
}

.el-button--primary:hover {
  background: linear-gradient(90deg, #66b1ff, #409eff);
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
