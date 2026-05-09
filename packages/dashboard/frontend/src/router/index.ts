import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/overview' },
    { path: '/overview', component: () => import('../views/OverviewView.vue') },
    { path: '/agents', component: () => import('../views/AgentsView.vue') },
    { path: '/routing', component: () => import('../views/RoutingView.vue') },
    { path: '/providers', component: () => import('../views/ProvidersView.vue') },
    { path: '/conversations', component: () => import('../views/ConversationsView.vue') },
    { path: '/plugins', component: () => import('../views/PluginsView.vue') },
    { path: '/store', component: () => import('../views/StoreView.vue') },
    { path: '/logs', component: () => import('../views/LogsView.vue') },
    { path: '/settings', component: () => import('../views/SettingsView.vue') },
  ],
})

export default router
