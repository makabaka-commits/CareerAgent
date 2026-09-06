package dev.careeragent.agent;

import dev.careeragent.auth.AuthFilter;
import dev.careeragent.common.*;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.job.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    private final InMemoryStore store;private final JobService jobs;private final AgentChatService agent;
    public ConversationController(InMemoryStore store,JobService jobs,AgentChatService agent){this.store=store;this.jobs=jobs;this.agent=agent;}
    public record CreateRequest(@NotBlank String scene,@Size(max=100) String title,Long jobId){}
    public record SendRequest(@NotBlank @Size(max=4000) String message){}
    @PostMapping public ApiResponse<Conversation> create(HttpServletRequest r,@Valid @RequestBody CreateRequest b){long u=uid(r);if(b.jobId()!=null)jobs.owned(u,b.jobId());long id=store.nextId();Conversation c=new Conversation(id,u,b.scene(),b.title()==null?"求职咨询":b.title(),b.jobId(),Instant.now());store.conversations.put(id,c);return ApiResponse.ok(c);}
    @GetMapping public ApiResponse<List<Conversation>> list(HttpServletRequest r){long u=uid(r);return ApiResponse.ok(store.conversations.values().stream().filter(c->c.userId()==u).sorted(Comparator.comparing(Conversation::createdAt).reversed()).toList());}
    @GetMapping("/{id}/messages") public ApiResponse<List<Message>> messages(HttpServletRequest r,@PathVariable long id){Conversation c=owned(uid(r),id);return ApiResponse.ok(store.messages.values().stream().filter(m->m.conversationId()==c.id()).sorted(Comparator.comparing(Message::createdAt)).toList());}
    @PostMapping("/{id}/messages") public ApiResponse<Message> send(HttpServletRequest r,@PathVariable long id,@Valid @RequestBody SendRequest b){long u=uid(r);Conversation c=owned(u,id);saveUser(c.id(),b.message());return ApiResponse.ok(agent.reply(u,c,b.message(),(event,data)->{}));}
    @GetMapping(value="/{id}/stream",produces="text/event-stream") public SseEmitter stream(HttpServletRequest r,@PathVariable long id,@RequestParam @Size(max=4000) String message){long u=uid(r);Conversation c=owned(u,id);saveUser(id,message);SseEmitter emitter=new SseEmitter(65_000L);CompletableFuture.runAsync(()->{try{Message answer=agent.reply(u,c,message,(event,data)->sendEvent(emitter,event,data));sendEvent(emitter,"done",Map.of("messageId",answer.id()));emitter.complete();}catch(Exception e){sendEvent(emitter,"error",Map.of("message",e.getMessage()));emitter.completeWithError(e);}});return emitter;}
    private void sendEvent(SseEmitter e,String name,Object data){try{e.send(SseEmitter.event().name(name).data(data));}catch(Exception ignored){}}
    private void saveUser(long conversationId,String content){long id=store.nextId();store.messages.put(id,new Message(id,conversationId,"USER",content,Map.of(),Instant.now()));}
    private Conversation owned(long u,long id){Conversation c=store.conversations.get(id);if(c==null||c.userId()!=u)throw new ApiException(HttpStatus.NOT_FOUND,"会话不存在");return c;}
    private long uid(HttpServletRequest r){return(long)r.getAttribute(AuthFilter.USER_ID);}
}
