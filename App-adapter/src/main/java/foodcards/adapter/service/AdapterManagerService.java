package foodcards.adapter.service;

import foodcards.adapter.config.GruKafkaConsumer;
import foodcards.adapter.dao.AdapterDao;
import foodcards.adapter.service.metrics.MetricsService;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.entityProcessor.EntityProcessor;
import foodcards.adapter.service.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterManagerService {

    private final ObjectProvider<KafkaProducer> producerProvider;
    private final ObjectProvider<GruKafkaConsumer> consumerProvider;
    private final AdapterDao dao;
    private final MetricsService metricsService;


    @Value("${KAFKA_CONCURRENCY:5}")
    private int defaultConcurrency;

    private final Map<Long, AppAdapterConfigEntity> activeConfigs = new ConcurrentHashMap<>();
    private final Map<Long, KafkaProducer> activeProducers = new ConcurrentHashMap<>();
    private final Map<Long, GruKafkaConsumer> activeConsumers = new ConcurrentHashMap<>();

    private final ApplicationContext context;

    public void addConfig(AppAdapterConfigEntity config) {
        log.info("добавление конфигурации: {}", config);

        dao.addConfig(config);
        startConfig(config);
    }

    public void startProducer(AppAdapterConfigEntity config) {
        Long id = config.getId();

        activeConfigs.put(id, config);

        if (activeProducers.containsKey(id)) {
            stopProducer(id);
        }

        String beanName = config.getSystemId().toLowerCase() + "Processor";
        EntityProcessor processor = context.getBean(beanName, EntityProcessor.class);

        KafkaProducer newWorker = producerProvider.getObject();
        activeProducers.put(id, newWorker);

        newWorker.start(config, processor);

        metricsService.incrementProducers();

        log.info("Producer запущен для config ID={}", id);
    }

    public void stopProducer(Long id) {
        KafkaProducer worker = activeProducers.remove(id);
        if (worker != null) {
            worker.shutDown();
            log.info("Producer остановлен для config ID={}", id);
        }

        metricsService.decrementProducers();
    }

    public void startConsumer(AppAdapterConfigEntity config) {
        Long id = config.getId();
        if (activeConsumers.containsKey(id)) {
            stopConsumer(id);
        }
        GruKafkaConsumer newConsumer = consumerProvider.getObject();
        activeConsumers.put(id, newConsumer);
        newConsumer.start(id, config.getTopicIn(), "app-adapter-group", defaultConcurrency, config.getSystemId());

        metricsService.incrementConsumers();

        log.info("Consumer запущен для config ID={}", id);


    }

    public void stopConsumer(Long id) {
        GruKafkaConsumer consumer = activeConsumers.remove(id);
        if (consumer != null) {
            consumer.stop();
            log.info("Consumer остановлен для config ID={}", id);
        }

        metricsService.decrementConsumers();
    }

    public void startConfig(AppAdapterConfigEntity config) {
        if (config.getStatusOut() != null && config.getStatusOut() > 0) {
            startProducer(config);
        }
        if (config.getStatusIn() != null && config.getStatusIn() > 0) {
            startConsumer(config);
        }
    }

    public void stopConfig(Long id) {
        stopProducer(id);
        stopConsumer(id);
    }

    public void restartConfig(AppAdapterConfigEntity config) {
        log.info("Перезапуск конфигурации ID={}", config.getId());
        stopConfig(config.getId());
        try {

            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startConfig(config);
    }

    public void restartAllConfigs() {
        log.info("перезапуск всех конфигураций");

        activeConfigs.values().forEach(this::restartConfig);
    }

}
