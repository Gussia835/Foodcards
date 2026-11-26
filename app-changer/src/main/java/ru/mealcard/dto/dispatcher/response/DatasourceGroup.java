package ru.mealcard.dto.dispatcher.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DatasourceGroup {
    @JsonProperty("jdbc_url")
    private String jdbcUrl;

    @JsonProperty("account")
    private List<String> account;
}
