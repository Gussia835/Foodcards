package foodcards.adapter.service.consumer;

import foodcards.adapter.service.entityProcessor.EntityProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
   private final ApplicationContext context;

    public void processMessages(List<String> jsonMessages, String systemId) throws IOException {
        if (jsonMessages == null || jsonMessages.isEmpty()) {
            return;
        }

        String beanName = systemId.toLowerCase() + "Processor";
        EntityProcessor processor = context.getBean(beanName, EntityProcessor.class);

        processor.processInboundBatch(jsonMessages);
    }
}