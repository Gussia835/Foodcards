package foodcards.adapter.service.consumer;

import foodcards.adapter.builder.EntityBuilder;
import foodcards.adapter.dao.AdapterDao;
import foodcards.adapter.dto.EventReceiveDTO;
import foodcards.adapter.mapper.dto.VistaUpdateDTO;
import foodcards.adapter.models.GruRejectTabEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static foodcards.adapter.utils.Constants.STATUS_ERROR;


@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateStatusService {
    private final AdapterDao dao;
    private final EntityBuilder entityBuilder;

    public void processSuccessEvents(List<EventReceiveDTO> events) {

        if (events == null || events.isEmpty()) {
            log.warn("Получен пустой список событий. Весь батч в ERROR.");
            processErrorEvents(events);
            return;
        }

        boolean hasAnyError = events.stream().anyMatch(event -> STATUS_ERROR.equals(event.getStatus()));

        if (hasAnyError) {
            log.warn("Обнаружена ошибка в части записей патча. Согласно ТЗ, весь батч помечается как ERROR.");
            processErrorEvents(events);
            return;
        }
        List<VistaUpdateDTO> successUpdates = new ArrayList<>();
        for (var event : events) {
            if (event.getData() != null) {
                successUpdates.add(new VistaUpdateDTO(
                        Long.parseLong(event.getEntityId()),
                        new BigDecimal(event.getData().getNewTbal()),
                        new BigDecimal(event.getData().getOldTbal())
                ));
            }
        }

        if (!successUpdates.isEmpty()) {
            dao.updateVistaTabSuccess(successUpdates);
        }
    }

    public void processErrorEvents(List<EventReceiveDTO> events) {
        if (events == null || events.isEmpty()) return;

        List<Long> errorIds = new ArrayList<>();
        List<GruRejectTabEntity> rejects = new ArrayList<>();
        for (var event : events) {
            errorIds.add(Long.parseLong(event.getEntityId()));
            rejects.add(entityBuilder.buildRejectTab(event.getEntityId(), event.getEntityValue(), event.getError()));
        }
        dao.updateVistaTabToError(errorIds);
        dao.saveRejects(rejects);
    }
}