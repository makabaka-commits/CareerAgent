package dev.careeragent.auth;
import dev.careeragent.common.ApiException;import org.springframework.http.HttpStatus;import org.springframework.stereotype.Component;import java.time.*;import java.util.*;import java.util.concurrent.ConcurrentHashMap;
@Component public class LoginAttemptGuard{
 private static final int MAX_FAILURES=5;private static final Duration WINDOW=Duration.ofMinutes(10);private final Map<String,Deque<Instant>> failures=new ConcurrentHashMap<>();
 public void check(String account){Deque<Instant> values=active(account);if(values.size()>=MAX_FAILURES)throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"登录失败次数过多，请十分钟后重试");}
 public void failure(String account){Deque<Instant> values=active(account);synchronized(values){values.addLast(Instant.now());}}
 public void success(String account){failures.remove(key(account));}
 private Deque<Instant> active(String account){Deque<Instant> values=failures.computeIfAbsent(key(account),k->new ArrayDeque<>());synchronized(values){Instant cutoff=Instant.now().minus(WINDOW);while(!values.isEmpty()&&values.peekFirst().isBefore(cutoff))values.removeFirst();}return values;}
 private String key(String account){return account.trim().toLowerCase(Locale.ROOT);}
}
