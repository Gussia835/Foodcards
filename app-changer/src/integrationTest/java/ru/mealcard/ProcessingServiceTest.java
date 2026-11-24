//package ru.mealcard;
//
//import io.quarkus.test.InjectMock;
//import io.quarkus.test.junit.QuarkusTest;
//import jakarta.inject.Inject;
//import ru.mealcard.dao.AppChangerDao;
//import ru.mealcard.dto.entity.TransactionUpdateDTO;
//import ru.mealcard.models.AppChangerTransEntity;
//import ru.mealcard.processor.MessageProcessor;
//import ru.mealcard.processor.ProcessorRegister;
//import ru.mealcard.repository.TransRepository;
//import ru.mealcard.service.processing.ProcessingService;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@QuarkusTest
//public class ProcessingServiceTest {
//    @InjectMock
//    TransRepository transRepository;
//
//    @InjectMock
//    ProcessorRegister processorRegistry;
//
//    @InjectMock
//    MessageProcessor messageProcessor;
//
//    @InjectMock
//    AppChangerDao dao;
//
//    @Inject
//    ProcessingService processingService;
//
//
//    @Test
//    void changesStatusToDoneTest() {
//        AppChangerTransEntity trans = new AppChangerTransEntity();
//        trans.id = 1L;
//        trans.setSystemId("GRU");
//        trans.setReqData("{\"test\":\"data\"}");
//        trans.setStatus("NEW");
//
//        when(transRepository.findNewTransactions(anyInt())).thenReturn(List.of(trans));
//        when(processorRegistry.getProcessor("GRU")).thenReturn(messageProcessor);
//        when(messageProcessor.process(anyString(), eq(1L)))
//                .thenReturn(TransactionUpdateDTO.builder().transId(1L).status("DONE").respData("{}").build());
//
//        processingService.processPending();
//
//        verify(dao).markTransProcessing(trans);
//        verify(dao).updateAfterProcessing(any(TransactionUpdateDTO.class));
//        verify(dao, never()).markTransAsError(any(), any());
//    }
//
//    @Test
//    void marksAsErrorTest() {
//        AppChangerTransEntity trans = new AppChangerTransEntity();
//        trans.id = 2L;
//        trans.setSystemId("GRU");
//        trans.setReqData("{\"test\":\"data\"}");
//
//        when(transRepository.findNewTransactions(anyInt())).thenReturn(List.of(trans));
//        when(processorRegistry.getProcessor("GRU")).thenReturn(messageProcessor);
//        when(messageProcessor.process(anyString(), eq(2L))).thenThrow(new RuntimeException("Procedure failed"));
//
//        processingService.processPending();
//
//        verify(dao).markTransProcessing(trans);
//        verify(dao).markTransAsError(eq(trans), any(RuntimeException.class));
//        verify(dao, never()).updateAfterProcessing(any());
//    }
//}
//