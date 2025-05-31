<!-- src/views/CheckoutView.vue -->
<template>
  <div class="checkout-container">
    <el-card class="checkout-card" shadow="hover">
      <!-- 标题区 -->
      <div class="checkout-header">
        <i class="el-icon-s-finance checkout-icon"></i>
        <span class="checkout-title">客户结账</span>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="occupiedRooms"
        stripe
        border
        highlight-current-row
        style="width: 100%;"
        class="checkout-table"
      >
        <el-table-column
          prop="id"
          label="房间号"
          width="120"
          align="center"
        />
        <el-table-column
          prop="guest"
          label="客人"
          align="center"
        />
        <el-table-column
          prop="cost"
          label="费用（￥）"
          align="center"
        >
          <template #default="{ row }">
            <el-tag type="warning" size="small">￥{{ Number(row.cost).toFixed(2) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button
              size="mini"
              type="danger"
              round
              @click="confirmCheckout(row)"
            >
              <i class="el-icon-price-tag"></i> 结账
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 如果没有已入住的房间，显示空状态 -->
      <div v-if="occupiedRooms.length === 0" class="empty-state">
        <el-empty description="暂无已入住房间" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoomsStore } from '@/stores/useRoomsStore';
import type { Room } from '@/types/Room';

const store = useRoomsStore();
const occupiedRooms = computed<Room[]>(() => store.rooms.filter(r => r.occupied));

/**
 * 弹出确认框后执行结账
 */
function confirmCheckout(room: Room) {
  ElMessageBox.confirm(
    `确定要给房间 #${room.id} 的客人 “${room.guest}” 结账吗？`,
    '结账确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    doCheckout(room.id);
  }).catch(() => {
    /* 用户取消 */
  });
}

/**
 * 真正执行结账流程
 */
function doCheckout(id: number) {
  try {
    const bill = store.checkout(id);
    ElMessage({
      type: 'success',
      message: `房间 #${bill.room} 的客人 ${bill.guest} 已结账，费用 ￥${bill.cost}`,
      center: true,
      duration: 3000,
    });
  } catch (error: any) {
    ElMessage({
      type: 'error',
      message: error.message || '结账失败',
      center: true,
      duration: 3000,
    });
  }
}
</script>

<style scoped>
.checkout-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.checkout-card {
  width: 100%;
  max-width: 800px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: #ffffff;
  animation: fadeIn 0.4s ease-out;
  padding: 24px;
}

.checkout-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.checkout-icon {
  font-size: 28px;
  color: #f56c6c;
  margin-right: 8px;
}

.checkout-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  user-select: none;
}

.checkout-table .el-table__header th {
  background-color: #fafafa;
  color: #606266;
  font-weight: 500;
}

.checkout-table .el-table__body tr:hover {
  background-color: #fbf6ee;
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
