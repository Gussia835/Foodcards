package ru.mealcard.factory.dto;

import net.datafaker.Faker;
import ru.mealcard.dto.internal.BalanceChange;
import ru.mealcard.dto.internal.ProcedureResult;
import ru.mealcard.dto.kafka.GruEventDTO;
import ru.mealcard.dto.kafka.GruIncomingDTO;
import ru.mealcard.dto.kafka.SmartEventDTO;
import ru.mealcard.dto.kafka.SmartOutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmartDtoFactoryTest {

    private SmartDtoFactory factory;
    private Faker faker;

    @BeforeEach
    void setUp() {
        factory = new SmartDtoFactory();
        faker = new Faker();
    }

    @ParameterizedTest(name = "operation {0}")
    @CsvSource({
            "CR, 1000.00, 1500.00",
            "DR, 1000.00, 500.00",
            "ZR, 1000.00, 0",
            "XX, 1000.00, 1000.00"
    })
    void buildSmartDtoOperationTest(String op, String oldTbal, String expected) {
        GruEventDTO event = createEvent(op);
        ProcedureResult result = createResult(op, oldTbal, expected);

        SmartEventDTO dto = factory.buildSmartEvent(event, result);

        if ("XX".equals(op)) {
            assertEquals("ERROR", dto.getStatus());
            assertEquals("-2", dto.getError().getCode());
            assertNull(dto.getData());
        } else {
            assertEquals("SUCCESS", dto.getStatus());
            assertEquals(expected, dto.getData().getNewTbal());
            assertNull(dto.getError());
        }
    }

    @Test
    void buildSmartOutDTO_emptyEvents() {
        GruIncomingDTO incoming = new GruIncomingDTO();
        incoming.setRequestId("req-empty");

        SmartOutDTO response = factory.buildSmartOutDTO(incoming, Collections.emptyList());

        assertEquals(0, response.getEvents().size());
        assertEquals("req-empty", response.getRequestId());
        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void buildSmartOutDTO_multipleEvents() {
        GruIncomingDTO incoming = new GruIncomingDTO();
        incoming.setRequestId("req-multi");
        incoming.setEventType("BALANCE");

        List<SmartEventDTO> events = List.of(
                createSuccessEvent(),
                createSuccessEvent(),
                createSuccessEvent()
        );

        SmartOutDTO response = factory.buildSmartOutDTO(incoming, events);

        assertEquals(3, response.getEvents().size());
        assertEquals("req-multi", response.getRequestId());
    }

    private GruEventDTO createEvent(String operation) {
        GruEventDTO event = new GruEventDTO();
        event.setId("42");  // ← String, как в вашем DTO
        event.setSystemAccount(faker.number().digits(16));
        event.setOperation(operation);
        event.setXalfa("500.00");
        event.setCurrency("222");
        return event;
    }

    private ProcedureResult createResult(String operation, String oldTbal, String newTbal) {
        ProcedureResult result = new ProcedureResult();
        boolean unknown = "XX".equals(operation);
        result.setCode(unknown ? -2 : 0);
        result.setMessage(unknown ? "Unknown operation: XX" : null);

        if (unknown) {
            result.setChanges(Collections.emptyList());
        } else {
            BalanceChange change = new BalanceChange();
            change.setAccount("123456789");
            change.setOldTbal(oldTbal);
            change.setNewTbal(newTbal);
            change.setOperation(operation);
            result.setChanges(List.of(change));
        }
        return result;
    }

    private SmartEventDTO createSuccessEvent() {
        GruEventDTO event = createEvent("CR");
        ProcedureResult result = createResult("CR", "1000.00", "1500.00");
        return factory.buildSmartEvent(event, result);
    }
}