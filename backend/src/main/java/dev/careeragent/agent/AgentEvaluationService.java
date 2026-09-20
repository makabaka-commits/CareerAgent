package dev.careeragent.agent;
import org.springframework.stereotype.Service;
import java.util.*;
@Service public class AgentEvaluationService{
 enum Intent{MATCH,INTERVIEW,EVIDENCE,GENERAL}private record Case(String prompt,Intent expected){}
 private final List<Case> cases=List.of(new Case("我适合这个岗位吗",Intent.MATCH),new Case("分析岗位差距",Intent.MATCH),new Case("匹配分为什么不高",Intent.MATCH),new Case("缺少哪些技能",Intent.MATCH),new Case("开始模拟面试",Intent.INTERVIEW),new Case("给我一道 Java 面试题",Intent.INTERVIEW),new Case("如何回答项目难点",Intent.INTERVIEW),new Case("评价我的面试回答",Intent.INTERVIEW),new Case("怎么证明 Redis 能力",Intent.EVIDENCE),new Case("补充项目证据",Intent.EVIDENCE),new Case("简历如何量化成果",Intent.EVIDENCE),new Case("技能证据太弱怎么办",Intent.EVIDENCE),new Case("制定三天准备计划",Intent.GENERAL),new Case("今天先做什么",Intent.GENERAL),new Case("介绍一下功能",Intent.GENERAL),new Case("如何使用系统",Intent.GENERAL));
 Intent classify(String prompt){String p=prompt.toLowerCase(Locale.ROOT);if(any(p,"匹配","岗位差距","适合","缺少哪些"))return Intent.MATCH;if(any(p,"面试","回答"))return Intent.INTERVIEW;if(any(p,"证据","简历","项目能力","量化"))return Intent.EVIDENCE;return Intent.GENERAL;}
 public Map<String,Object> run(){long passed=cases.stream().filter(c->classify(c.prompt)==c.expected).count();return Map.of("datasetSize",cases.size(),"passed",passed,"intentAccuracy",Math.round(passed*1000.0/cases.size())/10.0);}
 private boolean any(String v,String...terms){return Arrays.stream(terms).anyMatch(v::contains);}
}
