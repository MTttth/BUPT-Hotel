<!-- src/views/RoomAC.vue -->
<template>
  <div class="client-room-container">
    <el-card v-if="roomState" class="client-room-card" shadow="hover">
      <!-- 房间信息头 -->
      <div class="room-header">
        <i class="el-icon-house room-icon"></i>
        <span class="room-title">房间 #{{ roomId }}</span>
        <el-tag :type="roomState.status ? 'success' : 'info'" size="small" class="room-tag">
          {{ roomState.status ? '空调已开启' : '空调已关闭' }}
        </el-tag>
      </div>

      <!-- RoomControl 组件 -->
      <div class="room-control-wrapper">
        <RoomControl :room="apiRoom" />
      </div>
    </el-card>

    <!-- 空状态：没获取到 roomState，就展示“去入住”按钮 -->
    <div v-else class="empty-state">
      <el-empty description="尚未入住此房间" />
      <el-button type="primary" @click="goToCheckIn" class="empty-button">
        去入住
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import RoomControl from '@/components/RoomControl.vue'
import type { Wind , RoomDetail } from '@/types/Room'

const router = useRouter()
const props = defineProps<{ id?: string }>()

// 2. 转成数字
const roomId = props.id ? Number(props.id) : NaN

// 3. 如果 id 无效（NaN），直接跳去入住页
if (isNaN(roomId)) {
  router.replace({ name: 'CheckIn' })
}

// 保存后端心跳状态
const roomState = ref<{
  current_temp: number
  target_temp: number
  current_speed: Wind
  target_speed: Wind
  electrical_usage: number
  fee: number
  status: number
} | null>(null)

let timer: ReturnType<typeof setInterval>

// 心跳接口：获取最新状态
async function fetchStatus() {
  try {
    const res = await axios.post('/status_heartbeat', { room_id: roomId })
    if (res.data.code === 200 && res.data.data) {
      roomState.value = res.data.data
    } else {
      roomState.value = null
      ElMessage.error(res.data.msg || '心跳请求失败')
    }
  } catch (err: any) {
    roomState.value = null
    ElMessage.error(err.response?.data?.msg || '网络错误，获取状态失败')
  }
}

// 将后端字段映射成 LocalRoom 传给 RoomControl
const apiRoom = computed<RoomDetail>(() => {
  const s = roomState.value!
  return {
    roomId: roomId,
    guest: '', // 这里可以根据需要填充客人姓名
    currentTemp: s.current_temp,
    targetTemp: s.target_temp,
    currentSpeed: s.current_speed,
    targetSpeed: s.target_speed,
    electricalUsage: s.electrical_usage,
    cost: s.fee,
    status: s.status === 1, // 1=开机 0=关机
    roomStatus: 1 // 假设房间状态为占用
  }
})

// 挂载时立即心跳一次，且每 30s 刷新
onMounted(() => {
  fetchStatus()
  timer = setInterval(fetchStatus, 30_000)
})
onBeforeUnmount(() => {
  clearInterval(timer)
})

// “去入住” 按钮
function goToCheckIn() {
  router.push({ name: 'CheckIn' })
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
  background-color: #fff;
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
