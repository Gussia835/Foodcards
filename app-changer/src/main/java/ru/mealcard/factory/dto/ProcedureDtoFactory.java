package ru.mealcard.factory.dto;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import ru.mealcard.dto.entity.TransactionUpdateDTO;
import ru.mealcard.dto.internal.BalanceChange;
import ru.mealcard.dto.internal.ProcedureParamsDTO;
import ru.mealcard.dto.internal.ProcedureResult;
import ru.mealcard.dto.kafka.GruEventDTO;
import ru.mealcard.dto.kafka.GruIncomingDTO;
import ru.mealcard.dto.kafka.SmartEventDTO;
import ru.mealcard.dto.kafka.SmartOutDTO;
import ru.mealcard.utils.JsonUtil;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ProcedureDtoFactory {

    private final JsonUtil jsonUtil;
    private final SmartDtoFactory smartDtoFactory;
    private final TransDtoFactory transDtoFactory;

    public TransactionUpdateDTO buildTransUpdateForResponse(Long transId,
                                                            GruIncomingDTO incomingDTO,
                                                            List<SmartEventDTO> responseEvents) {
        SmartOutDTO response = smartDtoFactory.buildSmartOutDTO(incomingDTO, responseEvents);
        String responseData = jsonUtil.toJson(response);
        TransactionUpdateDTO updateDto = transDtoFactory.buildTransactionUpdate(transId, responseData);

        return updateDto;

    }

    public ProcedureParamsDTO buildProcedureParams(GruEventDTO event, String dbConnection) {
        return ProcedureParamsDTO.builder()
                .systemAccount(event.getSystemAccount())
                .operation(event.getOperation())
                .amount(event.getXalfa())
                .currency(event.getCurrency())
                .dbConnection(dbConnection)
                .build();
    }

    public ProcedureResult buildProcedureResult(int code,
                                                String message,
                                                List<BalanceChange> changes) {

        return ProcedureResult.builder()
                .code(code)
                .message(message)
                .changes(changes)
                .build();
    }

}
