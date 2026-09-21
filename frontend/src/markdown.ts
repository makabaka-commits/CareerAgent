export type InlinePart={type:'text'|'strong'|'emphasis'|'code';text:string}
export type MarkdownBlock={type:'heading'|'paragraph'|'unordered-list'|'ordered-list'|'quote'|'code'|'divider';level?:number;text?:string;items?:string[]}

export function parseInline(text:string):InlinePart[]{
  const parts:InlinePart[]=[]
  const pattern=/(\*\*[^*]+\*\*|__[^_]+__|`[^`]+`|\[[^\]]+\]\([^)]+\)|\*[^*]+\*|_[^_]+_)/g
  let cursor=0
  for(const match of text.matchAll(pattern)){
    const index=match.index??0
    if(index>cursor)parts.push({type:'text',text:text.slice(cursor,index)})
    const token=match[0]
    if(token.startsWith('**')||token.startsWith('__'))parts.push({type:'strong',text:token.slice(2,-2)})
    else if(token.startsWith('`'))parts.push({type:'code',text:token.slice(1,-1)})
    else if(token.startsWith('['))parts.push({type:'text',text:token.match(/^\[([^\]]+)\]/)?.[1]||token})
    else parts.push({type:'emphasis',text:token.slice(1,-1)})
    cursor=index+token.length
  }
  if(cursor<text.length)parts.push({type:'text',text:text.slice(cursor)})
  return parts.length?parts:[{type:'text',text}]
}

export function parseMarkdown(markdown:string):MarkdownBlock[]{
  const lines=markdown.replace(/\r\n/g,'\n').split('\n')
  const blocks:MarkdownBlock[]=[]
  let paragraph:string[]=[]
  let list:MarkdownBlock|undefined
  const flushParagraph=()=>{if(paragraph.length){blocks.push({type:'paragraph',text:paragraph.join(' ')});paragraph=[]}}
  const flushList=()=>{if(list){blocks.push(list);list=undefined}}
  const flush=()=>{flushParagraph();flushList()}

  for(let index=0;index<lines.length;index++){
    const line=lines[index].trimEnd(),trimmed=line.trim()
    if(!trimmed){flush();continue}
    if(trimmed.startsWith('```')){
      flush();const code:string[]=[]
      while(++index<lines.length&&!lines[index].trim().startsWith('```'))code.push(lines[index])
      blocks.push({type:'code',text:code.join('\n')});continue
    }
    const heading=trimmed.match(/^(#{1,6})\s+(.+)$/)
    if(heading){flush();blocks.push({type:'heading',level:heading[1].length,text:heading[2].replace(/\s+#+$/,'')});continue}
    if(/^([-*_])(?:\s*\1){2,}$/.test(trimmed)){flush();blocks.push({type:'divider'});continue}
    const unordered=trimmed.match(/^[-+*]\s+(.+)$/)
    if(unordered){flushParagraph();if(list?.type!=='unordered-list')flushList();list??={type:'unordered-list',items:[]};list.items!.push(unordered[1]);continue}
    const ordered=trimmed.match(/^\d+[.)]\s+(.+)$/)
    if(ordered){flushParagraph();if(list?.type!=='ordered-list')flushList();list??={type:'ordered-list',items:[]};list.items!.push(ordered[1]);continue}
    const quote=trimmed.match(/^>\s?(.*)$/)
    if(quote){flush();blocks.push({type:'quote',text:quote[1]});continue}
    flushList();paragraph.push(trimmed)
  }
  flush()
  return blocks
}
