package ru.mealcard.dto.dispatcher.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DispatcherRequestDTO {
    @JsonProperty("data")
    private RequestData data;
}
