<template>
  <el-card shadow="hover" class="room-card">
    <template #header>
      <div class="header-container">
        <span class="room-title">房间 #{{ room.roomId }}</span>
        <el-tag :type="room.roomStatus === 0 ? 'success' : 'info'" size="small">
          {{ room.roomStatus === 1 ? '已入住' : '空闲' }}
        </el-tag>
      </div>
    </template>

    <template v-if="room.roomStatus === 0">
      <div class="empty-state">
        <el-empty description="空房" />
      </div>
    </template>

    <template v-else>
      <div class="info-row">
        <div class="info-item">
          <i class="el-icon-user"></i>
          <span class="info-text">客人：<b>{{ room.guest }}</b></span>
        </div>
        <div class="info-item">
          <i class="el-icon-temperature"></i>
          <span class="info-text">
            当前温度：<b>{{ room.currentTemp.toFixed(1) }}°C</b>
            <el-tag v-if="room.status" type="warning" size="mini" class="ml-4">
              送风中
            </el-tag>
          </span>
        </div>
      </div>

      <div class="slider-row">
        <span class="slider-label">目标温度：</span>
        <el-slider v-model="localTemp" :min="minTemp" :max="maxTemp" :step="0.5" show-tooltip="never"
          class="temp-slider" />
        <span class="slider-value">{{ localTemp }}°C</span>
      </div>

      <div class="wind-row">
        <span class="wind-label">风速：</span>
        <el-button-group>
          <el-button :type="room.currentSpeed === 'slow' ? 'primary' : 'default'" @click="setWind('slow')">低</el-button>
          <el-button :type="room.currentSpeed === 'mid' ? 'primary' : 'default'" @click="setWind('mid')">中</el-button>
          <el-button :type="room.currentSpeed === 'high' ? 'primary' : 'default'" @click="setWind('high')">高</el-button>
        </el-button-group>
      </div>

      <div class="cost-row">
        <el-statistic :value="room.cost.toFixed(2)" prefix="￥" title="累计费用" :precision="2" />
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, ref, computed, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { RoomDetail, Wind } from '@/types/Room'

// 接收父组件传入的 room 对象
const props = defineProps<{ room: RoomDetail }>()
const { room } = props

// 本地状态
const localTemp = ref(room.targetTemp)
const minTemp = computed(() => (18))
const maxTemp = computed(() => (30))

// 监听外部更新
watch(() => room.targetTemp, v => (localTemp.value = v))

let debounceTimer: ReturnType<typeof setTimeout> | null = null
// 温度修改
watch(localTemp, v => {
  if (room.roomStatus === 0) return
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(async () => {
    try {
      if (room.status === false) {
        await axios.post('/power_on', {
          room_id: room.roomId,
          target_temp: v,
          target_speed: room.currentSpeed
        })
        ElMessage.success('空调已开启')
      } else {
        await axios.post('/adjust_temperature', { room_id: room.roomId, target_temp: v })
        ElMessage.success('温度请求已发送')
      }
    } catch (e: any) {
      ElMessage.error(e.response?.data?.msg || '温度设置失败')
    }
    debounceTimer = null
  }, 1000)
})

// 风速修改
async function setWind(speed: Wind) {
  if (room.roomStatus === 0) return
  try {
    if (room.status === false) {
      await axios.post('/power_on', {
        room_id: room.roomId,
        target_temp: room.targetTemp,
        target_speed: speed
      })
      ElMessage.success('空调已开启')
    } else {
      await axios.post('/adjust_wind', { room_id: room.roomId, target_speed: speed })
      ElMessage.success('风速请求已发送')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.msg || '风速设置失败')
  }
}

</script>

<style scoped>
.room-card {
  border-radius: 12px;
  margin: 8px 0;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-title {
  font-size: 18px;
  font-weight: 600;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 220px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .el-icon-user,
.info-item .el-icon-temperature {
  margin-right: 6px;
  color: #409eff;
}

.info-text {
  font-size: 14px;
}

.mode-switch {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

.mode-switch .el-radio-button {
  width: 80px;
  text-align: center;
  font-size: 14px;
}

.slider-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.slider-label {
  width: 70px;
  font-size: 14px;
}

.temp-slider {
  flex: 1;
}

.slider-value {
  width: 50px;
  text-align: right;
  font-weight: 500;
  margin-left: 8px;
  font-size: 14px;
}

.wind-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.wind-label {
  width: 70px;
  font-size: 14px;
}

.wind-row .el-button-group {
  margin-left: 6px;
}

.wind-row .el-button {
  min-width: 50px;
  font-size: 14px;
}

.cost-row {
  margin-top: 8px;
  text-align: right;
}
</style>
