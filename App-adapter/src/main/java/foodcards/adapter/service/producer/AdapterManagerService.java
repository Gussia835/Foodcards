package foodcards.adapter.service.producer;

import foodcards.adapter.config.GruKafkaConsumer;
import foodcards.adapter.dao.AppAdapterDAO;
import foodcards.adapter.models.AppAdapterConfigEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterManagerService {

    private final ObjectProvider<GruKafkaProducer> producerProvider;
    private final ObjectProvider<GruKafkaConsumer> consumerProvider;
    private final AppAdapterDAO dao;

    @Value("${KAFKA_CONCURRENCY:5}")
    private int defaultConcurrency;

    private final Map<Long, AppAdapterConfigEntity> activeConfigs = new ConcurrentHashMap<>();
    private final Map<Long, GruKafkaProducer> activeProducers = new ConcurrentHashMap<>();
    private final Map<Long, GruKafkaConsumer> activeConsumers = new ConcurrentHashMap<>();

    private final AtomicInteger workerCounter = new AtomicInteger(0);

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
        GruKafkaProducer newWorker = producerProvider.getObject();
        activeProducers.put(id, newWorker);
        newWorker.start(config);
        log.info("Producer запущен для config ID={}", id);
    }

    public void stopProducer(Long id) {
        GruKafkaProducer worker = activeProducers.remove(id);
        if (worker != null) {
            worker.shutDown();
            log.info("Producer остановлен для config ID={}", id);
        }
    }

    public void startConsumer(AppAdapterConfigEntity config) {
        Long id = config.getId();
        if (activeConsumers.containsKey(id)) {
            stopConsumer(id);
        }
        GruKafkaConsumer newConsumer = consumerProvider.getObject();
        activeConsumers.put(id, newConsumer);
        newConsumer.start(id, config.getTopicIn(), "app-adapter-group", defaultConcurrency);
        log.info("Consumer запущен для config ID={}", id);
    }

    public void stopConsumer(Long id) {
        GruKafkaConsumer consumer = activeConsumers.remove(id);
        if (consumer != null) {
            consumer.stop();
            log.info("Consumer остановлен для config ID={}", id);
        }
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

    public int getActiveProducersCount() {
        return activeProducers.size();

    }
    public int getActiveConsumersCount() {
        return activeConsumers.size();

    }
}
