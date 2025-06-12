<!-- src/views/CheckoutView.vue -->
<template>
  <div class="checkout-container">
    <el-card class="checkout-card" shadow="hover">
      <!-- 标题 -->
      <div class="checkout-header">
        <i class="el-icon-s-finance checkout-icon"></i>
        <span class="checkout-title">客户结账</span>
      </div>

      <!-- 入住房间列表 -->
      <el-table v-if="roomDetails.length" :data="roomDetails" stripe border highlight-current-row
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
              <i class="el-icon-price-tag"></i>
              结账
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-state">
        <el-empty description="暂无已入住房间" />
      </div>
    </el-card>

    <!-- 账单明细弹窗 -->
    <el-dialog v-model="detailDialogVisible" custom-class="detail-dialog" width="600px" @close="resetDialog"
      append-to-body destroy-on-close>
      <template #title>
        <div class="dialog-title">
          <i class="el-icon-document"></i>
          <span>账单明细</span>
        </div>
      </template>

      <!-- 总费用 -->
      <div class="detail-summary">
        <span class="label">总费用：</span>
        <span class="value">￥{{ billTotal.toFixed(2) }}</span>
      </div>

      <!-- 详细列表 -->
      <el-table v-if="billDetails.length" :data="formattedDetails" stripe border class="detail-table">
        <el-table-column prop="start" label="开始时间" width="180" />
        <el-table-column prop="end" label="结束时间" width="180" />
        <el-table-column prop="duration" label="时长" width="100" align="center" />
        <el-table-column prop="fee" label="费用 (￥)" width="100" align="right">
          <template #default="{ row }">
            ￥{{ row.fee.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="no-details">
        <el-empty description="暂无明细记录" />
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false" type="primary">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'
import { ElMessage, ElMessageBox } from 'element-plus'
dayjs.extend(duration)

interface RoomDetail {
  room_id: number
  fee: number
}

interface BillItem {
  start_time: string
  end_time: string
  fee: number
}

const roomDetails = ref<RoomDetail[]>([])

const detailDialogVisible = ref(false)
const billTotal = ref(0)
const billDetails = ref<BillItem[]>([])

// 风格小动画：开窗时自动滚顶
onMounted(() => {
  const dialog = document.querySelector('.detail-dialog')
  if (dialog) dialog.scrollTop = 0
})

async function fetchRoomDetails() {
  try {
    const res = await axios.get('/api/get_room_detail_list')
    if (res.data.code === 200 && Array.isArray(res.data.data.rooms)) {
      roomDetails.value = res.data.data.rooms
        .filter((r: any) => r.room_status === 1)
        .map((r: any) => ({
          room_id: r.room_id,
          fee: r.fee,
        }))
    }
  } catch { }
}

function confirmCheckout(roomId: number) {
  ElMessageBox.confirm(
    `确定要给房间 #${roomId} 结账吗？`,
    '结账确认',
    { type: 'warning' }
  ).then(() => doCheckout(roomId))
}

async function doCheckout(roomId: number) {
  try {
    const res = await axios.post('/api/check_out', { room_id: roomId })
    if (res.data.code === 200) {
      billTotal.value = res.data.data.bill
      billDetails.value = res.data.data.detailed_list
      detailDialogVisible.value = true
      fetchRoomDetails()
      ElMessage.success('结账成功')
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch {
    ElMessage.error('网络或服务器错误')
  }
}

function resetDialog() {
  billTotal.value = 0
  billDetails.value = []
}

// **格式化**：把原始时间 & 计算时长
const formattedDetails = computed(() =>
  billDetails.value.map(item => {
    const start = dayjs(item.start_time)
    const end = dayjs(item.end_time)
    const diff = dayjs.duration(end.diff(start))
    const h = diff.hours().toString().padStart(2, '0')
    const m = diff.minutes().toString().padStart(2, '0')
    const s = diff.seconds().toString().padStart(2, '0')
    return {
      start: start.format('MM-DD HH:mm:ss'),
      end: end.isValid() ? end.format('MM-DD HH:mm:ss') : '—',
      duration: end.isValid() ? `${h}:${m}:${s}` : '—',
      fee: item.fee
    }
  })
)

onMounted(fetchRoomDetails)
</script>

<style scoped>
.checkout-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.checkout-card {
  width: 100%;
  max-width: 860px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
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
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

/* 主表格 */
.checkout-table .el-table__header th {
  background: #fafafa;
  color: #606266;
  font-weight: 500;
}

.checkout-table .el-table__body tr:hover {
  background: #fbf6ee;
}

/* 弹窗自定义类 + 动画 */
.detail-dialog .el-dialog {
  animation: zoomIn .3s ease-out;
}

@keyframes zoomIn {
  from {
    transform: translateY(20px) scale(0.98);
    opacity: 0
  }

  to {
    transform: translateY(0) scale(1);
    opacity: 1
  }
}

.dialog-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  color: #303133;
}

.dialog-title .el-icon-document {
  margin-right: 8px;
  color: #409eff;
}

.detail-summary {
  margin: 16px 0;
  font-size: 16px;
}

.detail-table .el-table__body tr:hover {
  background: #f0f9eb;
}

.no-details {
  text-align: center;
  padding: 40px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
