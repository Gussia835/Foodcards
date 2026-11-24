// package ru.mealcard;
//
// import io.quarkus.test.InjectMock;
// import io.quarkus.test.junit.QuarkusTest;
// import jakarta.inject.Inject;
// import ru.mealcard.controller.ControllerClient;
// import ru.mealcard.dao.AppChangerDao;
// import ru.mealcard.dto.internal.IncomingMessageCommand;
// import ru.mealcard.dto.internal.RoutingInfo;
// import ru.mealcard.dto.kafka.GruEventDTO;
// import ru.mealcard.dto.kafka.GruIncomingDTO;
// import ru.mealcard.models.AppChangerTransEntity;
// import ru.mealcard.repository.TransRepository;
// import ru.mealcard.service.processing.ProcessingService;
// import org.junit.jupiter.api.Test;
//
// import java.util.List;
//
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.when;
//
// @QuarkusTest
// class IntegrationTest {
//
//     @InjectMock
//     ControllerClient controllerClient;
//
//     @Inject
//     AppChangerDao dao;
//
//     @Inject
//     TransRepository transRepository;
//
//     @Inject
//     ProcessingService processingService;
//
//     @Test
//     void successSend() {
//         RoutingInfo routing = new RoutingInfo();
//         when(controllerClient.getRouting(anyString())).thenReturn(routing);
//
//         GruEventDTO event = new GruEventDTO();
//         event.setId("1");
//         event.setSystemAccount("111222333");
//         event.setOperation("CR");
//         event.setXalfa("500.00");
//         event.setCurrency("222");
//
//         GruIncomingDTO incoming = new GruIncomingDTO();
//         incoming.setRequestId("req-e2e-001");
//         incoming.setSystemId("GRU");
//         incoming.setEventType("BALANCE");
//         incoming.setEntityType("ACCOUNT");
//         incoming.setEvents(List.of(event));
//
//         String validJson = """
//                 {
//                   "requestId": "req-e2e-001",
//                   "systemId": "GRU",
//                   "eventType": "BALANCE",
//                   "entityType": "ACCOUNT",
//                   "events": [{
//                     "id": "1",
//                     "systemAccount": "111222333",
//                     "operation": "CR",
//                     "xalfa": "500.00",
//                     "currency": "222"
//                   }]
//                 }
//                 """;
//
//         dao.saveIncomning(new IncomingMessageCommand("KAFKA", validJson, incoming));
//
//         AppChangerTransEntity saved = transRepository.findByRequestId("req-e2e-001");
//         assertNotNull(saved, "Запись должна быть сохранена в БД");
//         assertEquals("NEW", saved.getStatus());
//
//         processingService.processPending();
//
//         AppChangerTransEntity processed = transRepository.findByRequestId("req-e2e-001");
//
//         assertEquals("DONE", processed.getStatus(), "Статус должен стать DONE");
//         assertNotNull(processed.getRespData());
//         assertTrue(processed.getRespData().contains("SUCCESS"));
//         assertTrue(processed.getRespData().contains("1500.00"), "Баланс должен быть 1500.00");
//     }
// }