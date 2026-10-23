package foodcards.adapter.service;

import foodcards.adapter.mapper.AppAdapterConfigMapper;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.producer.AdapterManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigRunner implements CommandLineRunner {
    private final AdapterManagerService adapterManager;
    private final AppAdapterConfigMapper configMapper;

    @Override
    public void run(String... args) {
        log.info("Начинается работа потоков");
        List<AppAdapterConfigEntity> configs = configMapper.findAll();

        for (AppAdapterConfigEntity config : configs) {
            if (isActive(config)) {
                adapterManager.startConfig(config);
            }
        }

        log.info("Инициализация заверешна. Producer: {}, Consumer: {}",
                                        adapterManager.getActiveProducersCount(),
                                        adapterManager.getActiveConsumersCount());
    }

    private boolean isActive(AppAdapterConfigEntity config) {
        return (config.getStatusOut() != null && config.getStatusOut() > 0) ||
                (config.getStatusIn() != null && config.getStatusIn() > 0);
    }
}