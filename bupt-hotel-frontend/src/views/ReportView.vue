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
			</div>

			<!-- 结果展示：可以用表格或图表 -->
			<el-table v-if="reportData.length" :data="reportData" stripe border style="width: 100%; margin-top: 16px;">
				<el-table-column prop="roomCount" label="入住房间数" width="140" />
				<el-table-column prop="totalFee" label="总费用(￥)" />
				<el-table-column prop="avgTemp" label="平均温度(℃)" />
				<el-table-column prop="totalUsage" label="总耗电(度)" />
			</el-table>

			<div v-else-if="!loading" class="empty-state" style="margin-top: 40px;">
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

// 接口返回的数据结构
interface ReportRow {
	roomCount: number
	totalFee: number
	avgTemp: number
	totalUsage: number
}

const reportData = ref<ReportRow[]>([])

/** 拉取指定时间段的统计报表 */
async function fetchReport() {
	if (!dateRange.value) {
		ElMessage.warning('请选择开始和结束日期')
		return
	}
	loading.value = true
	reportData.value = []

	const [start, end] = dateRange.value
	try {
		const res = await axios.get('api/get_statistics', {
			params: {
				start_time: start.toISOString().slice(0, 10),
				end_time: end.toISOString().slice(0, 10)
			}
		})
		if (res.data.code === 200 && Array.isArray(res.data.data.stats)) {
			// 假设后端返回 data.stats: Array<{ roomCount,totalFee,avgTemp,totalUsage }>
			reportData.value = res.data.data.stats
		} else {
			ElMessage.error(res.data.msg || '获取统计失败')
		}
	} catch (err: any) {
		ElMessage.error(err.response?.data?.msg || '网络或服务器错误')
	} finally {
		loading.value = false
	}
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

.empty-state {
	display: flex;
	justify-content: center;
	align-items: center;
}
</style>
