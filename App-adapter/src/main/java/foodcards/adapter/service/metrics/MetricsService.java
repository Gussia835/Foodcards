package foodcards.adapter.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Getter
public class MetricsService {

    private final MeterRegistry registry;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Counter sentCounter;
    private final Counter receivedCounter;
    private final Counter errorsCounter;
    private final Counter processedCounter;

    private final Timer processingTimer;

    private final AtomicInteger activeProducersCount = new AtomicInteger(0);
    private final AtomicInteger activeConsumersCount = new AtomicInteger(0);

    public MetricsService(MeterRegistry registry, KafkaTemplate<String, String> kafkaTemplate) {
        this.registry = registry;

        this.kafkaTemplate = kafkaTemplate;

        this.sentCounter = registry.counter("sent.messages");
        this.receivedCounter = registry.counter("received.messages");
        this.errorsCounter = registry.counter("error.messages");
        this.processedCounter = registry.counter("processed.messages");

        this.processingTimer = registry.timer("timer.process");

        Gauge.builder("active.producers", activeProducersCount::get)
                .description("Активные producers")
                        .register(registry);

        Gauge.builder("active.consumers", activeConsumersCount::get)
                .description("активные consumers")
                .register(registry);
    }

    public void incSentRecords() {
        sentCounter.increment();
    }

    public void incReceivedRecords(int count) {
        receivedCounter.increment(count);
    }

    public void incProcessedMessages(int count) {
        processedCounter.increment(count);
    }

    public void incErrorRecords() {
        errorsCounter.increment();
    }

    public void incrementProducers() {

        activeProducersCount.incrementAndGet();
    }

    public void decrementProducers() {
        activeProducersCount.decrementAndGet();

    }

    public void incrementConsumers() {
        activeConsumersCount.incrementAndGet();

    }
    public void decrementConsumers() {

        activeConsumersCount.decrementAndGet();

    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);

    }

    public void stopTimer(Timer.Sample sample) {

        sample.stop(processingTimer);

    }

    public Map<String, Object> getKafkaProducerMetrics() {
        Map<String, Object> result = new HashMap<>();
        kafkaTemplate.metrics().forEach((metricName, metric) -> {
            String key = metricName.name();
            if (metricName.tags().containsKey("node-id")) {
                key = "node-" + metricName.tags().get("node-id") + "." + key;
            }
            result.put(key, metric.metricValue());
        });
        return result;
    }

    public Map<String, Object> getKafkaConsumerMetrics() {
        Map<String, Object> result = new HashMap<>();

        registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("kafka.consumer"))
                .forEach(meter -> {

                    String tags = meter.getId().getTags().stream()
                            .map(tag -> tag.getKey() + "=" + tag.getValue())
                            .collect(Collectors.joining(","));

                    String fullName = meter.getId().getName() + "{" + tags + "}";


                    double value = meter.measure().iterator().next().getValue();
                    result.put(fullName, value);
                });

        return result;
    }
}
