<script setup lang="ts">
import {computed} from 'vue'
import {parseInline,parseMarkdown} from '../markdown'
const props=defineProps<{content:string}>()
const blocks=computed(()=>parseMarkdown(props.content))
</script>

<template>
  <div class="answer-content">
    <template v-for="(block,index) in blocks" :key="index">
      <h3 v-if="block.type==='heading'&&block.level===1"><template v-for="(part,i) in parseInline(block.text||'')" :key="i"><strong v-if="part.type==='strong'">{{part.text}}</strong><em v-else-if="part.type==='emphasis'">{{part.text}}</em><code v-else-if="part.type==='code'">{{part.text}}</code><template v-else>{{part.text}}</template></template></h3>
      <h4 v-else-if="block.type==='heading'"><template v-for="(part,i) in parseInline(block.text||'')" :key="i"><strong v-if="part.type==='strong'">{{part.text}}</strong><em v-else-if="part.type==='emphasis'">{{part.text}}</em><code v-else-if="part.type==='code'">{{part.text}}</code><template v-else>{{part.text}}</template></template></h4>
      <p v-else-if="block.type==='paragraph'"><template v-for="(part,i) in parseInline(block.text||'')" :key="i"><strong v-if="part.type==='strong'">{{part.text}}</strong><em v-else-if="part.type==='emphasis'">{{part.text}}</em><code v-else-if="part.type==='code'">{{part.text}}</code><template v-else>{{part.text}}</template></template></p>
      <ul v-else-if="block.type==='unordered-list'"><li v-for="(item,itemIndex) in block.items" :key="itemIndex"><template v-for="(part,i) in parseInline(item)" :key="i"><strong v-if="part.type==='strong'">{{part.text}}</strong><em v-else-if="part.type==='emphasis'">{{part.text}}</em><code v-else-if="part.type==='code'">{{part.text}}</code><template v-else>{{part.text}}</template></template></li></ul>
      <ol v-else-if="block.type==='ordered-list'"><li v-for="(item,itemIndex) in block.items" :key="itemIndex"><template v-for="(part,i) in parseInline(item)" :key="i"><strong v-if="part.type==='strong'">{{part.text}}</strong><em v-else-if="part.type==='emphasis'">{{part.text}}</em><code v-else-if="part.type==='code'">{{part.text}}</code><template v-else>{{part.text}}</template></template></li></ol>
      <blockquote v-else-if="block.type==='quote'">{{block.text}}</blockquote>
      <pre v-else-if="block.type==='code'"><code>{{block.text}}</code></pre>
      <hr v-else-if="block.type==='divider'">
    </template>
  </div>
</template>
