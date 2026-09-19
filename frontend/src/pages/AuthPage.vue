<script setup lang="ts">
import {ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {useCareerStore} from '../stores/career'
import BrandMark from '../components/BrandMark.vue'
const store=useCareerStore(),router=useRouter(),route=useRoute(),loginMode=ref(true)
const form=ref({username:'',email:'',password:''}),message=ref('')
async function submit(){message.value='';try{if(loginMode.value)await store.login(form.value.username,form.value.password);else await store.register(form.value.username,form.value.email,form.value.password);router.push(String(route.query.redirect||'/app/dashboard'))}catch(e:any){message.value=e.message}}
</script>
<template><div class="auth-page"><section class="auth-visual"><RouterLink class="product-brand" to="/"><BrandMark/><b>CareerAgent</b></RouterLink><div><p class="kicker">EVIDENCE, NOT GUESSWORK</p><h1>把每一次求职准备，<br>变成可验证的进步。</h1><p>你的简历证据、岗位要求和面试表现，始终由你掌控。</p></div><small>Spring Boot + Spring AI + Vue 3</small></section><section class="auth-form-wrap"><form class="auth-form" @submit.prevent="submit"><p class="kicker">{{loginMode?'WELCOME BACK':'CREATE YOUR WORKSPACE'}}</p><h2>{{loginMode?'继续你的求职准备':'建立职业工作空间'}}</h2><label>用户名或邮箱<input v-model="form.username" autocomplete="username" required placeholder="输入用户名或邮箱"></label><label v-if="!loginMode">邮箱<input v-model="form.email" type="email" required placeholder="name@example.com"></label><label>密码<input v-model="form.password" type="password" minlength="6" autocomplete="current-password" required placeholder="至少 6 位"></label><p v-if="message" class="form-error">{{message}}</p><button class="button full" :disabled="store.loading">{{store.loading?'正在进入…':loginMode?'登录':'创建账号'}}</button><button class="plain-button" type="button" @click="loginMode=!loginMode">{{loginMode?'没有账号？创建一个':'已有账号？返回登录'}}</button><div class="auth-divider"><span>或</span></div><RouterLink class="outline-button" to="/demo">无需注册，使用演示数据</RouterLink></form></section></div></template>
