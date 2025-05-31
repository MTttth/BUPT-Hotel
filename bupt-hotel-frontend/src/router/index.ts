import { createRouter, createWebHistory } from 'vue-router';
import ControlPanel from '@/views/ControlPanelView.vue';
import CheckIn from '@/views/CheckInView.vue';
import Checkout from '@/views/CheckoutView.vue';
import Monitor from '@/views/MonitorView.vue';

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: ControlPanel },
    { path: '/checkin', component: CheckIn },
    { path: '/checkout', component: Checkout },
    { path: '/monitor', component: Monitor },
  ],
});