package com.smartwater.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvUploadSummaryDto {
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private int duplicateRows;
    @Builder.Default
    private List<CsvRowError> errors = new ArrayList<>();
    @Builder.Default
    private List<WaterUsageLogDto> createdLogs = new ArrayList<>();
}
