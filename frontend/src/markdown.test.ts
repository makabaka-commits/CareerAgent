import {describe,expect,it} from 'vitest'
import {parseInline,parseMarkdown} from './markdown'

describe('agent answer markdown',()=>{
  it('turns headings and lists into display blocks',()=>{
    expect(parseMarkdown('# 三天计划\n\n## 第一天\n- **目标：** 梳理岗位\n- 输出证据清单')).toEqual([
      {type:'heading',level:1,text:'三天计划'},
      {type:'heading',level:2,text:'第一天'},
      {type:'unordered-list',items:['**目标：** 梳理岗位','输出证据清单']}
    ])
  })
  it('keeps model content as text instead of executable html',()=>{
    expect(parseInline('**重点** `<script>`')).toEqual([
      {type:'strong',text:'重点'},
      {type:'text',text:' '},
      {type:'code',text:'<script>'}
    ])
  })
})
