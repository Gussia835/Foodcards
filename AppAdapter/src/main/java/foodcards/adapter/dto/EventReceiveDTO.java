package foodcards.adapter.dto;

import foodcards.adapter.utils.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventReceiveDTO {
    private String entityValue;
    private String entityId;
    private DataSuccessDTO data;
    private Status status;
    private DataErrorDTO error;
}
