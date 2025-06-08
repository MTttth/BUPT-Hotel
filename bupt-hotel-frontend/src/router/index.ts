// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import ControlPanel from '@/views/ControlPanelView.vue'
import CheckIn from '@/views/CheckInView.vue'
import Checkout from '@/views/CheckoutView.vue'
import Monitor from '@/views/MonitorView.vue'
import RoomAC from '@/views/RoomAC.vue'

const routes = [
  {
    path: '/',
    redirect: '/checkin'
  },
  {
    path: '/controlpanel',
    name: 'ControlPanel',
    component: ControlPanel,
  },
  {
    path: '/checkin',
    name: 'CheckIn',
    component: CheckIn,
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: Monitor,
  },
  {
    path: '/room',
    name: 'RoomAC',
    component: RoomAC,
  },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
