import {createRouter,createWebHistory} from 'vue-router'
import LandingPage from './pages/LandingPage.vue'
import AuthPage from './pages/AuthPage.vue'
import DemoPage from './pages/DemoPage.vue'
import AppLayout from './layouts/AppLayout.vue'
import DashboardPage from './pages/DashboardPage.vue'
import ProfilePage from './pages/ProfilePage.vue'
import ResumePage from './pages/ResumePage.vue'
import JobsPage from './pages/JobsPage.vue'
import AgentPage from './pages/AgentPage.vue'
import InterviewPage from './pages/InterviewPage.vue'
import NotFoundPage from './pages/NotFoundPage.vue'
const router=createRouter({history:createWebHistory(),routes:[
  {path:'/',component:LandingPage},{path:'/login',component:AuthPage},{path:'/demo',component:DemoPage},
  {path:'/app',component:AppLayout,meta:{auth:true},children:[
    {path:'',redirect:'/app/dashboard'},{path:'dashboard',component:DashboardPage,meta:{title:'总览'}},
    {path:'profile',component:ProfilePage,meta:{title:'职业画像'}},{path:'resumes',component:ResumePage,meta:{title:'简历证据'}},
    {path:'jobs',component:JobsPage,meta:{title:'岗位匹配'}},{path:'agent',component:AgentPage,meta:{title:'Career Agent'}},
    {path:'interviews',component:InterviewPage,meta:{title:'模拟面试'}}]},
  {path:'/:pathMatch(.*)*',component:NotFoundPage}
]})
router.beforeEach(to=>to.meta.auth&&!localStorage.getItem('career-token')?`/login?redirect=${encodeURIComponent(to.fullPath)}`:true)
export default router
