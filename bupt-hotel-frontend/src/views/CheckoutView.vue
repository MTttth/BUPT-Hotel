<!-- src/views/CheckoutView.vue -->
<template>
  <div class="checkout-container">
    <el-card class="checkout-card" shadow="hover">
      <!-- 标题区 -->
      <div class="checkout-header">
        <i class="el-icon-s-finance checkout-icon"></i>
        <span class="checkout-title">客户结账</span>
      </div>

      <!-- 房间列表 + 操作列 -->
      <el-table v-if="roomDetails.length" :data="roomDetails" stripe border highlight-current-row style="width: 100%;"
        class="checkout-table">
        <el-table-column prop="room_id" label="房间号" width="120" align="center" />
        <el-table-column prop="fee" label="当前费用（￥）" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">￥{{ row.fee.toFixed(2) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button size="mini" type="danger" round @click="confirmCheckout(row.room_id)">
              <i class="el-icon-price-tag"></i> 结账
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无已入住房间" />
      </div>
    </el-card>

    <!-- 结账明细对话框 -->
    <el-dialog title="账单明细" :visible.sync="detailDialogVisible" width="600px" @close="resetDialog">
      <div class="detail-summary">
        <strong>总费用：</strong>￥{{ billTotal.toFixed(2) }}
      </div>
      <el-table :data="billDetails" stripe style="width: 100%;">
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
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

interface RoomDetail {
  room_id: number
  fee: number
}

// 账单明细条目
interface BillItem {
  start_time: string
  end_time: string
  fee: number
}

const roomDetails = ref<RoomDetail[]>([])

// 对话框状态
const detailDialogVisible = ref(false)
const billTotal = ref(0)
const billDetails = ref<BillItem[]>([])

/** 拉取已入住房间的简单列表 */
async function fetchRoomDetails() {
  try {
    const res = await axios.get('/get_room_detail_list')
    if (res.data.code === 200 && Array.isArray(res.data.data.rooms)) {
      // 只保留 roomStatus===1 的入住房间
      roomDetails.value = res.data.data.rooms
        .filter((r: any) => r.room_status === 1)
        .map((r: any) => ({
          room_id: r.room_id,
          fee: r.fee,
        }))
    } else {
      ElMessage.error(res.data.msg || '获取房间列表失败')
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.msg || '网络错误，获取房间列表失败')
  }
}

/** 确认并执行结账 */
function confirmCheckout(roomId: number) {
  ElMessageBox.confirm(
    `确定要给房间 #${roomId} 结账吗？`,
    '结账确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => doCheckout(roomId))
    .catch(() => {
      /* 取消 */
    })
}

/** 调用 /check_out 接口，展示明细 */
async function doCheckout(roomId: number) {
  try {
    const res = await axios.post('/check_out', { room_id: roomId })
    if (res.data.code === 200 && res.data.data) {
      billTotal.value = res.data.data.bill
      billDetails.value = res.data.data.detailed_list
      detailDialogVisible.value = true
      // 拉新列表，移除此房间
      await fetchRoomDetails()
      ElMessage.success('结账成功')
    } else {
      ElMessage.error(res.data.msg || '结账失败')
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.msg || '网络或服务器错误')
  }
}

/** 关闭对话框时重置数据 */
function resetDialog() {
  billTotal.value = 0
  billDetails.value = []
}

// 挂载时拉一次
onMounted(() => {
  fetchRoomDetails()
})
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
  background-color: #fff;
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
