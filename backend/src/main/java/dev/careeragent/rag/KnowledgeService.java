package dev.careeragent.rag;

import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class KnowledgeService {
    private final InMemoryStore store;
    public KnowledgeService(InMemoryStore store){this.store=store;}
    @PostConstruct void seed(){
        if(!store.knowledge.isEmpty()) return;
        add("Java 实习面试知识","knowledge/java/core.md","Java","集合与并发","HashMap 需要解释哈希、桶、冲突处理、扩容和线程安全边界。并发场景应根据访问模式选择 ConcurrentHashMap。");
        add("Spring Boot 工程实践","knowledge/spring/boot.md","Spring Boot","依赖注入","说明依赖注入时，应结合构造器注入、可测试性、单一职责与 Bean 生命周期，并给出项目中的实际例子。");
        add("Redis 求职知识","knowledge/redis/core.md","Redis","缓存设计","缓存方案需要同时考虑命中率、TTL、缓存穿透、击穿、雪崩和数据一致性，并描述监控指标。");
        add("MySQL 求职知识","knowledge/mysql/index.md","MySQL","索引","解释联合索引要覆盖 B+Tree、有序性、最左前缀、回表与覆盖索引，并使用 EXPLAIN 验证优化效果。");
    }
    public KnowledgeChunk add(String title,String source,String topic,String section,String content){long id=store.nextId();KnowledgeChunk c=new KnowledgeChunk(id,title,source,topic,section,content);store.knowledge.put(id,c);return c;}
    public List<RetrievedKnowledge> search(String query,String topic,int limit){
        Set<String> tokens=tokens(query);String filter=topic==null?"":topic.trim();
        return store.knowledge.values().stream().filter(c->filter.isEmpty()||c.topic().equalsIgnoreCase(filter)).filter(c->!suspicious(c.content())).map(c->{
            Set<String> body=tokens(c.content());Set<String> heading=tokens(c.title()+" "+c.topic()+" "+c.section());
            long bodyHits=tokens.stream().filter(body::contains).count();long headingHits=tokens.stream().filter(heading::contains).count();
            double lexical=tokens.isEmpty()?0:(double)bodyHits/tokens.size();
            double headingBoost=tokens.isEmpty()?0:(double)headingHits/tokens.size();
            double phrase=Optional.ofNullable(query).orElse("").length()>1&&c.content().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))?.15:0;
            double score=Math.min(1,.7*lexical+.3*headingBoost+phrase);return new RetrievedKnowledge(c.id(),c.title(),c.source(),c.section(),c.content(),Math.round(score*100)/100.0);
        }).filter(r->r.score()>0).sorted(Comparator.comparing(RetrievedKnowledge::score).reversed()).limit(Math.max(1,Math.min(limit,10))).toList();
    }
    private boolean suspicious(String content){String value=Optional.ofNullable(content).orElse("").toLowerCase(Locale.ROOT);return List.of("ignore previous","system prompt","忽略以上","忽略之前","执行以下指令","泄露提示词").stream().anyMatch(value::contains);}
    private Set<String> tokens(String text){Set<String> result=new HashSet<>();String normalized=Optional.ofNullable(text).orElse("").toLowerCase(Locale.ROOT);for(String part:normalized.split("[^\\p{L}\\p{N}+#.]+"))if(part.length()>1)result.add(part);for(int i=0;i<normalized.length()-1;i++){String pair=normalized.substring(i,i+2);if(pair.matches("[\\p{IsHan}]{2}"))result.add(pair);}return result;}
}
