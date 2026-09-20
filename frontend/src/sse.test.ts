import {describe,expect,it} from 'vitest'
import {parseSseBuffer} from './sse'
describe('SSE parser',()=>{
  it('keeps incomplete blocks and parses json safely',()=>{const result=parseSseBuffer('event:status\ndata:"分析中"\n\nevent:tool_call\ndata:{"name":"match"}\n\nevent:content\ndata:');expect(result.events).toEqual([{name:'status',data:'分析中'},{name:'tool_call',data:{name:'match'}}]);expect(result.rest).toBe('event:content\ndata:')})
  it('accepts plain text payloads',()=>{expect(parseSseBuffer('event:content\ndata:hello\n\n').events[0].data).toBe('hello')})
})
