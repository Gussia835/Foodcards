package ru.mealcard.dao;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.mealcard.dto.internal.IncomingMessageCommand;
import ru.mealcard.dto.entity.IoLogDTO;
import ru.mealcard.dto.entity.TransactionUpdateDTO;
import ru.mealcard.factory.dto.IoMsgsDtoFactory;
import ru.mealcard.factory.entity.IoMsgsFactory;
import ru.mealcard.factory.entity.TransEntityFactory;
import ru.mealcard.models.AppChangerIoMsgsEntity;
import ru.mealcard.models.AppChangerTransEntity;
import ru.mealcard.repository.IoMsgsRepository;
import ru.mealcard.repository.TransRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;

import static ru.mealcard.utils.Constants.*;

@ApplicationScoped
@RequiredArgsConstructor
public class AppChangerDao {

    private final IoMsgsRepository ioMsgsRepository;
    private final TransRepository transRepository;

    private final TransEntityFactory transFactory;
    private final IoMsgsFactory ioMsgsFactory;
    private final IoMsgsDtoFactory ioMsgsDtoFactory;

    @ConfigProperty(name = "app.node-id")
    String nodeId;

    /*
    Сохранение входящего
     */
    @Transactional
    public void saveIncomning(IncomingMessageCommand command) {
        AppChangerTransEntity trans = transFactory.buildTrans(command);
        transRepository.save(trans);

        IoLogDTO logDto = ioMsgsDtoFactory.buildIoLog(trans, command.getRawJson(), nodeId);
        AppChangerIoMsgsEntity ioMsg = ioMsgsFactory.buildIoMsg(logDto);
        ioMsgsRepository.save(ioMsg);
    }

    /*
    Частичное обновление
     */
    @Transactional
    public void updateAfterProcessing(TransactionUpdateDTO dto) {
        AppChangerTransEntity trans = transRepository.findById(dto.getTransId());

        if (trans == null) {
            throw new IllegalArgumentException("Transaction not found: " + dto.getTransId());
        }

        trans.setStatus(dto.getStatus());
        trans.setRespData(dto.getRespData());
        trans.setRespCode(dto.getRespCode());
        trans.setRespDesc(dto.getRespDesc());
    }

    /*
    Даем статус Progress записям
     */
    @Transactional
    public void markTransProcessing(AppChangerTransEntity trans) {
        trans.setStatus(STATUS_PROCESSING);
    }


    @Transactional
    public void markAsSent(Long transId) {
        AppChangerTransEntity trans = transRepository.findById(transId);

        if (trans == null) {
            throw new IllegalArgumentException("Transaction not found: " + transId);
        }

        trans.setStatus(STATUS_SENT);
        trans.setUpdTs(LocalDateTime.now());

        AppChangerIoMsgsEntity ioMsg = AppChangerIoMsgsEntity.builder()
                .transId(trans.getId())
                .protocolType(trans.getProtocolType())
                .msgType("SMART")
                .dir(DIR_OUT)
                .msg(trans.getRespData())
                .insTs(LocalDateTime.now())
                .nodeId(nodeId)
                .build();

        ioMsgsRepository.save(ioMsg);
    }

    /*
    Пометить ошибкой
     */
    @Transactional
    public void markTransAsError(AppChangerTransEntity trans, Exception e) {
        trans.setStatus(STATUS_ERROR);
        trans.setRespDesc(e.getMessage());
    }


}
