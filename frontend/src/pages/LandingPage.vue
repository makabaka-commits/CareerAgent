<script setup lang="ts">
const capabilities=[
  ['01','读懂你的证据','从 PDF、DOCX 与 Markdown 中抽取技能，不把“提到”误当成“精通”。'],
  ['02','解释岗位差距','分开计算覆盖、证据、熟练度与项目相关性，每一分都有来源。'],
  ['03','把差距变成行动','Agent 调用领域工具，给出项目补强、投递和面试训练建议。']
]
</script>
<template>
  <div class="landing">
    <header class="site-nav"><RouterLink class="product-brand dark" to="/"><b>Stepwise</b><small>让每一步，都有依据。</small></RouterLink><nav><a href="#product">产品</a><a href="#method">方法</a><a href="#engineering">工程</a></nav><div><RouterLink class="nav-link" to="/login">登录</RouterLink><RouterLink class="button small" to="/demo">访客体验</RouterLink></div></header>
    <main>
      <section class="landing-hero"><div class="hero-copy"><p class="kicker"><span></span> EVIDENCE-LED CAREER GROWTH</p><h1>让求职建议，<br>建立在证据上。</h1><p class="hero-lead">Stepwise 把简历、岗位、技能差距、知识检索和模拟面试串成一个可解释的成长工作流。</p><div class="hero-actions"><RouterLink class="button" to="/demo">以访客身份体验 <b>↗</b></RouterLink><a class="text-link" href="#method">了解评分方法 ↓</a></div></div>
        <div class="hero-product"><div class="agent-window"><div class="window-top"><span>Stepwise 顾问</span><i>● READY</i></div><div class="prompt"><p>我适合这个 Java 后端岗位吗？</p><button>↑</button></div><div class="agent-result"><small>已调用 4 个领域工具</small><strong>值得投递，但 Redis 证据偏弱。</strong><p>技能覆盖 89% · 证据强度 67% · 置信度中等</p><div><span>Java</span><span>Spring Boot</span><span class="weak">Redis</span></div></div></div><span class="floating-note one">可追溯引用</span><span class="floating-note two">确定性评分</span></div>
      </section>
      <section id="product" class="statement"><p>不是另一个聊天框。</p><h2>这是一个会读取事实、调用工具、展示依据，并把差距转化为下一步行动的求职工作空间。</h2></section>
      <section class="capability-grid"><article v-for="item in capabilities" :key="item[0]"><span>{{item[0]}}</span><h3>{{item[1]}}</h3><p>{{item[2]}}</p></article></section>
      <section id="method" class="method-section"><div><p class="kicker">EXPLAINABLE BY DESIGN</p><h2>一个分数，四个维度。</h2><p>模型负责抽取和解释，Java 规则负责计分。算法不会因为语言表达漂亮就擅自提高分数。</p></div><div class="dimension-demo"><div><span>技能覆盖</span><b>40%</b></div><div><span>证据强度</span><b>30%</b></div><div><span>熟练度适配</span><b>20%</b></div><div><span>项目相关性</span><b>10%</b></div></div></section>
      <section id="engineering" class="engineering"><p class="kicker">BUILT LIKE A REAL PRODUCT</p><div><h2>从体验闭环到生产架构。</h2><p>Spring Boot · Spring AI · Vue 3 · PostgreSQL · pgvector · Redis · SSE · Docker</p></div><RouterLink class="button light" to="/demo">进入访客体验 ↗</RouterLink></section>
    </main>
    <footer><RouterLink class="product-brand dark" to="/"><b>Stepwise</b><small>让每一步，都有依据。</small></RouterLink><p>为计算机专业学生构建的可解释求职智能体。</p><span>© 2026 Stepwise</span></footer>
  </div>
</template>
