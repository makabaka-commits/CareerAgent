<script setup lang="ts">
import {computed,onMounted,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {useCareerStore} from '../stores/career'
const route=useRoute(),router=useRouter(),store=useCareerStore(),mobileOpen=ref(false)
const title=computed(()=>String(route.meta.title||'Stepwise'))
const nav=[['/app/dashboard','总览','01'],['/app/profile','职业画像','02'],['/app/resumes','简历证据','03'],['/app/jobs','岗位匹配','04'],['/app/agent','Stepwise 顾问','05'],['/app/interviews','模拟面试','06']]
function signOut(){store.logout();router.push('/')}
function chooseJob(event:Event){const value=(event.target as HTMLSelectElement).value;store.selectJob(value?Number(value):undefined)}
onMounted(()=>store.ensureReady())
</script>
<template>
  <div class="workspace-shell">
    <aside class="app-sidebar" :class="{open:mobileOpen}">
      <RouterLink class="product-brand" to="/"><b>Stepwise</b><small>让每一步，都有依据。</small></RouterLink>
      <nav><RouterLink v-for="item in nav" :key="item[0]" :to="item[0]" @click="mobileOpen=false"><span>{{item[2]}}</span>{{item[1]}}</RouterLink></nav>
      <div class="sidebar-foot"><span>当前方向</span><strong>{{store.profile.targetPosition||'尚未设置'}}</strong><button @click="signOut">退出账号 ↗</button></div>
    </aside>
    <main class="workspace-main">
      <header class="workspace-header"><button class="menu-button" @click="mobileOpen=!mobileOpen">☰</button><div><p>CAREER WORKSPACE</p><h1>{{title}}</h1></div><label class="target-select"><span>目标岗位</span><select :value="store.selectedJob" @change="chooseJob"><option value="">尚未选择</option><option v-for="job in store.jobs" :key="job.id" :value="job.id">{{job.companyName}} · {{job.positionName}}</option></select></label></header>
      <div v-if="store.error" class="global-error">{{store.error}}</div>
      <div v-if="!store.ready&&store.loading" class="page-state"><div class="loader"></div><p>正在组装你的职业上下文…</p></div>
      <RouterView v-else />
    </main>
  </div>
</template>
