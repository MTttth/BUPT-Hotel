<!-- src/views/ControlPanelView.vue -->
<template>
  <el-row :gutter="16">
    <el-col
      v-for="room in rooms"
      :key="room.roomId"
      :xs="24"
      :sm="12"
      :md="8"
      :lg="6"
    >
      <!-- 直接把 RoomDetail 对象传给 RoomControl -->
      <RoomControl :room="room" />
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoomsStore } from '@/stores/useRoomsStore'
import RoomControl from '@/components/RoomControl.vue'
import type { RoomDetail } from '@/types/Room'

// 1. 获取 Pinia store
const store = useRoomsStore()
const { rooms, fetchRooms } = store

// 2. 组件挂载时拉取一次所有房间状态
onMounted(() => {
  fetchRooms()
})
</script>
