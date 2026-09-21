package dev.careeragent.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentChatServiceTest {
    @Test
    void recognizesDeepSeekEndpoint() {
        assertThat(AgentChatService.isDeepSeekBaseUrl("https://api.deepseek.com")).isTrue();
        assertThat(AgentChatService.isDeepSeekBaseUrl("https://api.openai.com")).isFalse();
    }

    @Test
    void rejectsIncompletePlanButAcceptsStructuredPlan() {
        assertThat(AgentChatService.shouldFallback("给我三天准备计划", "# 三天面试准备计划")).isTrue();
        String complete = "第一天：梳理岗位要求并整理项目证据。".repeat(4)
                + "第二天：补齐薄弱技能并完成可运行示例。".repeat(4)
                + "第三天：完成模拟面试并复盘表达。".repeat(4);
        assertThat(AgentChatService.shouldFallback("给我三天准备计划", complete)).isFalse();
    }

    @Test
    void removesInternalJobIdentifier() {
        assertThat(AgentChatService.sanitizeAnswer("计划如下（jobId=206）\n第一天复习 Java"))
                .doesNotContainIgnoringCase("jobId")
                .doesNotContain("206");
    }
}
