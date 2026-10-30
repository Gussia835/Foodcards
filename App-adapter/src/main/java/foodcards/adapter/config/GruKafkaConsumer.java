package foodcards.adapter.config;

import foodcards.adapter.service.metrics.MetricsService;
import foodcards.adapter.service.consumer.KafkaConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class GruKafkaConsumer {
    private final ConcurrentKafkaListenerContainerFactory<String, String> factory;
    private ConcurrentMessageListenerContainer<String, String> container;
    private final KafkaConsumerService messageProcessor;
    private final MetricsService metricsService;
    private Long configId;


    public void start(Long configId, String topicIn, String groupId, int concurrency, String systemId) {
        if (container != null && container.isRunning()) {
            log.warn("Consumer для configId={} уже запущен", configId);
            return;
        }

        this.configId = configId;
        log.info("Сборка Consumer configId={} для топика: {}, concurrency: {}", configId, topicIn, concurrency);

        container = factory.createContainer(topicIn);
        container.setConcurrency(concurrency);
        container.setBeanName("kafka-consumer-" + configId);
        container.getContainerProperties().setGroupId(groupId);
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        container.setupMessageListener((BatchAcknowledgingMessageListener<String, String>) (records, acknowledgment) -> {
            List<String> jsonMessages = records.stream()
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());

            log.info("configId={} Получил батч размером {} сообщений", groupId, jsonMessages.size());

            try {
                messageProcessor.processMessages(jsonMessages, systemId);
                metricsService.incReceivedRecords(jsonMessages.size());

                acknowledgment.acknowledge();
                log.info("Consumer {} успешно обработал и подтвердил батч", configId);

            } catch (Exception e) {

                metricsService.incErrorRecords();
                log.error("Consumer {} упал. Offset НЕ подтвержден, сообщения вернутся в Kafka", configId, e);
                throw new RuntimeException("Ошибка обработки сообщений в Consumer", e);
            }
        });

        container.start();
        log.info("Consumer {} успешно запущен", configId);
    }

    public void stop() {
        if (container != null && container.isRunning()) {
            log.info("Остановка Consumer {}", configId);
            container.stop();
            container = null;
        }
    }
}
