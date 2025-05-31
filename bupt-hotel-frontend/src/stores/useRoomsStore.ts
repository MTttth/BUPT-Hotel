import { defineStore } from 'pinia';
import { reactive } from 'vue';
import type { Room, Wind, Mode } from '@/types/Room';

const WIND_RATE: Record<Wind, number> = { high: 1, medium: 0.5, low: 1 / 3 };
const TEMP_RATE: Record<Wind, number> = {
  high: 0.6, // +20 %
  medium: 0.5,
  low: 0.4, // –20 %
};

export const useRoomsStore = defineStore('rooms', () => {
  const rooms = reactive<Room[]>([]);

  /* 初始化房间：在第一次使用 store 时自动执行一次。 */
  function initRooms(count = 20) {
    if (rooms.length) return; // 避免重复
    for (let i = 1; i <= count; i++) {
      rooms.push({
        id: i,
        occupied: false,
        guest: null,
        mode: 'cool',
        targetTemp: 25,
        wind: 'medium',
        currentTemp: 25,
        cost: 0,
        running: false,
      });
    }
  }

  function checkIn(id: number, guest: string) {
    const r = rooms.find(r => r.id === id);
    if (!r || r.occupied) throw new Error('房间已占用!');
    Object.assign(r, {
      occupied: true,
      guest,
      mode: 'cool',
      targetTemp: 25,
      wind: 'medium',
      currentTemp: 25,
      cost: 0,
      running: false,
    });
  }

  function checkout(id: number) {
    const r = rooms.find(r => r.id === id);
    if (!r?.occupied) throw new Error('房间为空');
    const bill = { room: id, guest: r.guest!, cost: r.cost.toFixed(2) };
    Object.assign(r, {
      occupied: false,
      guest: null,
      cost: 0,
      running: false,
    });
    return bill;
  }

  function updateSettings(id: number, payload: Partial<Pick<Room, 'targetTemp' | 'wind' | 'mode'>>) {
    const r = rooms.find(r => r.id === id);
    if (!r) return;
    if (payload.targetTemp !== undefined) r.targetTemp = payload.targetTemp;
    if (payload.wind) r.wind = payload.wind;
    if (payload.mode) r.mode = payload.mode;
    // 切风速视为新送风请求
    if (!r.running) r.running = true;
  }

  // 防抖逻辑：2 次调温 <1 s 只保留最后一次
  let debounceMap: Record<number, ReturnType<typeof setTimeout> | null> = {};
  function debounceUpdate(id: number, temp: number) {
    if (debounceMap[id]) clearTimeout(debounceMap[id]!);
    debounceMap[id] = setTimeout(() => {
      updateSettings(id, { targetTemp: temp });
      debounceMap[id] = null;
    }, 1000);
  }

  /* 简易定时器模拟温度变化与计费，每秒=1分钟 (demo) */
  setInterval(() => {
    rooms.forEach(r => {
      if (!r.occupied) return;
      if (!r.running) {
        // 关机自然回温/降温 0.5 °C / min
        if (r.mode === 'cool' && r.currentTemp < 29) r.currentTemp += 0.5;
        if (r.mode === 'heat' && r.currentTemp > 21) r.currentTemp -= 0.5;
        return;
      }
      const dir = r.mode === 'cool' ? -1 : 1;
      r.currentTemp += dir * TEMP_RATE[r.wind];
      r.cost += WIND_RATE[r.wind];
      const reached = dir < 0 ? r.currentTemp <= r.targetTemp : r.currentTemp >= r.targetTemp;
      if (reached) r.running = false; // 达标后停送风
    });
  }, 60000); // 60 秒

  // 初始化 20 个房间 (一次)
  initRooms();

  return { rooms, checkIn, checkout, updateSettings, debounceUpdate };
});