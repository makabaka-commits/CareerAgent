package dev.careeragent.agent;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class AgentEvaluationServiceTest{
 @Test void benchmarkRemainsDeterministic(){var result=new AgentEvaluationService().run();assertThat(result.get("datasetSize")).isEqualTo(16);assertThat((Double)result.get("intentAccuracy")).isGreaterThanOrEqualTo(90.0);}
}
