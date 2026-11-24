package ru.mealcard.dao;

//import io.quarkus.test.junit.QuarkusTest;
//import jakarta.inject.Inject;
//import net.datafaker.Faker;
//import ru.mealcard.dto.internal.IncomingMessageCommand;
//import ru.mealcard.dto.kafka.GruEventDTO;
//import ru.mealcard.dto.kafka.GruIncomingDTO;
//import ru.mealcard.models.AppChangerIoMsgsEntity;
//import ru.mealcard.models.AppChangerTransEntity;
//import ru.mealcard.repository.IoMsgsRepository;
//import ru.mealcard.repository.TransRepository;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@QuarkusTest
//public class AppChangerDaoIntegrationTest {
//    @Inject
//    AppChangerDao appChangerDao;
//
//    @Inject
//    TransRepository transRepository;
//
//    @Inject
//    IoMsgsRepository ioMsgsRepository;
//
//    Faker faker = new Faker();
//
//    @Test
//    void saveTransAndIoTest() {
//        GruIncomingDTO incoming = GruIncomingDTO.builder()
//                                    .requestId("req-integration-001")
//                                    .systemId("GRU")
//                                    .eventType("BALANCE")
//                                    .entityType("ACCOUNT")
//                                    .build();
//
//        GruEventDTO eventDTO = GruEventDTO.builder()
//                                .id("123")
//                                .systemAccount(faker.number().digits(16))
//                                .operation("CR")
//                                .xalfa("500")
//                                .currency("222")
//                                .build();
//        incoming.setEvents(List.of(eventDTO));
//
//        String rawJson = "{\"requestId\":\"req-integration-001\", \"systemId\":\"GRU\"}";
//        IncomingMessageCommand command = new IncomingMessageCommand("KAFKA", rawJson, incoming);
//
//        appChangerDao.saveIncomning(command);
//
//        AppChangerTransEntity savedTrans = transRepository.findByRequestId("req-integration-001");
//        assertNotNull(savedTrans);
//        assertEquals("NEW", savedTrans.getStatus());
//        assertEquals("GRU", savedTrans.getSystemId());
//        assertEquals("req-integration-001", savedTrans.getRequestId());
//
//        List<AppChangerIoMsgsEntity> ioMsgs = ioMsgsRepository.findByTransId(savedTrans.id);
//        assertNotNull(ioMsgs);
//        assertEquals(1, ioMsgs.size(), "Должна быть ровно одна запись в логе IO_MSGS");
//        assertEquals("KAFKA", ioMsgs.get(0).getProtocolType());
//        assertEquals("IN", ioMsgs.get(0).getDir());
//
//    }
//
//}
//