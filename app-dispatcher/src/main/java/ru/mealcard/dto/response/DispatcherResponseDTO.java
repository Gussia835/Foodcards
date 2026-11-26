package ru.mealcard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
