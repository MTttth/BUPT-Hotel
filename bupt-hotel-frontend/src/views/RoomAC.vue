<!-- src/views/RoomAC.vue -->
<template>
  <div class="client-room-container">
    <!-- 1. 如果没有 id 参数，或者 id 无效，就让用户选房 -->
    <div v-if="!roomId" class="select-room">
      <el-card class="select-card" shadow="hover">
        <div class="select-header">
          <i class="el-icon-house"></i>
          <span>请选择要控制的房间</span>
        </div>
        <el-select v-model="selectedId" placeholder="选择已入住房间" @change="onSelect" style="width: 200px;">
          <el-option v-for="r in occupiedRooms" :key="r.id" :label="`房间 #${r.id}`" :value="r.id" />
        </el-select>
      </el-card>
      <el-empty description="暂无可控制的房间" v-if="occupiedRooms.length === 0" />
      <div class="goto-checkin">
        <el-button type="primary" @click="goToCheckIn">去入住</el-button>
      </div>
    </div>

    <!-- 2. 如果选了 id 但是接口没有查到 roomState，则提示未入住 -->
    <div v-else-if="roomState === null" class="empty-state">
      <el-empty description="该房间尚未入住或不存在" />
      <el-button type="primary" @click="resetSelect">重新选择</el-button>
    </div>

    <!-- 3. 正常：房间已入住，展示心跳+RoomControl -->
    <el-card v-else class="client-room-card" shadow="hover">
      <div class="room-header">
        <i class="el-icon-house room-icon"></i>
        <span class="room-title">房间 #{{ roomId }}</span>
        <el-tag :type="roomState.status ? 'success' : 'info'" size="small">
          {{ roomState.status ? '空调已开启' : '空调已关闭' }}
        </el-tag>
      </div>
      <div class="room-control-wrapper">
        <RoomControl :room="apiRoom" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import RoomControl from '@/components/RoomControl.vue'
import { useRoomsStore } from '@/stores/useRoomsStore'
import type { RoomDetail, Wind } from '@/types/Room'

const router = useRouter()
const route = useRoute()
const store = useRoomsStore()

// 1. 路由参数 id
const routeId = route.params.id as string | undefined
const roomId = ref<number | null>(routeId ? Number(routeId) : null)
// 2. 下拉选中的 id
const selectedId = ref<number | null>(null)

// 3. 心跳接口状态
const roomState = ref<null | {
  current_temp: number
  target_temp: number
  current_speed: Wind
  target_speed: Wind
  electrical_usage: number
  fee: number
  status: number
}>(null)

let timer: ReturnType<typeof setInterval>

// 4. 已入住房间列表（从全局 store 过滤）
const occupiedRooms = computed<RoomDetail[]>(() =>
  store.rooms.filter(r => r.roomStatus === 1)
)

// 5. apiRoom 映射
const apiRoom = computed<RoomDetail>(() => {
  const s = roomState.value!
  return {
    id: roomId.value!,
    guest: '',                // 如果需要，可自行填充
    currentTemp: s.current_temp,
    targetTemp: s.target_temp,
    currentSpeed: s.current_speed,
    targetSpeed: s.target_speed,
    electricalUsage: s.electrical_usage,
    cost: s.fee,
    status: s.status === 1,
    roomStatus: 1
  }
})

// 6. 心跳拉取函数
async function fetchStatus() {
  try {
    const res = await axios.post('/api/status_heartbeat', {
      room_id: roomId.value
    })
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

// 7. 选中房间后，导航+拉状态
function onSelect(id: number) {
  roomId.value = id
  router.replace({ name: 'RoomAC', params: { id } })
}

// 8. 跳入住页
function goToCheckIn() {
  router.push({ name: 'CheckIn' })
}

// 9. 重新选择
function resetSelect() {
  roomId.value = null
  roomState.value = null
  selectedId.value = null
  router.replace({ name: 'RoomAC' })
}

// 10. 挂载时：先拉全房态，再心跳
onMounted(async () => {
  await store.fetchRooms()
  // 如果路由里有 id，直接心跳
  if (roomId.value) {
    fetchStatus()
    timer = setInterval(fetchStatus, 2000)
  }
})
onBeforeUnmount(() => {
  clearInterval(timer)
})

// 11. 如果用户在路由内手动修改 id（如浏览器地址栏），监听并触发相应流程
watch(
  () => route.params.id,
  newId => {
    const n = newId ? Number(newId) : null
    roomId.value = isNaN(n!) ? null : n
    if (roomId.value) {
      fetchStatus()
      timer = setInterval(fetchStatus, 2000)
    } else {
      clearInterval(timer)
      roomState.value = null
    }
  }
)
</script>

<style scoped>
.client-room-container {
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeIn 0.5s;
}

/* ===== 选房界面 ===== */
.select-room {
  width: 100%;
  max-width: 360px;
  text-align: center;
}

.select-card {
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.select-header {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.select-header .el-icon-house {
  font-size: 24px;
  color: #409eff;
  margin-right: 8px;
}

.select-dropdown {
  width: 100%;
}

.goto-checkin {
  background: linear-gradient(90deg, #409eff, #66b1ff);
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 24px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: background 0.3s;
}

.goto-checkin:hover {
  background: linear-gradient(90deg, #66b1ff, #409eff);
}

.empty-placeholder {
  margin: 40px 0;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  margin-top: 60px;
}

.empty-button {
  margin-top: 16px;
  background: #f56c6c;
  color: #fff;
  border: none;
}

.empty-button:hover {
  background: #dd6161;
}

/* ===== 正常控制卡片 ===== */
.client-room-card {
  width: 100%;
  max-width: 700px;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  background: #fff;
}

.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.room-icon {
  font-size: 28px;
  color: #409eff;
  margin-right: 8px;
}

.room-title {
  font-size: 22px;
  font-weight: bold;
  color: #2c3e50;
}

.room-tag {
  font-size: 14px;
}

/* 通用淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
