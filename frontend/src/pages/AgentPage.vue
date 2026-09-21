<script setup lang="ts">
import {nextTick,onMounted,ref} from 'vue'
import {useCareerStore} from '../stores/career'
import AnswerContent from '../components/AnswerContent.vue'
const store=useCareerStore(),input=ref('我适合这个岗位吗？应该优先补强什么证据？'),sending=ref(false),messages=ref<HTMLElement>()
async function send(){if(!input.value.trim()||sending.value)return;const text=input.value;input.value='';sending.value=true;try{await store.sendChat(text);await nextTick();messages.value?.scrollTo({top:messages.value.scrollHeight,behavior:'smooth'});await store.loadAgentStatus()}finally{sending.value=false}}
const prompts=['分析我的岗位差距','如何证明 Redis 项目能力？','给我一份三天面试准备计划']
onMounted(()=>store.loadAgentStatus())
</script>
<template>
  <section class="agent-page">
    <header class="agent-header"><div><span class="agent-orb">✦</span><div><h2>Stepwise 顾问</h2><p>{{store.selectedJobData?`已绑定 ${store.selectedJobData.companyName} · ${store.selectedJobData.positionName}`:'当前为通用求职咨询'}}</p></div></div><span class="ready-dot">● {{store.agentStatus?.label||'正在检测'}}</span></header>
    <div v-if="store.agentStatus" class="agent-runtime"><span>意图评测 {{store.agentStatus.evaluation.intentAccuracy}}%</span><span>{{store.agentStatus.evaluation.datasetSize}} 条固定样例</span><span>平均响应 {{store.agentStatus.averageLatencyMs}} ms</span><span>降级 {{store.agentStatus.fallbacks}} 次</span></div>
    <div ref="messages" class="agent-messages">
      <div v-if="!store.chatMessages.length" class="agent-empty"><span>✦</span><h2>从一个具体问题开始。</h2><p>我会读取最小必要上下文，公开工具调用轨迹，并为知识结论附上来源。</p><div><button v-for="prompt in prompts" :key="prompt" @click="input=prompt">{{prompt}}</button></div></div>
      <article v-for="(message,index) in store.chatMessages" :key="index" :class="['chat-message',message.role.toLowerCase()]"><div class="message-meta"><span>{{message.role==='USER'?'YOU':'STEPWISE'}}</span><small>{{message.status}}</small></div><AnswerContent v-if="message.role==='ASSISTANT'&&message.content" class="answer-bubble" :content="message.content"/><p v-else>{{message.content||'正在组织可验证的回答…'}}</p><div v-if="message.trace?.length" class="tool-trace"><span v-for="(tool,i) in message.trace" :key="i">↳ {{tool.name}} · {{tool.status}}</span></div><div v-if="message.citations?.length" class="citation-list"><span v-for="citation in message.citations" :key="citation.chunkId">↗ {{citation.title}} / {{citation.section}}</span></div></article>
    </div>
    <form class="agent-composer" @submit.prevent="send"><textarea v-model="input" rows="2" placeholder="询问岗位差距、项目证据或面试准备…" @keydown.ctrl.enter.prevent="send"></textarea><div><small>Ctrl + Enter 发送</small><button class="button" :disabled="sending">{{sending?'分析中':'发送 ↑'}}</button></div></form>
  </section>
</template>
