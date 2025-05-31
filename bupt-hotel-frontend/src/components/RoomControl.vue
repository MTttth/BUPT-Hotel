<template>
  <el-card shadow="hover" class="room-card">
    <!-- 卡片头：房间号 + 状态标签 -->
    <template #header>
      <div class="header-container">
        <span class="room-title">房间 #{{ room.id }}</span>
        <el-tag :type="room.occupied ? 'success' : 'info'" size="small">
          {{ room.occupied ? '已入住' : '空闲' }}
        </el-tag>
      </div>
    </template>

    <!-- 如果房间未入住，直接显示空房提示 -->
    <template v-if="!room.occupied">
      <div class="empty-state">
        <el-empty description="空房" />
      </div>
    </template>

    <!-- 房间已入住：展示温控、风速、当前温度、送风状态、累计费用 -->
    <template v-else>
      <!-- 客人信息 + 当前温度 + 送风状态 -->
      <div class="info-row">
        <div class="info-item">
          <i class="el-icon-user"></i>
          <span class="info-text">客人：<b>{{ room.guest }}</b></span>
        </div>
        <div class="info-item">
          <i class="el-icon-temperature"></i>
          <span class="info-text">
            当前温度：<b>{{ room.currentTemp.toFixed(1) }}°C</b>
            <el-tag
              v-if="room.running"
              type="warning"
              size="mini"
              class="ml-4"
            >
              送风中
            </el-tag>
          </span>
        </div>
      </div>

      <!-- 制冷 / 制热 模式切换 -->
      <el-radio-group v-model="localMode" @change="changeMode" class="mode-switch">
        <el-radio-button label="cool">制冷</el-radio-button>
        <el-radio-button label="heat">制热</el-radio-button>
      </el-radio-group>

      <!-- 目标温度滑块 -->
      <div class="slider-row">
        <span class="slider-label">目标温度：</span>
        <el-slider
          v-model="localTemp"
          :min="minTemp"
          :max="maxTemp"
          :step="0.5"
          show-tooltip="never"
          class="temp-slider"
        />
        <span class="slider-value">{{ localTemp }}°C</span>
      </div>

      <!-- 风速按钮组 -->
      <div class="wind-row">
        <span class="wind-label">风速：</span>
        <el-button-group>
          <el-button
            :type="room.wind === 'low' ? 'primary' : 'default'"
            @click="setWind('low')"
          >
            低
          </el-button>
          <el-button
            :type="room.wind === 'medium' ? 'primary' : 'default'"
            @click="setWind('medium')"
          >
            中
          </el-button>
          <el-button
            :type="room.wind === 'high' ? 'primary' : 'default'"
            @click="setWind('high')"
          >
            高
          </el-button>
        </el-button-group>
      </div>

      <!-- 累计费用 显示 -->
      <div class="cost-row">
        <el-statistic
          :value="room.cost.toFixed(2)"
          prefix="￥"
          title="累计费用"
          :precision="2"
        />
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoomsStore } from '@/stores/useRoomsStore';
import type { Room, Wind, Mode } from '@/types/Room';

// 接收父组件传入的 room 对象
const props = defineProps<{ room: Room }>();
const store = useRoomsStore();

// 本地维护一个 temp，用来做防抖：只有当用户停止拖动 1 s 后才真正调用 store.debounceUpdate
const localTemp = ref(props.room.targetTemp);

// 本地维护一个 mode（'cool' 或 'heat'），切换制冷/制热 时要同步 store
const localMode = ref<Mode>(props.room.mode);

// 计算当前模式下可选的最小/最大温度范围
const minTemp = computed(() => (localMode.value === 'cool' ? 18 : 25));
const maxTemp = computed(() => (localMode.value === 'cool' ? 25 : 30));

// watch：当 props.room.targetTemp 发生变化时，即时更新本地值
watch(
  () => props.room.targetTemp,
  (newVal) => {
    localTemp.value = newVal;
  }
);

// watch：当本地 localTemp 改变后，调用防抖函数，1 s 内只有最后一次调用会生效
watch(localTemp, (newVal) => {
  // 如果房间未入住就不执行
  if (!props.room.occupied) return;
  // 调用 Pinia store 中的 debounceUpdate
  store.debounceUpdate(props.room.id, newVal);
});

// 切换风速：直接调用 updateSettings，同步到 Pinia store，立即发起新送风请求
function setWind(w: Wind) {
  if (!props.room.occupied) return;
  store.updateSettings(props.room.id, { wind: w });
}

// 切换制冷 / 制热：修改本地 mode、修改 store 中的 mode，并重置温度为 目标区间内的中点
function changeMode(val: Mode) {
  if (!props.room.occupied) return;
  localMode.value = val;

  // 1. 同步到 Pinia
  store.updateSettings(props.room.id, { mode: val });

  // 2. 切换模式后，默认目标温度设置到中间值
  if (val === 'cool') {
    localTemp.value = 21.5; // 18–25 的中间
    store.debounceUpdate(props.room.id, localTemp.value);
  } else {
    localTemp.value = 27.5; // 25–30 的中间
    store.debounceUpdate(props.room.id, localTemp.value);
  }
}
</script>

<style scoped>
.room-card {
  border-radius: 12px;
  margin: 8px 0;
}

/* 卡片 Header 部分：房间号 + 状态标签 */
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

/* 客人信息 & 当前温度 行 */
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

/* 制冷 / 制热 模式切换 */
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

/* 目标温度 滑块行 */
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

/* 风速按钮行 */
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
/* 将三档按钮宽度统一，视觉更整齐 */
.wind-row .el-button {
  min-width: 50px;
  font-size: 14px;
}

/* 累计费用 显示 */
.cost-row {
  margin-top: 8px;
  text-align: right;
}
</style>
