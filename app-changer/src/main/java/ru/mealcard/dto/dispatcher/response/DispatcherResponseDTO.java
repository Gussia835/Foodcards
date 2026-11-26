package ru.mealcard.dto.dispatcher.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import ru.mealcard.dto.response.ResponseData;

@Data
public class DispatcherResponseDTO {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("systemId")
    private String systemId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error")
    private ErrorDetails error;

    @JsonProperty("data")
    private ResponseData data;
}
