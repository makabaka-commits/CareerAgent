<script setup lang="ts">
import {ref} from 'vue'
import {useCareerStore} from '../stores/career'
const store=useCareerStore(),input=ref<HTMLInputElement>(),dragging=ref(false),notice=ref('')
async function accept(file?:File){if(!file)return;notice.value='';try{await store.uploadResume(file);notice.value='解析完成。技能等级由上下文推断，请核对后确认。'}catch(e:any){notice.value=e.message}}
function choose(event:Event){accept((event.target as HTMLInputElement).files?.[0])}
function drop(event:DragEvent){dragging.value=false;accept(event.dataTransfer?.files?.[0])}
async function remove(id:number){if(window.confirm('确定删除这份简历及其技能证据吗？'))await store.deleteResume(id)}
</script>
<template>
  <section class="workspace-page">
    <div class="upload-zone" :class="{dragging}" @dragover.prevent="dragging=true" @dragleave.prevent="dragging=false" @drop.prevent="drop" @click="input?.click()"><input ref="input" hidden type="file" accept=".pdf,.docx,.txt,.md" @change="choose"><span>＋</span><h2>上传你的当前简历</h2><p>拖放或选择 PDF、DOCX、TXT、Markdown，最大 8 MB；扫描版 PDF 可选 OCR。</p><button class="outline-button" :disabled="store.loading">{{store.loading?'正在解析…':'选择文件'}}</button></div>
    <p v-if="notice" class="inline-notice">{{notice}}</p>
    <div class="section-heading"><div><p class="kicker">RESUME EVIDENCE</p><h2>结构化记录</h2></div><span>{{store.resumes.length}} 份</span></div>
    <div v-if="store.resumes.length" class="resume-grid">
      <article v-for="resume in store.resumes" :key="resume.id"><div class="document-icon">DOC</div><div class="resume-copy"><span class="status-chip">{{resume.status}}</span><h3>{{resume.fileName}}</h3><div v-if="resume.parsed?.skills?.length" class="parsed-skills"><label v-for="skill in resume.parsed.skills" :key="skill.name"><b>{{skill.name}}</b><span>置信度 {{Math.round((skill.confidence||0)*100)}}%</span><select v-model.number="skill.level"><option v-for="level in 5" :key="level" :value="level">Lv.{{level}}</option></select><small>{{skill.evidence}}</small></label></div><p v-else>等待解析</p><small>{{new Date(resume.createdAt).toLocaleDateString()}}</small></div><div class="card-actions"><button v-if="resume.status==='PARSED'" class="button small" @click="store.confirmResume(resume.id,resume.parsed)">核对并确认</button><b v-else-if="resume.current">✓ 当前简历</b><button class="icon-button danger" title="删除" @click.stop="remove(resume.id)">×</button></div></article>
    </div>
    <div v-else class="empty-state surface"><b>尚未上传简历</b><p>仓库中的 tests/fixtures/resume-demo.md 可用于体验。</p></div>
  </section>
</template>
