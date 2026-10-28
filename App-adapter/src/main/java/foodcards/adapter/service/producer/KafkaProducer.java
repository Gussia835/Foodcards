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
import java.util.concurrent.atomic.AtomicBoolean;

import static foodcards.adapter.utils.Constants.*;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class KafkaProducer {
    private final TimeParserUtil timeParser;
    private EntityProcessor processor;


    @Value("${app.kafka.sleep-time-ms:" + DEFAULT_SLEEP_TIME + "}")
    private String sleepTimeConfig;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    @Async("workerTaskExecutor")
    public void start(AppAdapterConfigEntity config, EntityProcessor processor) {
        this.processor = processor;

        long sleepTime = timeParser.parseToMillis(sleepTimeConfig);

        while (isRunning.get()) {
            try {
                if (!processor.processOutboundBatch(config)) {
                    Thread.sleep(sleepTime);

                }
            } catch (InterruptedException e) {
                log.error("Поток {} прерван", config.getEntityType());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Ошибка в цикле {}", config.getEntityType(), e);
                try {
                    Thread.sleep(timeParser.parseToMillis(DEFAULT_SLEEP_TIME));

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                   break;
                }
            }
        }
    }

    @PreDestroy
    public void shutDown() {
        log.info("Завершаем поток");
        this.isRunning.set(false);
    }
}