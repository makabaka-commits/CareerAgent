export type SseEvent={name?:string,data:unknown}
export function parseSseBuffer(buffer:string){
  const normalized=buffer.replace(/\r\n/g,'\n'),blocks=normalized.split('\n\n'),rest=blocks.pop()||''
  const events:SseEvent[]=blocks.flatMap(block=>{
    const lines=block.split('\n')
    const name=lines.find(line=>line.startsWith('event:'))?.slice(6).trim()
    const dataLines=lines.filter(line=>line.startsWith('data:')).map(line=>line.slice(5).replace(/^ /,''))
    if(!dataLines.length)return[]
    const raw=dataLines.join('\n')
    try{return[{name,data:JSON.parse(raw)}]}catch{return[{name,data:raw}]}
  })
  return{events,rest}
}
