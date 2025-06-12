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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoomsStore } from '@/stores/useRoomsStore';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { useRouter } from 'vue-router';
const store = useRoomsStore();
const router = useRouter();
const roomId = ref<number | null>(null);
const guest = ref<string>('');

// 用来存后端拉回的空闲房间 ID 列表
const emptyRoomIds = ref<number[]>([]);

// 计算真正可选的房间对象列表
const emptyRooms = computed(() => {
  // 如果你希望直接用 ID 数组，也可以只返回 emptyRoomIds.value
  return store.rooms.filter(r => emptyRoomIds.value.includes(r.id));
});

async function fetchEmptyRooms() {
  try {
    const res = await axios.get('api/get_emptyroom');
    if (res.data.code === 200 && Array.isArray(res.data.data.empty_room_list)) {
      emptyRoomIds.value = res.data.data.empty_room_list;
    } else {
      console.warn('拉取空房失败：', res.data.msg);
    }
  } catch (err: any) {
    console.error('网络错误，获取空闲房间失败', err);
  }
}

// 在组件挂载时马上拉一次，并启动定时器
let timer: ReturnType<typeof setInterval>;
onMounted(() => {
  fetchEmptyRooms();
  timer = setInterval(fetchEmptyRooms, 30 * 1000); // 每 30s 刷新一次
});

// 在卸载时清理定时器
onBeforeUnmount(() => {
  clearInterval(timer);
});

async function submit() {
  if (!roomId.value || !guest.value.trim()) {
    ElMessage.warning('请填写房间号和客人姓名');
    return;
  }
  try {
    const res = await axios.post('api/check_in', {
      room_id: roomId.value,
    });
    if (res.data.code === 200) {
      ElMessage.success('Check in successfully');
      router.push({ name: 'RoomAC', params: { id: roomId.value } });
      // 重置表单
      roomId.value = null;
      guest.value = '';
      // 立即刷新一下空房列表，避免刚入住的房间仍出现在选项中
      await fetchEmptyRooms();
    } else {
      ElMessage.error(res.data.msg || 'Check in failed');
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.msg || '网络错误，稍后重试');
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
