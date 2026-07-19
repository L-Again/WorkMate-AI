import { createRouter, createWebHistory } from 'vue-router'
import AgentChatView from '../views/AgentChatView.vue'
import KnowledgeView from '../views/KnowledgeView.vue'
import CategoryView from '../views/CategoryView.vue'
import TicketView from '../views/TicketView.vue'
import ModelLogView from '../views/ModelLogView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/agent' },
    { path: '/agent', component: AgentChatView },
    { path: '/knowledge', component: KnowledgeView },
    { path: '/categories', component: CategoryView },
    { path: '/tickets', component: TicketView },
    { path: '/model-logs', component: ModelLogView },
  ],
})

export default router