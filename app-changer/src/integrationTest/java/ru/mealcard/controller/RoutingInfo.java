package ru.mealcard.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingInfo {
    private String dbConnection;
    private String schemaName;
    private String procedureName;
}