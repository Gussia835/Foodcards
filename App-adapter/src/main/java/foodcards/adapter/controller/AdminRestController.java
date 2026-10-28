package foodcards.adapter.controller;

import foodcards.adapter.dao.AdapterDao;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.AdapterManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final AdapterDao dao;
    private final AdapterManagerService adapterManager;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("adapter_transactions", dao.getTransactionStats());
        response.put("active_producers", adapterManager.getActiveProducersCount());
        response.put("active_consumers", adapterManager.getActiveConsumersCount());
        response.put("service_status", "RUNNING");
        return response;
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
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }

    }
}
