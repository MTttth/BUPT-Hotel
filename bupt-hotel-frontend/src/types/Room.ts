export type Wind = 'low' | 'medium' | 'high';
export type Mode = 'cool' | 'heat';

export interface Room {
  id: number;
  occupied: boolean;
  guest: string | null;
  mode: Mode;
  targetTemp: number;
  wind: Wind;
  currentTemp: number;
  cost: number;
  running: boolean;
}