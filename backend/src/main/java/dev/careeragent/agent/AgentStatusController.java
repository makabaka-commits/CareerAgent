package dev.careeragent.agent;
import dev.careeragent.common.ApiResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/agent") public class AgentStatusController{
 private final ObjectProvider<ChatModel> models;private final AgentTelemetry telemetry;private final AgentEvaluationService evaluation;
 public AgentStatusController(ObjectProvider<ChatModel> models,AgentTelemetry telemetry,AgentEvaluationService evaluation){this.models=models;this.telemetry=telemetry;this.evaluation=evaluation;}
 @GetMapping("/status")public ApiResponse<Map<String,Object>> status(){boolean model=models.getIfAvailable()!=null;return ApiResponse.ok(Map.of("mode",model?"MODEL":"FALLBACK","label",model?"智能模型模式":"规则保障模式","calls",telemetry.calls(),"fallbacks",telemetry.fallbacks(),"averageLatencyMs",telemetry.averageMillis(),"evaluation",evaluation.run()));}
}
