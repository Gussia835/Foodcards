package foodcards.adapter.service.producer;

import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.entityProcessor.EntityProcessor;
import foodcards.adapter.utils.TimeParserUtil;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static foodcards.adapter.utils.Constants.*;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class KafkaProducerWorker {
    private final TimeParserUtil timeParser;

    private EntityProcessor processor;
    private AppAdapterConfigEntity config;
    private long sleepTimeMs;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private Future<?> executionFuture;

    public void start(AppAdapterConfigEntity config, String sleepTimeConfig, EntityProcessor processor) {
        this.config = config;
        this.sleepTimeMs = timeParser.parseToMillis(sleepTimeConfig);
        this.isRunning.set(true);
        this.processor = processor;

        log.info("Запуск Producer-воркера для config ID={}, задержка {} мс", config.getId(), sleepTimeMs);
    }

    public void setExecutionFuture(Future<?> executionFuture) {
        this.executionFuture = executionFuture;
    }

    public Runnable createTask() {
        return () -> {
            log.info("Поток {} начал работу для config ID={}", Thread.currentThread().getName(), config.getId());

            while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    boolean hasData = processor.processOutboundBatch(config);
                    if (!hasData) {
                        Thread.sleep(sleepTimeMs);
                    }
                } catch (InterruptedException e) {
                    log.info("Поток для config ID={} был прерван. Завершение.", config.getId());
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Ошибка в цикле обработки для config ID={}. Повтор через 5 сек.", config.getId(), e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            log.info("Поток для config ID={} успешно завершен.", config.getId());
        };
    }

    @PreDestroy
    public void shutDown() {
        log.info("Завершаем поток");
        this.isRunning.set(false);

        if (executionFuture != null) {
            executionFuture.cancel(true);
        }
    }
}