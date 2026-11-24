package ru.mealcard.service.processing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import ru.mealcard.dto.internal.ProcedureParamsDTO;
import ru.mealcard.dto.internal.ProcedureResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcedureServiceTest {
    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    @InjectMocks
    ProcedureService service;

    private ProcedureParamsDTO params;

    @BeforeEach
    void setUp() {
        params = ProcedureParamsDTO.builder()
                .systemAccount("123456789")
                .operation("CR")
                .amount("500.00")
                .currency("222")
                .build();

        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    void callSuccess() {
        Object[] row = {"123456789", "1000.00", "1500.00", "CR", 0, null};
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(row));

        ProcedureResult result = service.call(params);

        assertEquals(0, result.getCode());
        assertNull(result.getMessage());
        assertEquals(1, result.getChanges().size());
        assertEquals("1500.00", result.getChanges().get(0).getNewTbal());
    }

    @Test
    void unknownOperationTest() {
        Object[] row = {"123456789", "1000.00", "1000.00", "XX", -2, "Unknown operation: XX"};
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(row));

        ProcedureResult result = service.call(params);

        assertEquals(-2, result.getCode());
        assertEquals("Unknown operation: XX", result.getMessage());
        assertTrue(result.getChanges().isEmpty());
    }

    @Test
    void emptyResultTest() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        ProcedureResult result = service.call(params);

        assertEquals(-1, result.getCode());
        assertEquals("Функция не вернула результатов", result.getMessage());
    }

    @Test
    void dbExceptionTest() {
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("Connection refused"));

        ProcedureResult result = service.call(params);

        assertEquals(-1, result.getCode());
        assertTrue(result.getMessage().contains("Database execution error"));
    }

    @Test
    void nullAmountTest() {
        params.setAmount(null);
        Object[] row = {"123456789", "0", "0", "CR", 0, null};
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(row));

        service.call(params);

        verify(query).setParameter("p_amount", new BigDecimal("0"));
    }
}
