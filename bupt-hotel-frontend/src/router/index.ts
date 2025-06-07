// src/router/index.ts
import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import ControlPanel from '@/views/ControlPanelView.vue'
import CheckIn from '@/views/CheckInView.vue'
import Checkout from '@/views/CheckoutView.vue'
import Monitor from '@/views/MonitorView.vue'
import AdminLogin from '@/views/AdminLogin.vue'
import ReceptionLogin from '@/views/ReceptionLogin.vue'
import ClientLogin from '@/views/ClientLogin.vue'
import ManagerLogin from '@/views/ManagerLogin.vue'
import RoomAC from '@/views/RoomAC.vue'

// 定义允许访问的角色类型
export type Role = 'ADMIN' | 'RECEPTION' | 'MANAGER' | 'CLIENT'

// 路由表
const routes = [
  // --------------------
  // 1. 各角色登录页（未登录时可访问）
  // --------------------
  {
    path: '/login/admin',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: { guestOnly: true } // 已登录的角色不允许再访问登录页
  },
  {
    path: '/login/reception',
    name: 'ReceptionLogin',
    component: ReceptionLogin,
    meta: { guestOnly: true }
  },
  {
    path: '/login/client',
    name: 'ClientLogin',
    component: ClientLogin,
    meta: { guestOnly: true }
  },
  {
    path: '/login/manager',
    name: 'ManagerLogin',
    component: ManagerLogin,
    meta: { guestOnly: true }
  },

  // --------------------
  // 2. 受保护的业务页
  // --------------------
  {
    path: '/',
    redirect: '/checkin'
  },
  {
    path: '/controlpanel',
    name: 'ControlPanel',
    component: ControlPanel,
    meta: { requiresRole: ['ADMIN'] as Role[] },
  },
  {
    path: '/checkin',
    name: 'CheckIn',
    component: CheckIn,
    meta: {
      requiresRole: ['RECEPTION'] as Role[], // 只有 RECEPTION 或 ADMIN 可访问
    },
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: {
      requiresRole: ['RECEPTION'] as Role[],
    },
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: Monitor,
    meta: {
      requiresRole: ['MANAGER'] as Role[],
    },
  },
  {
    path: '/room',
    name: 'RoomAC',
    component: RoomAC,
    meta: { requiresRole: ['CLIENT'] as Role[] },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 全局前置守卫
 * 1. 如果目标路由 meta.guestOnly = true，并且当前已登录，直接 redirect 到 “/”(或其他页面)
 * 2. 如果目标路由 meta.requiresRole 定义了一个角色列表，但当前角色不在列表中，则重定向到相应登录页
 * 3. 否则，正常放行
 */
router.beforeEach((to: RouteLocationNormalized, from, next) => {
  const authStore = useAuthStore()
  const currentRole = authStore.role // 可能是 null 或 Role

  // ---------- 1. guestOnly  ------------
  if (to.meta.guestOnly) {
    // 如果当前已经登录，不允许再访问登录页，直接跳到“/”
    // if (currentRole !== null) {
    //   return next({ name: 'ControlPanel' })
    // }
    return next()
  }

  // ---------- 2. requiresRole ------------
  // const requiredRoles = to.meta.requiresRole as Role[] | undefined
  const requiredRoles = [] as Role[]
  if (requiredRoles) {
    // 如果用户未登录或当前角色不在允许列表中，则跳转到对应登录页
    if (!currentRole || !requiredRoles.includes(currentRole)) {
      // 角色不符，需要先跳到哪一个登录页？
      // 优先考虑：如果未登录则先去“ReceptionLogin”让前台登录，
      // 然后管理员或部门专员访问才去对应的登录页。
      // 你也可以根据自己的业务逻辑调整跳转顺序。
      console.log(`当前角色: ${currentRole}, 需要的角色: ${requiredRoles}`)
      if (requiredRoles.includes('ADMIN')) {
        return next({
          name: 'AdminLogin',
          query: { redirect: to.fullPath },
        })
      }
      if (requiredRoles.includes('MANAGER')) {
        return next({
          name: 'ManagerLogin',
          query: { redirect: to.fullPath },
        })
      }
      if (requiredRoles.includes('RECEPTION')) {
        return next({
          name: 'ReceptionLogin',
          query: { redirect: to.fullPath },
        })
      }
      if (requiredRoles.includes('CLIENT')) {
        return next({
          name: 'ClientLogin',
          query: { redirect: to.fullPath },
        })
      }
    }
  }

  // ---------- 3. 放行 ------------
  return next()
})

export default router
