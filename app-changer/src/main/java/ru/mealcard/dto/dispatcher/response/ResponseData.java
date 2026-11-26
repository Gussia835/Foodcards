package ru.mealcard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.mealcard.dto.dispatcher.response.DatasourceGroup;

import java.util.List;

@Data
public class ResponseData {
    @JsonProperty("datasource")
    private List<DatasourceGroup> datasource;
}
