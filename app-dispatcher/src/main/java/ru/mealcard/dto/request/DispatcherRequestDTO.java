package ru.mealcard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispatcherRequestDTO {
    @NotNull(message = "Поле 'data' обязательно")
    private RequestData data;
}
