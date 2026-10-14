package com.smartwater.service;

import com.smartwater.dto.CreateWaterUsageRequest;
import com.smartwater.dto.CsvRowError;
import com.smartwater.dto.CsvUploadSummaryDto;
import com.smartwater.dto.WaterUsageLogDto;
import com.smartwater.entity.WaterSource;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.InvalidReadingException;
import com.smartwater.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvUploadService {

    private final WaterUsageService waterUsageService;

    public CsvUploadSummaryDto processCsvFile(MultipartFile file) {
        List<CsvRowError> errors = new ArrayList<>();
        List<WaterUsageLogDto> createdLogs = new ArrayList<>();
        int totalRows = 0;
        int successfulRows = 0;
        int failedRows = 0;
        int duplicateRows = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("meter_number", "reading_date", "current_reading")
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            int rowNum = 1; // 1-based data row after header
            for (CSVRecord record : csvParser) {
                rowNum++;
                totalRows++;

                String meterNumber = record.isMapped("meter_number") ? record.get("meter_number") : record.get(0);
                String readingDateStr = record.isMapped("reading_date") ? record.get("reading_date") : record.get(1);
                String currentReadingStr = record.isMapped("current_reading") ? record.get("current_reading") : record.get(2);

                if (meterNumber == null || meterNumber.isBlank()) {
                    failedRows++;
                    errors.add(new CsvRowError(rowNum, meterNumber, readingDateStr, currentReadingStr, "Meter number is missing"));
                    continue;
                }

                LocalDate readingDate;
                try {
                    readingDate = LocalDate.parse(readingDateStr.trim());
                } catch (DateTimeParseException | NullPointerException e) {
                    failedRows++;
                    errors.add(new CsvRowError(rowNum, meterNumber, readingDateStr, currentReadingStr, "Invalid reading date format. Expected YYYY-MM-DD"));
                    continue;
                }

                BigDecimal currentReading;
                try {
                    currentReading = new BigDecimal(currentReadingStr.trim());
                } catch (NumberFormatException | NullPointerException e) {
                    failedRows++;
                    errors.add(new CsvRowError(rowNum, meterNumber, readingDateStr, currentReadingStr, "Invalid numerical current reading: " + currentReadingStr));
                    continue;
                }

                CreateWaterUsageRequest request = CreateWaterUsageRequest.builder()
                        .meterNumber(meterNumber.trim())
                        .readingDate(readingDate)
                        .currentReading(currentReading)
                        .source(WaterSource.CSV)
                        .build();

                try {
                    WaterUsageLogDto createdLog = waterUsageService.recordWaterUsage(request);
                    createdLogs.add(createdLog);
                    successfulRows++;
                } catch (DuplicateResourceException e) {
                    duplicateRows++;
                    failedRows++;
                    errors.add(new CsvRowError(rowNum, meterNumber, readingDateStr, currentReadingStr, e.getMessage()));
                } catch (ResourceNotFoundException | InvalidReadingException e) {
                    failedRows++;
                    errors.add(new CsvRowError(rowNum, meterNumber, readingDateStr, currentReadingStr, e.getMessage()));
                } catch (Exception e) {
                    failedRows++;
                    errors.add(new CsvRowError(rowNum, meterNumber, readingDateStr, currentReadingStr, "Unexpected error: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        return CsvUploadSummaryDto.builder()
                .totalRows(totalRows)
                .successfulRows(successfulRows)
                .failedRows(failedRows)
                .duplicateRows(duplicateRows)
                .errors(errors)
                .createdLogs(createdLogs)
                .build();
    }
}
