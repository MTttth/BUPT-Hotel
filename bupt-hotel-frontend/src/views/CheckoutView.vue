<!-- src/views/CheckoutView.vue -->
<template>
  <div class="checkout-container">
    <el-card class="checkout-card" shadow="hover">
      <!-- 标题区 -->
      <div class="checkout-header">
        <i class="el-icon-s-finance checkout-icon"></i>
        <span class="checkout-title">客户结账</span>
      </div>

      <!-- 入住房间列表 -->
      <el-table :data="occupiedRooms" stripe border highlight-current-row style="width: 100%;" class="checkout-table">
        <el-table-column prop="id" label="房间号" width="120" align="center" />
        <el-table-column prop="guest" label="客人" align="center" />
        <el-table-column prop="cost" label="当前费用（￥）" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">
              ￥{{ Number(row.cost).toFixed(2) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button size="mini" type="danger" round @click="confirmCheckout(row)">
              <i class="el-icon-price-tag"></i> 结账
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="occupiedRooms.length === 0" class="empty-state">
        <el-empty description="暂无已入住房间" />
      </div>
    </el-card>

    <!-- 账单明细对话框 -->
    <el-dialog title="账单明细" :visible.sync="detailDialogVisible" width="600px" @close="resetDialog">
      <div class="detail-summary">
        <strong>总费用：</strong>￥{{ billAmount.toFixed(2) }}
      </div>
      <el-table :data="detailedList" stripe style="width: 100%;">
        <el-table-column prop="start_time" label="开始时间" width="180" />
        <el-table-column prop="end_time" label="结束时间" width="180" />
        <el-table-column prop="fee" label="费用 (￥)" width="100" align="right" />
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoomsStore } from '@/stores/useRoomsStore';
import type { Room } from '@/types/Room';

const store = useRoomsStore();
const occupiedRooms = computed<Room[]>(() => store.rooms.filter(r => r.occupied));

// Dialog state
const detailDialogVisible = ref(false);
const billAmount = ref(0);
const detailedList = ref<Array<{ start_time: string; end_time: string; fee: number }>>([]);

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
  )
    .then(() => {
      doCheckout(room.id);
    })
    .catch(() => {
      // 用户取消
    });
}

/**
 * 真正执行结账流程：调用后端 POST /check_out
 */
async function doCheckout(id: number) {
  try {
    const res = await axios.post('/check_out', { room_id: id });
    if (res.data.code === 200 && res.data.data) {
      // 后端返回账单明细
      billAmount.value = res.data.data.bill;
      detailedList.value = res.data.data.detailed_list;
      // 更新本地状态
      store.checkout(id);
      // 打开明细对话框
      detailDialogVisible.value = true;
      ElMessage.success('结账成功');
    } else {
      ElMessage.error(res.data.msg || '结账失败');
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.msg || '网络或服务器错误');
  }
}

/**
 * 关闭对话框时重置数据
 */
function resetDialog() {
  billAmount.value = 0;
  detailedList.value = [];
}
</script>

<style scoped>
.checkout-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  align-items: center;
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
}

.detail-summary {
  margin-bottom: 16px;
  font-size: 16px;
}

.dialog-footer {
  text-align: right;
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
