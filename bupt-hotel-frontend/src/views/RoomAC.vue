<!-- src/views/RoomAC.vue -->
<template>
  <div class="client-room-container">
    <el-card v-if="myRoom" class="client-room-card" shadow="hover">
      <!-- 房间信息头 -->
      <div class="room-header">
        <i class="el-icon-house room-icon"></i>
        <span class="room-title">房间 #{{ myRoom.id }}</span>
        <el-tag type="success" size="small" class="room-tag">
          已入住 • {{ myRoom.guest }}
        </el-tag>
      </div>

      <!-- RoomControl 组件 -->
      <div class="room-control-wrapper">
        <RoomControl :room="myRoom" />
      </div>
    </el-card>

    <div v-else class="empty-state">
      <el-empty description="您当前没有入住的房间" />
      <el-button type="primary" @click="goToCheckIn" class="empty-button">
        去入住
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/useAuthStore';
import { useRoomsStore } from '@/stores/useRoomsStore';
import RoomControl from '@/components/RoomControl.vue';
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const store = useRoomsStore();
const router = useRouter();

// 根据登录用户名查找对应房间
const myRoom = computed(() => {
  return store.rooms.find(r => r.id === auth.userRoomId) || null;
});

function goToCheckIn() {
  router.push({ name: 'CheckIn' });
}
</script>

<style scoped>
.client-room-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: center;
}

.client-room-card {
  width: 100%;
  max-width: 600px;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.4s ease-out;
  padding: 24px;
}

.room-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.room-icon {
  font-size: 28px;
  color: #409eff;
  margin-right: 8px;
}

.room-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-right: 12px;
}

.room-tag {
  font-size: 14px;
}

.room-control-wrapper {
  /* 给 RoomControl 一点上下间距 */
  margin-top: 12px;
}

.empty-state {
  text-align: center;
  animation: fadeIn 0.4s ease-out;
}

.empty-state .el-empty {
  margin-bottom: 16px;
}

.empty-button {
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  transition: background 0.3s;
}

.empty-button:hover {
  background: linear-gradient(90deg, #66b1ff, #409eff);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
