import { defineStore } from 'pinia'
import { ref, onMounted } from 'vue'
import axios from 'axios'
import type { RoomDetail } from '@/types/Room'

export const useRoomsStore = defineStore('rooms', () => {
  const rooms = ref<RoomDetail[]>([])

  async function fetchRooms() {
    try {
      const res = await axios.get('api/get_room_detail_list')
      if (res.data.code === 200 && Array.isArray(res.data.data.rooms)) {
        rooms.value = res.data.data.rooms.map((r: any) => ({
          id: r.room_id,
          guest: r.guest || null,
          currentTemp: r.current_temp,
          targetTemp: r.target_temp,
          currentSpeed: r.current_speed,
          targetSpeed: r.target_speed,
          electricalUsage: r.electrical_usage,
          cost: r.fee,
          status: r.status,
          roomStatus: r.room_status,
        }))
      }
    } catch (e) {
      console.error(e)
    }
  }

  onMounted(() => {
    fetchRooms()
    setInterval(fetchRooms, 1_000)
  })

  return { rooms, fetchRooms }
})
