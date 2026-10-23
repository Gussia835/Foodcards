package foodcards.adapter.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VistaUpdateDTO {
    private Long id;
    private BigDecimal newTbal;
    private BigDecimal oldTbal;
}
