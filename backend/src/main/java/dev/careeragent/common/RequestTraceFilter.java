package dev.careeragent.common;
import jakarta.servlet.*;import jakarta.servlet.http.*;import org.slf4j.MDC;import org.springframework.core.Ordered;import org.springframework.core.annotation.Order;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;import java.io.IOException;import java.util.UUID;
@Component @Order(Ordered.HIGHEST_PRECEDENCE+1) public class RequestTraceFilter extends OncePerRequestFilter{
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{String trace=headerOrId(request.getHeader("X-Request-ID"));MDC.put("traceId",trace);response.setHeader("X-Request-ID",trace);try{chain.doFilter(request,response);}finally{MDC.remove("traceId");}}
 private String headerOrId(String value){return value!=null&&value.matches("[A-Za-z0-9_-]{8,64}")?value:UUID.randomUUID().toString().replace("-","").substring(0,16);}
}
