<script setup lang="ts">
import {onMounted,ref} from 'vue'
import {useRouter} from 'vue-router'
import {useCareerStore} from '../stores/career'
const store=useCareerStore(),router=useRouter(),failed=ref('')
onMounted(async()=>{try{await store.startDemo();setTimeout(()=>router.replace('/app/dashboard'),500)}catch(e:any){failed.value=e.message}})
</script>
<template><div class="demo-loader"><p class="kicker">PREPARING WORKSPACE</p><h1>正在准备访客工作空间。</h1><p>{{failed||'载入职业画像、岗位要求和证据评分…'}}</p><div class="progress-line"><i></i></div><RouterLink v-if="failed" class="button" to="/">返回首页</RouterLink></div></template>
