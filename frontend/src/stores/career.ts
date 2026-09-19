import {computed,ref} from 'vue'
import {defineStore} from 'pinia'
import {api} from '../api'

export const useCareerStore=defineStore('career',()=>{
  const token=ref(localStorage.getItem('career-token')||'')
  const user=ref<any>(JSON.parse(localStorage.getItem('career-user')||'null'))
  const loading=ref(false),ready=ref(false),error=ref('')
  const profile=ref<any>({school:'',major:'计算机科学与技术',grade:'应届生',targetPosition:'Java 后端实习',targetCity:'',selfDescription:''})
  const skills=ref<any[]>([]),resumes=ref<any[]>([]),jobs=ref<any[]>([]),conversations=ref<any[]>([])
  const selectedJob=ref<number|undefined>(Number(localStorage.getItem('career-job'))||undefined)
  const match=ref<any>(null),chatMessages=ref<any[]>([]),conversationId=ref<number>()
  const interview=ref<any>(),question=ref<any>(),report=ref<any>()
  const selectedJobData=computed(()=>jobs.value.find(j=>j.id===selectedJob.value))

  function saveAuth(data:any){token.value=data.token;user.value=data.user;localStorage.setItem('career-token',data.token);localStorage.setItem('career-user',JSON.stringify(data.user))}
  async function task<T>(fn:()=>Promise<T>){loading.value=true;error.value='';try{return await fn()}catch(e:any){error.value=e.message||'请求失败';throw e}finally{loading.value=false}}
  async function login(account:string,password:string){return task(async()=>{const data:any=await api.post('/auth/login',{account,password});saveAuth(data);await refresh()})}
  async function register(username:string,email:string,password:string){return task(async()=>{const data:any=await api.post('/auth/register',{username,email,password});saveAuth(data);await refresh()})}
  async function startDemo(){return task(async()=>{const data:any=await api.post('/auth/demo');saveAuth(data);selectedJob.value=data.jobId;localStorage.setItem('career-job',String(data.jobId));await refresh();await calculateMatch()})}
  function logout(){token.value='';user.value=null;localStorage.removeItem('career-token');localStorage.removeItem('career-user');localStorage.removeItem('career-job');reset()}
  function reset(){ready.value=false;skills.value=[];resumes.value=[];jobs.value=[];conversations.value=[];match.value=null;chatMessages.value=[];conversationId.value=undefined;interview.value=undefined;question.value=undefined;report.value=undefined}
  async function refresh(){const data:any=await Promise.all([api.get('/profiles/me'),api.get('/profiles/me/skills'),api.get('/resumes'),api.get('/jobs'),api.get('/conversations')]);[profile.value,skills.value,resumes.value,jobs.value,conversations.value]=data;if(!jobs.value.some(j=>j.id===selectedJob.value))selectedJob.value=jobs.value[0]?.id;if(selectedJob.value)localStorage.setItem('career-job',String(selectedJob.value));ready.value=true}
  async function ensureReady(){if(!ready.value&&token.value)await task(refresh)}
  async function saveProfile(){await task(async()=>{profile.value=await api.put('/profiles/me',profile.value)})}
  async function uploadResume(file:File){await task(async()=>{const body=new FormData();body.append('file',file);const value:any=await api.post('/resumes',body);await api.post(`/resumes/${value.id}/parse`);await refresh()})}
  async function confirmResume(id:number){await task(async()=>{await api.post(`/resumes/${id}/confirm`);await refresh()})}
  async function deleteResume(id:number){await task(async()=>{await api.delete(`/resumes/${id}`);await refresh()})}
  async function createJob(form:any){await task(async()=>{const job:any=await api.post('/jobs',form);await refresh();selectJob(job.id);await calculateMatch()})}
  function selectJob(id:number|undefined){selectedJob.value=id;match.value=null;if(id)localStorage.setItem('career-job',String(id));else localStorage.removeItem('career-job')}
  async function calculateMatch(){if(!selectedJob.value)return;match.value=await task(()=>api.post(`/jobs/${selectedJob.value}/match`))}
  async function ensureConversation(){if(conversationId.value)return conversationId.value;const value:any=await api.post('/conversations',{scene:'JOB_ANALYSIS',title:selectedJobData.value?.positionName||'求职咨询',jobId:selectedJob.value});conversationId.value=value.id;return value.id}
  async function sendChat(text:string){
    if(!text.trim())return
    chatMessages.value.push({role:'USER',content:text,status:'已发送'})
    const id=await ensureConversation()
    const response=await fetch(`/api/v1/conversations/${id}/stream?message=${encodeURIComponent(text)}`,{headers:{Authorization:`Bearer ${token.value}`}})
    if(!response.body)throw new Error('当前浏览器不支持流式响应')
    const assistant:any={role:'ASSISTANT',content:'',status:'正在分析',citations:[],trace:[]};chatMessages.value.push(assistant)
    const reader=response.body.getReader(),decoder=new TextDecoder();let buffer=''
    while(true){const {done,value}=await reader.read();if(done)break;buffer+=decoder.decode(value,{stream:true});const events=buffer.split('\n\n');buffer=events.pop()||'';for(const block of events){const name=block.match(/event:(.+)/)?.[1]?.trim();const raw=block.match(/data:(.+)/)?.[1]?.trim();if(!raw)continue;let data:any;try{data=JSON.parse(raw)}catch{data=raw}if(name==='status')assistant.status=String(data);if(name==='tool_call')assistant.trace.push(data);if(name==='content')assistant.content+=typeof data==='string'?data:String(data);if(name==='citation')assistant.citations.push(data);if(name==='done')assistant.status='完成'}}
  }
  async function startInterview(){if(!selectedJob.value)throw new Error('请先选择岗位');await task(async()=>{interview.value=await api.post('/interviews',{jobId:selectedJob.value});question.value=await api.post(`/interviews/${interview.value.id}/start`);report.value=undefined})}
  async function submitAnswer(answer:string){if(!interview.value||!question.value)return;await task(async()=>{const data:any=await api.post(`/interviews/${interview.value.id}/answers`,{questionId:question.value.id,answer});question.value=data.nextQuestion;if(data.canFinish)await finishInterview()})}
  async function finishInterview(){if(!interview.value)return;report.value=await api.post(`/interviews/${interview.value.id}/finish`);question.value=undefined}
  return{token,user,loading,ready,error,profile,skills,resumes,jobs,conversations,selectedJob,selectedJobData,match,chatMessages,interview,question,report,login,register,startDemo,logout,refresh,ensureReady,saveProfile,uploadResume,confirmResume,deleteResume,createJob,selectJob,calculateMatch,sendChat,startInterview,submitAnswer,finishInterview}
})
