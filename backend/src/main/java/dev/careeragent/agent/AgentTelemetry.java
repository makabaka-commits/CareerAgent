package dev.careeragent.agent;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
@Component public class AgentTelemetry{
 private final AtomicLong calls=new AtomicLong(),fallbacks=new AtomicLong(),totalMillis=new AtomicLong();private final MeterRegistry meters;
 public AgentTelemetry(MeterRegistry meters){this.meters=meters;}
 public void record(boolean fallback,long nanos){calls.incrementAndGet();if(fallback)fallbacks.incrementAndGet();totalMillis.addAndGet(TimeUnit.NANOSECONDS.toMillis(nanos));meters.counter("stepwise.agent.calls","mode",fallback?"fallback":"model").increment();Timer.builder("stepwise.agent.latency").register(meters).record(nanos,TimeUnit.NANOSECONDS);}
 public long calls(){return calls.get();}public long fallbacks(){return fallbacks.get();}public long averageMillis(){return calls.get()==0?0:totalMillis.get()/calls.get();}
}
