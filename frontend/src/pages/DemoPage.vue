<script setup lang="ts">
import {onMounted,ref} from 'vue'
import {useRouter} from 'vue-router'
import {useCareerStore} from '../stores/career'
import BrandMark from '../components/BrandMark.vue'
const store=useCareerStore(),router=useRouter(),failed=ref('')
onMounted(async()=>{try{await store.startDemo();setTimeout(()=>router.replace('/app/dashboard'),500)}catch(e:any){failed.value=e.message}})
</script>
<template><div class="demo-loader"><BrandMark/><p class="kicker">PREPARING DEMO</p><h1>正在创建一份有差距的真实示例。</h1><p>{{failed||'装配职业画像、岗位要求和证据评分…'}}</p><div class="progress-line"><i></i></div><RouterLink v-if="failed" class="button" to="/">返回首页</RouterLink></div></template>
