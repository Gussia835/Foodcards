package ru.mealcard.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mealcard.dto.kafka.GruIncomingDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomingMessageCommand {
    private String protocolType;
    private String rawJson;
    private GruIncomingDTO payload;
}
