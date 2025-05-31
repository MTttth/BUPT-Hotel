<template>
  <el-row :gutter="16">
    <el-col
      v-for="room in rooms"
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
import { useRoomsStore } from '@/stores/useRoomsStore';
import RoomControl from '@/components/RoomControl.vue';

const store = useRoomsStore();
const { rooms } = store;
</script>
