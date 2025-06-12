// src/types/Room.ts

export type Wind = 'slow' | 'mid' | 'high' | 'stop';

/**
 * 后端返回的房间监控详情
 */
export interface RoomDetail {
  /** 房间号 */
  id: number;
  /** 客人姓名，空闲时为 null */
  guest: string | null;
  /** 当前温度 */
  currentTemp: number;
  /** 目标温度 */
  targetTemp: number;
  /** 当前风速 */
  currentSpeed: Wind;
  /** 目标风速 */
  targetSpeed: Wind;
  /** 耗电量（度） */
  electricalUsage: number;
  /** 累计费用（元） */
  cost: number;
  /** 空调开关状态，true=开，false=关 */
  status: boolean;
  /** 房间占用状态，1=占用，0=空闲 */
  roomStatus: number;
}