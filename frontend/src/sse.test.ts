import {describe,expect,it} from 'vitest'
import {parseSseBuffer} from './sse'
describe('SSE parser',()=>{
  it('keeps incomplete blocks and parses json safely',()=>{const result=parseSseBuffer('event:status\ndata:"分析中"\n\nevent:tool_call\ndata:{"name":"match"}\n\nevent:content\ndata:');expect(result.events).toEqual([{name:'status',data:'分析中'},{name:'tool_call',data:{name:'match'}}]);expect(result.rest).toBe('event:content\ndata:')})
  it('accepts plain text payloads',()=>{expect(parseSseBuffer('event:content\ndata:hello\n\n').events[0].data).toBe('hello')})
  it('joins multiline data according to the SSE specification',()=>{
    const result=parseSseBuffer('event:content\r\ndata:三天面试准备计划\r\ndata:\r\ndata:第一天｜梳理岗位与证据\r\ndata:- 整理技能证据\r\n\r\n')
    expect(result.events).toEqual([{name:'content',data:'三天面试准备计划\n\n第一天｜梳理岗位与证据\n- 整理技能证据'}])
  })
})
