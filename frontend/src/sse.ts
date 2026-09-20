export type SseEvent={name?:string,data:unknown}
export function parseSseBuffer(buffer:string){
  const blocks=buffer.split('\n\n'),rest=blocks.pop()||''
  const events:SseEvent[]=blocks.flatMap(block=>{
    const name=block.match(/(?:^|\n)event:\s*(.+)/)?.[1]?.trim()
    const raw=block.match(/(?:^|\n)data:\s*(.+)/)?.[1]?.trim()
    if(!raw)return[]
    try{return[{name,data:JSON.parse(raw)}]}catch{return[{name,data:raw}]}
  })
  return{events,rest}
}
