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
      <el-table
        v-if="rooms && rooms.length"
        :data="rooms"
        stripe
        border
        highlight-current-row
        style="width: 100%;"
        class="monitor-table"
      >
        <el-table-column
          prop="id"
          label="房间号"
          width="100"
          align="center"
        />
        <el-table-column
          prop="guest"
          label="客人"
          align="center"
        >
          <template #default="{ row }">
            <span v-if="row.guest">{{ row.guest }}</span>
            <span v-else>
              <el-tag type="info" size="mini">空闲</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="currentTemp"
          label="当前温度 (℃)"
          width="140"
          align="center"
        >
          <template #default="{ row }">
            <span class="temp-text">{{ row.currentTemp.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="targetTemp"
          label="目标温度 (℃)"
          width="140"
          align="center"
        >
          <template #default="{ row }">
            <span class="temp-text">{{ row.targetTemp }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="wind"
          label="风速"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.wind === 'low'"
              type="info"
              size="mini"
            >
              低风
            </el-tag>
            <el-tag
              v-else-if="row.wind === 'medium'"
              type="primary"
              size="mini"
            >
              中风
            </el-tag>
            <el-tag
              v-else
              type="success"
              size="mini"
            >
              高风
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="cost"
          label="累计费用 (￥)"
          width="140"
          align="center"
        >
          <template #default="{ row }">
            <el-tag type="warning" size="mini">￥{{ row.cost.toFixed(2) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="running"
          label="送风中"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.running"
              type="warning"
              size="mini"
            >
              是
            </el-tag>
            <el-tag
              v-else
              type="success"
              size="mini"
            >
              否
            </el-tag>
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
import { useRoomsStore } from '@/stores/useRoomsStore';
const { rooms } = useRoomsStore();
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
  max-width: 900px;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.4s ease-out;
  padding: 24px;
}

.monitor-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.monitor-icon {
  font-size: 28px;
  color: #67c23a;
  margin-right: 8px;
}

.monitor-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  user-select: none;
}

.monitor-table .el-table__header th {
  background-color: #fafafa;
  color: #606266;
  font-weight: 500;
}

.monitor-table .el-table__body tr:hover {
  background-color: #f0f9eb;
}

.temp-text {
  font-weight: 500;
  color: #409eff;
}

.empty-state {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
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
