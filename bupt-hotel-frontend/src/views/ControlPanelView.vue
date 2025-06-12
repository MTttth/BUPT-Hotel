<template>
  <el-row :gutter="16">
    <el-col
      v-for="room in store.rooms"
      :key="room.id"
      :xs="24"
      :sm="12"
      :md="8"
      :lg="6"
    >
      <RoomControl :room="room" />
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoomsStore } from '@/stores/useRoomsStore'
import RoomControl from '@/components/RoomControl.vue'

// 拿到整个 store
const store = useRoomsStore()

let timer: ReturnType<typeof setInterval>

onMounted(() => {
  store.fetchRooms()                   // 立刻拉一次
  timer = setInterval(store.fetchRooms, 2000) // 每秒刷新
})

onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>
