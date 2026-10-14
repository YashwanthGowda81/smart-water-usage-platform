package com.smartwater;

import com.smartwater.dto.CsvUploadSummaryDto;
import com.smartwater.dto.WaterUsageLogDto;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.service.CsvUploadService;
import com.smartwater.service.WaterUsageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvUploadServiceTest {

    @Mock
    private WaterUsageService waterUsageService;

    @InjectMocks
    private CsvUploadService csvUploadService;

    @Test
    void processCsvFile_ValidAndDuplicateRows_ReturnsAccurateSummary() {
        String csvContent = """
                meter_number,reading_date,current_reading
                MTR-1001,2026-10-01,1212.50
                MTR-1002,2026-10-01,1534.20
                MTR-INVALID,2026-10-01,abc
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "readings.csv", "text/csv", csvContent.getBytes()
        );

        WaterUsageLogDto mockDto = WaterUsageLogDto.builder()
                .id(1L)
                .meterNumber("MTR-1001")
                .readingDate(LocalDate.of(2026, 10, 1))
                .currentReading(new BigDecimal("1212.50"))
                .build();

        when(waterUsageService.recordWaterUsage(argThat(req -> req != null && "MTR-1001".equals(req.getMeterNumber()))))
                .thenReturn(mockDto);
        when(waterUsageService.recordWaterUsage(argThat(req -> req != null && "MTR-1002".equals(req.getMeterNumber()))))
                .thenThrow(new DuplicateResourceException("Duplicate reading"));

        CsvUploadSummaryDto summary = csvUploadService.processCsvFile(file);

        assertNotNull(summary);
        assertEquals(3, summary.getTotalRows());
        assertEquals(1, summary.getSuccessfulRows());
        assertEquals(2, summary.getFailedRows());
        assertEquals(1, summary.getDuplicateRows());
        assertEquals(2, summary.getErrors().size());
    }
}
