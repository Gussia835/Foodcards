package foodcards.adapter.service;

import foodcards.adapter.config.GruKafkaConsumer;
import foodcards.adapter.dao.AdapterDao;
import foodcards.adapter.service.metrics.MetricsService;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.entityProcessor.EntityProcessor;
import foodcards.adapter.service.producer.KafkaProducerWorker;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterManagerService {

    private final ObjectProvider<KafkaProducerWorker> producerProvider;
    private final ObjectProvider<GruKafkaConsumer> consumerProvider;
    private final AdapterDao dao;
    private final MetricsService metricsService;

    private final ExecutorService producerExecutor = Executors.newFixedThreadPool(50, r -> {
        Thread t = new Thread(r);
        t.setName("Producer-Worker-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    @Value("${KAFKA_CONCURRENCY:5}")
    private int defaultConcurrency;

    @Value("${app.kafka.sleep-time-ms:5000}")
    private String sleepTimeConfig;

    private final Map<Long, AppAdapterConfigEntity> activeConfigs = new ConcurrentHashMap<>();
    private final Map<Long, KafkaProducerWorker> activeProducers = new ConcurrentHashMap<>();
    private final Map<Long, GruKafkaConsumer> activeConsumers = new ConcurrentHashMap<>();

    private final ApplicationContext context;

    public int getActiveProducersCount() {
        return metricsService.getActiveProducersCount().get();
    }

    public int getActiveConsumersCount() {
        return metricsService.getActiveConsumersCount().get();
    }

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

        KafkaProducerWorker newWorker = producerProvider.getObject();
        activeProducers.put(id, newWorker);

        newWorker.start(config, sleepTimeConfig, processor);

        Future<?> future = producerExecutor.submit(newWorker.createTask());
        newWorker.setExecutionFuture(future);

        activeProducers.put(id, newWorker);
        metricsService.incrementProducers();

        log.info("Producer запущен для config ID={}", id);
    }

    public void stopProducer(Long id) {
        KafkaProducerWorker worker = activeProducers.remove(id);
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

    @PreDestroy
    public void shutdownAll() {
        log.info("Остановка всех Producer воркеров...");
        activeProducers.keySet().forEach(this::stopProducer);
        producerExecutor.shutdown();
        try {
            if (!producerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                producerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            producerExecutor.shutdownNow();
        }
    }

}
