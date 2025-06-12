<!-- src/views/ReportView.vue -->
<template>
	<div class="report-container">
		<el-card class="report-card" shadow="hover">
			<div class="report-header">
				<i class="el-icon-data-analysis report-icon"></i>
				<span class="report-title">时段统计报表</span>
			</div>

			<!-- 时间范围选择 -->
			<div class="report-controls">
				<el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
					end-placeholder="结束日期" align="right" unlink-panels />
				<el-button type="primary" @click="fetchReport" :loading="loading">
					查询
				</el-button>
				<!-- 新增：下载按钮 -->
				<el-button type="success" icon="el-icon-download" @click="downloadCsv" :disabled="!report">
					下载报表
				</el-button>
			</div>

			<!-- 如果有报表数据，则渲染 -->
			<template v-if="report">
				<!-- 整体指标卡片 -->
				<div class="summary-cards">
					<el-card class="summary-card" shadow="hover">
						<div class="card-label">总入住房间数</div>
						<div class="card-value">{{ report.total_rooms }}</div>
					</el-card>
					<el-card class="summary-card" shadow="hover">
						<div class="card-label">总耗电时长</div>
						<div class="card-value">{{ report.total_usage_time }}</div>
					</el-card>
					<el-card class="summary-card" shadow="hover">
						<div class="card-label">总费用（￥）</div>
						<div class="card-value">￥{{ report.total_fee.toFixed(2) }}</div>
					</el-card>
				</div>

				<!-- 房间明细表 -->
				<el-table :data="report.room_summaries" stripe border style="width: 100%; margin-top: 16px;">
					<el-table-column prop="room_id" label="房间号" width="120" />
					<el-table-column prop="usage_time" label="使用时长" />
					<el-table-column prop="total_fee" label="费用 (￥)" width="120">
						<template #default="{ row }">
							￥{{ row.total_fee.toFixed(2) }}
						</template>
					</el-table-column>
				</el-table>
			</template>

			<!-- 没有数据时 -->
			<div v-else-if="!loading" class="empty-state">
				<el-empty description="暂无数据，请选择时间后查询" />
			</div>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 时间范围
const dateRange = ref<[Date, Date] | null>(null)
const loading = ref(false)

// 定义后台 report 结构
interface RoomSummary {
	room_id: number
	usage_time: string
	total_fee: number
}
interface Report {
	total_rooms: number
	total_usage_time: string
	total_fee: number
	room_summaries: RoomSummary[]
}

// 存放获取到的报表
const report = ref<Report | null>(null)

async function fetchReport() {
	if (!dateRange.value) {
		ElMessage.warning('请选择开始和结束日期')
		return
	}
	loading.value = true
	report.value = null

	const [start, end] = dateRange.value
	const startStr = `${start.toISOString().slice(0, 10)} 00:00:00`
	const endStr = `${end.toISOString().slice(0, 10)} 23:59:59`

	try {
		const res = await axios.post('/api/report', {
			start_time: startStr,
			end_time: endStr,
		})
		if (res.data.code === 200 && res.data.data.report) {
			report.value = res.data.data.report
		} else {
			ElMessage.error(res.data.msg || '获取统计失败')
		}
	} catch (err: any) {
		ElMessage.error(err.response?.data?.msg || '网络或服务器错误')
	} finally {
		loading.value = false
	}
}

function downloadCsv() {
	if (!report.value) return

	// 1. 先拼头部 summary
	const lines = []
	lines.push(['总入住房间数', report.value.total_rooms.toString()])
	lines.push(['总耗电时长', report.value.total_usage_time])
	lines.push(['总费用（元）', report.value.total_fee.toFixed(2)])
	lines.push([]) // 空行
	// 2. 再拼表头
	lines.push(['房间号', '使用时长', '总费用(元)'])
	// 3. 然后拼每行
	for (const row of report.value.room_summaries) {
		lines.push([
			row.room_id.toString(),
			row.usage_time,
			row.total_fee.toFixed(2)
		])
	}
	// 4. 转成 CSV 文本
	const csvContent = lines.map(r => r.map(c => `"${c.replace(/"/g, '""')}"`).join(',')).join('\r\n')
	const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
	const url = URL.createObjectURL(blob)

	// 5. 触发下载
	const link = document.createElement('a')
	link.href = url
	link.download = `report_${new Date().toISOString().slice(0, 10)}.csv`
	document.body.appendChild(link)
	link.click()
	document.body.removeChild(link)
	URL.revokeObjectURL(url)
}

</script>

<style scoped>
.report-container {
	padding: 24px;
	background: #f5f7fa;
	min-height: calc(100vh - 64px);
	display: flex;
	justify-content: center;
}

.report-card {
	width: 100%;
	max-width: 900px;
	border-radius: 12px;
	padding: 24px;
}

.report-header {
	display: flex;
	align-items: center;
	margin-bottom: 16px;
}

.report-icon {
	font-size: 28px;
	color: #67c23a;
	margin-right: 8px;
}

.report-title {
	font-size: 20px;
	font-weight: 600;
	color: #303133;
}

.report-controls {
	display: flex;
	gap: 12px;
	align-items: center;
	margin-bottom: 16px;
}

.summary-cards {
	display: flex;
	gap: 16px;
	margin-bottom: 24px;
}

.summary-card {
	flex: 1;
	text-align: center;
}

.card-label {
	color: #606266;
	margin-bottom: 8px;
}

.card-value {
	font-size: 24px;
	font-weight: bold;
	color: #303133;
}

.empty-state {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 40px 0;
}
</style>
