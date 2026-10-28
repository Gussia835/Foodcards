package foodcards.adapter.service.entityProcessor;

import foodcards.adapter.dto.KafkaSendDTO;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.models.AppAdapterTransEntity;

import java.util.List;

public interface EntityProcessor {
    String getEntityType();

    boolean processOutboundBatch(AppAdapterConfigEntity config);
    void processInboundBatch(List<String> jsonMessages);
}
