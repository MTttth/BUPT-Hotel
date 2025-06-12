<!-- src/views/MonitorView.vue -->
<template>
  <div class="monitor-container">
    <el-card class="monitor-card" shadow="hover">
      <!-- 标题区域 -->
      <div class="monitor-header">
        <i class="el-icon-monitor monitor-icon"></i>
        <span class="monitor-title">空调监控总览</span>
      </div>

      <!-- 表格区域 -->
      <el-table v-if="rooms.length" :data="rooms" stripe border highlight-current-row style="width: 100%;"
        class="monitor-table">
        <el-table-column prop="id" label="房间号" width="100" align="center" />

        <el-table-column prop="roomStatus" label="房间状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.roomStatus === 1 ? 'danger' : 'success'" size="mini">
              {{ row.roomStatus === 1 ? '已入住' : '空闲' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="guest" label="客人" align="center">
          <template #default="{ row }">
            <span v-if="row.guest">{{ row.guest }}</span>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <el-table-column prop="currentTemp" label="当前温度 (℃)" width="140" align="center">
          <template #default="{ row }">
            {{ row.currentTemp.toFixed(1) }}
          </template>
        </el-table-column>

        <el-table-column prop="targetTemp" label="目标温度 (℃)" width="140" align="center">
          <template #default="{ row }">
            {{ row.targetTemp.toFixed(1) }}
          </template>
        </el-table-column>

        <el-table-column prop="currentSpeed" label="当前风速" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.currentSpeed === 'stop'" type="info" size="mini">停止</el-tag>
            <el-tag v-else-if="row.currentSpeed === 'low'" type="info" size="mini">低风</el-tag>
            <el-tag v-else-if="row.currentSpeed === 'medium'" type="primary" size="mini">中风</el-tag>
            <el-tag v-else type="success" size="mini">高风</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="targetSpeed" label="目标风速" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.targetSpeed === 'stop'" type="info" size="mini">停止</el-tag>
            <el-tag v-else-if="row.targetSpeed === 'low'" type="info" size="mini">低风</el-tag>
            <el-tag v-else-if="row.targetSpeed === 'medium'" type="primary" size="mini">中风</el-tag>
            <el-tag v-else type="success" size="mini">高风</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="空调开关" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-text="已开启" inactive-text="已关闭" disabled />
          </template>
        </el-table-column>

        <el-table-column prop="electricalUsage" label="耗电 (度)" width="120" align="center">
          <template #default="{ row }">
            {{ row.electricalUsage.toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column prop="cost" label="累计费用 (￥)" width="140" align="center">
          <template #default="{ row }">
            ￥{{ row.cost.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态提示 -->
      <div v-else class="empty-state">
        <el-empty description="暂无房间数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

interface ApiRoom {
  room_id: number;
  guest?: string | null;
  current_temp: number;
  target_temp: number;
  current_speed: 'low' | 'medium' | 'high' | 'stop';
  target_speed: 'low' | 'medium' | 'high' | 'stop';
  electrical_usage: number;
  fee: number;
  status: number;       // 1=开机 0=关机
  room_status: number;  // 1=占用 0=空闲
}

interface Room {
  id: number;
  guest: string;
  currentTemp: number;
  targetTemp: number;
  currentSpeed: 'low' | 'medium' | 'high' | 'stop';
  targetSpeed: 'low' | 'medium' | 'high' | 'stop';
  electricalUsage: number;
  cost: number;
  status: boolean;
  roomStatus: number;
}

const rooms = ref<Room[]>([]);
let timer: ReturnType<typeof setInterval>;

async function fetchRoomDetails() {
  try {
    const res = await axios.get('/api/get_room_detail_list');
    if (res.data.code === 200 && Array.isArray(res.data.data.rooms)) {
      rooms.value = res.data.data.rooms
        .filter((r: ApiRoom) => r.room_status === 1 || r.room_status === 0)
        .map((r: ApiRoom): Room => ({
          id: r.room_id,
          guest: r.guest || '',
          currentTemp: r.current_temp,
          targetTemp: r.target_temp,
          currentSpeed: r.current_speed,
          targetSpeed: r.target_speed,
          electricalUsage: r.electrical_usage,
          cost: r.fee,
          status: r.status === 1,
          roomStatus: r.room_status,
        }));
    } else {
      ElMessage.error(res.data.msg || '获取房间列表失败');
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.msg || '网络错误，获取房间列表失败');
  }
}

onMounted(() => {
  fetchRoomDetails();
  timer = setInterval(fetchRoomDetails, 2_000);
});

onBeforeUnmount(() => {
  clearInterval(timer);
});
</script>

<style scoped>
.monitor-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.monitor-card {
  width: 100%;
  max-width: 1000px;
  border-radius: 12px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 24px;
  animation: fadeIn 0.4s ease-out;
}

.monitor-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.monitor-icon {
  font-size: 28px;
  color: #409eff;
  margin-right: 8px;
}

.monitor-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.monitor-table .el-table__header th {
  background-color: #fafafa;
  color: #606266;
  font-weight: 500;
}

.monitor-table .el-table__body tr:hover {
  background-color: #e3f2fd;
}

.empty-state {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.temp-text {
  font-weight: 500;
  color: #409eff;
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
