package ru.mealcard.dto.dispatcher.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestData {
    @JsonProperty("systemId")
    private String systemId;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("account")
    private List<String> accounts;
}
