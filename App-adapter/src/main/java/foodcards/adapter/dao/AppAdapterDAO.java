package foodcards.adapter.dao;

import foodcards.adapter.mapper.*;
import foodcards.adapter.mapper.dto.VistaUpdateDTO;
import foodcards.adapter.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static foodcards.adapter.utils.Constants.*;

@Repository
@RequiredArgsConstructor
public class AppAdapterDAO {
    private final GruVistaTabMapper gruVistaTabMapper;
    private final AppAdapterTransMapper transMapper;
    private final AppAdapterIoMsgsMapper ioMsgsMapper;
    private final GruRejectTabMapper rejectTabMapper;
    private final AppAdapterConfigMapper configMapper;

    @Value("${app.kafka.batch-size:100}")
    public int BATCH_SIZE;

    /*
    gruVistaTab: Получение ожидающих обработки из GruVistaTab
    */
    @Transactional
    public List<GruVistaTabEntity> getWaiting(int batchSize) {
        List<GruVistaTabEntity> records = gruVistaTabMapper.selectWaitingRecords(BATCH_SIZE);

        if (records != null && !records.isEmpty()) {
            List<Long> ids = records.stream().map(GruVistaTabEntity::getId).toList();
            gruVistaTabMapper.updateStatusToProgress(ids);
        }
        return records;
    }

    /*
    trans и IO: сохранение в таблицу trans и IOMsgs
    */
    @Transactional
    public void saveOutboundAudit(AppAdapterTransEntity trans, AppAdapterIoMsgsEntity ioMsg) {
        transMapper.insert(trans);
        ioMsg.setTransId(trans.getId());
        ioMsgsMapper.insert(ioMsg);
    }

    /*
    trans: изменение статуса на SEND_TO_KAFKA
    */
    @Transactional
    public void markTransactionAsSentToKafka(String requestId) {
        AppAdapterTransEntity trans = transMapper.findByRequestId(requestId);
        if (trans != null) {
            transMapper.updateStatus(trans.getId(), STATUS_SENT_TO_KAFKA);
        }
    }

    /*
    trans: изменение статуса на PROCESSED
    */
    @Transactional
    public void markTransactionAsProcessed(String requestId) {
        AppAdapterTransEntity trans = transMapper.findByRequestId(requestId);
        if (trans != null) {
            transMapper.updateStatus(trans.getId(), STATUS_PROCESSED);
        }
    }

    /*
    IoMsgs: запись в таблицу Io лога
    */
    @Transactional
    public void saveInboundAudit(AppAdapterIoMsgsEntity ioMsg) {
        ioMsgsMapper.insert(ioMsg);
    }

    /*
    trans: запись по id
    */
    public AppAdapterTransEntity findTransByRequestId(String requestId) {
        return transMapper.findByRequestId(requestId);
    }

    /*
    trans: получение ифнормации о записях
    */
    public Map<String, Integer> getTransactionStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("WAIT", transMapper.countByStatus(STATUS_WAIT));
        stats.put("PROGRESS", transMapper.countByStatus(STATUS_PROGRESS));
        stats.put("SENT_TO_KAFKA", transMapper.countByStatus(STATUS_SENT_TO_KAFKA));
        stats.put("PROCESSED", transMapper.countByStatus(STATUS_PROCESSED));
        stats.put("ERROR", transMapper.countByStatus(STATUS_ERROR));
        return stats;
    }

    /*
     Config: Поиск конфигурации
     */
    public AppAdapterConfigEntity findConfigById(Long id) {
        return configMapper.findById(id);
    }

    /*
    Config: добавление кофнигурации
    */
    @Transactional
    public void addConfig(AppAdapterConfigEntity config) {
        configMapper.insert(config);
    }

    /*
    GruVistaTab: обнволение статуса на success
    */
    @Transactional()
    public void updateVistaTabSuccess(List<VistaUpdateDTO> updates) {
        if (updates != null && !updates.isEmpty()) {
            gruVistaTabMapper.batchUpdateSuccess(updates);
        }
    }

    /*
    GruVistaTab: обнволение статуса на error
    */
    @Transactional
    public void updateVistaTabToError(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            gruVistaTabMapper.batchUpdateToError(ids);
        }
    }

    /*
    reject: Добавление записи в таблицу ошибок
    */
    @Transactional
    public void saveRejects(List<GruRejectTabEntity> rejects) {
        if (rejects != null && !rejects.isEmpty()) {
            rejectTabMapper.batchInsert(rejects);
        }
    }


}