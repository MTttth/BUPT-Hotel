<!-- src/components/RoomControl.vue -->
<template>
  <el-card shadow="hover" class="room-card">
    <!-- 卡片头：房间号 + 开关按钮 -->
    <template #header>
      <div class="header-container">
        <div>
          <span class="room-title">房间 #{{ room.id }}</span>
          <el-tag :type="room.roomStatus === 1 ? 'success' : 'info'" size="small">
            {{ room.roomStatus === 1 ? '已入住' : '空闲' }}
          </el-tag>
        </div>
        <el-button size="mini" :type="room.status ? 'danger' : 'primary'" @click="togglePower">
          {{ room.status ? '关闭空调' : '启动空调' }}
        </el-button>
      </div>
    </template>

    <!-- 空房 -->
    <template v-if="room.roomStatus === 0">
      <div class="empty-state">
        <el-empty description="空房" />
      </div>
    </template>

    <!-- 已入住 -->
    <template v-else>
      <!-- 客人 & 当前温度 & 当前风速 -->
      <div class="info-row">
        <div class="info-item">
          <i class="el-icon-user"></i>
          <span class="info-text">客人：<b>{{ room.guest }}</b></span>
        </div>
        <div class="info-item">
          <i class="el-icon-temperature"></i>
          <span class="info-text">
            当前温度：<b>{{ room.currentTemp.toFixed(1) }}°C</b>
          </span>
        </div>
        <div class="info-item">
          <i class="el-icon-c-scale-to-original"></i>
          <span class="info-text">
            当前风速：<b>{{ room.currentSpeed }}</b>
          </span>
        </div>
      </div>

      <!-- 目标温度滑块（只有开机时可用） -->
      <div class="slider-row">
        <span class="slider-label">目标温度：</span>
        <el-slider v-model="localTemp" :min="18" :max="30" :step="0.5" show-tooltip="never" class="temp-slider"
          :disabled="!room.status" />
        <span class="slider-value">{{ localTemp }}°C</span>
      </div>

      <!-- 风速按钮组（只有开机时可用） -->
      <div class="wind-row">
        <span class="wind-label">目标风速：</span>
        <el-button-group>
          <el-button :type="room.currentSpeed === 'slow' ? 'primary' : 'default'" @click="setWind('slow')"
            :disabled="!room.status">低</el-button>
          <el-button :type="room.currentSpeed === 'mid' ? 'primary' : 'default'" @click="setWind('mid')"
            :disabled="!room.status">中</el-button>
          <el-button :type="room.currentSpeed === 'high' ? 'primary' : 'default'" @click="setWind('high')"
            :disabled="!room.status">高</el-button>
        </el-button-group>
      </div>

      <!-- 累计费用 -->
      <div class="cost-row">
        <el-statistic :value="room.cost.toFixed(2)" prefix="￥" title="累计费用" :precision="2" />
      </div>
    </template>
  </el-card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { RoomDetail, Wind } from '@/types/Room'

export default defineComponent({
  name: 'RoomControl',
  props: {
    room: {
      type: Object as () => RoomDetail,
      required: true
    }
  },
  data() {
    return {
      // 本地防抖用的目标温度
      localTemp: this.room.targetTemp as number,
      debounceTimer: null as ReturnType<typeof setTimeout> | null
    }
  },
  watch: {
    // 父组件更新 room.targetTemp，要同步 localTemp
    'room.targetTemp'(newVal: number) {
      this.localTemp = newVal
    },
    // localTemp 变动时才发温度调整请求
    localTemp(this: any, v: number) {
      if (!this.room.status) return
      if (this.debounceTimer) clearTimeout(this.debounceTimer)
      this.debounceTimer = setTimeout(async () => {
        try {
          await axios.post('/api/adjust_temperature', {
            room_id: this.room.id,
            target_temp: v
          })
          ElMessage.success('温度请求已发送')
        } catch (err: any) {
          ElMessage.error(err.response?.data?.msg || '温度设置失败')
        }
        this.debounceTimer = null
      }, 1000)
    }
  },
  methods: {
    // 风速设置
    async setWind(speed: Wind) {
      if (!this.room.status) return
      try {
        await axios.post('/api/adjust_wind', {
          room_id: this.room.id,
          target_speed: speed
        })
        ElMessage.success('风速请求已发送')
      } catch (err: any) {
        ElMessage.error(err.response?.data?.msg || '风速设置失败')
      }
    },

    // 开 / 关 空调
    async togglePower() {
      try {
        if (this.room.status) {
          // 当前是开机，发关机请求
          await axios.post('/api/power_off', { room_id: this.room.id })
          ElMessage.success('空调已关闭')
          this.room.status = false
        } else {
          // 当前是关机，发开机请求
          await axios.post('/api/power_on', {
            room_id: this.room.id,
            target_temp: this.room.targetTemp,
            target_speed: this.room.currentSpeed
          })
          ElMessage.success('空调已开启')
          this.room.status = true
        }
      } catch (err: any) {
        ElMessage.error(err.response?.data?.msg || '切换开关失败')
      }
    }
  }
})
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
