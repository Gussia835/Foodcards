package ru.mealcard.dto.dispatcher.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErrorDetails {
    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;
}
