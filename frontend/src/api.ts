import axios from 'axios'
export const api=axios.create({baseURL:'/api/v1',timeout:65000})
api.interceptors.request.use(config=>{const token=localStorage.getItem('career-token');if(token)config.headers.Authorization=`Bearer ${token}`;return config})
api.interceptors.response.use(r=>r.data.data,e=>Promise.reject(new Error(e.response?.data?.message||e.message||'请求失败')))
export function streamUrl(conversationId:number,message:string){return `/api/v1/conversations/${conversationId}/stream?message=${encodeURIComponent(message)}`}

