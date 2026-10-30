package foodcards.adapter.controller;

import foodcards.adapter.dao.AdapterDao;
import foodcards.adapter.service.metrics.MetricsService;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.AdapterManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminRestController {

    private final AdapterDao dao;
    private final AdapterManagerService adapterManager;
    private final MetricsService metricsService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("adapter_transactions", dao.getTransactionStats());
        response.put("active_producers", metricsService.getActiveProducersCount());
        response.put("active_consumers", metricsService.getActiveConsumersCount());
        response.put("service_status", "RUNNING");
        return response;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {

        TimeUnit timeUnit = metricsService.getProcessingTimer().baseTimeUnit();

        Map<String, Object> response = Map.of(
                "sent messages", metricsService.getSentCounter().count(),
                "received messages", metricsService.getReceivedCounter().count(),
                "processed messages", metricsService.getProcessedCounter().count(),
                "error messages", metricsService.getErrorsCounter().count(),
                "active producers", metricsService.getActiveProducersCount(),
                "active consumers", metricsService.getActiveConsumersCount(),

                "mean processing time", metricsService.getProcessingTimer().mean(timeUnit),
                "total processing time", metricsService.getProcessingTimer().totalTime(timeUnit)
        );

        return response;
    }

    @GetMapping("/metrics/kafka")
    public Map<String, Object> getKafkaMetrics() {
        return Map.of(
                "producers_metrics", metricsService.getKafkaProducerMetrics(),
                "consumer_metrics", metricsService.getKafkaConsumerMetrics()
        );
    }

    @PostMapping("/restart/{id}")
    public ResponseEntity<Map<String, String>> restartConfig(@PathVariable Long id) {
        AppAdapterConfigEntity config = dao.findConfigById(id);
        if (config != null) {
            adapterManager.restartConfig(config);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Конфигурация ID=" + id + " перезапущена"));
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/stop/{id}")
    public ResponseEntity<Map<String, String>> stopConfig(@PathVariable Long id) {
        adapterManager.stopConfig(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Конфигурация ID=" + id + " остановлена"));
    }

    @PostMapping("/configs")
    public ResponseEntity<Map<String, String>> addConfig(@RequestBody AppAdapterConfigEntity config) {
        try {
            adapterManager.addConfig(config);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Конфигурация успешно добавлена и запущена"));
        } catch (Exception e) {
            log.error("Ошибка при добавлении конфигурации", e);
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }

    }
}
