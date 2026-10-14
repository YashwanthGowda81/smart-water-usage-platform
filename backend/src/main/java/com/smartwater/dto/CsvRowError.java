package com.smartwater.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvRowError {
    private int rowNumber;
    private String meterNumber;
    private String readingDate;
    private String rawCurrentReading;
    private String errorMessage;
}
