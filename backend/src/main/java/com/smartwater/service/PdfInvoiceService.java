package com.smartwater.service;

import com.smartwater.dto.BillItemDto;
import com.smartwater.dto.HouseholdBillDto;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfInvoiceService {

    private final BillingCycleService billingCycleService;

    public byte[] generatePdfInvoice(Long billId) throws IOException {
        HouseholdBillDto bill = billingCycleService.getBillById(billId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // Header Title
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                cs.newLineAtOffset(50, 750);
                cs.showText("SMART WATER UTILITY INVOICE");
                cs.endText();

                // Apartment & Bill Info
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(50, 720);
                cs.showText("Invoice ID: #INV-" + bill.getId());
                cs.newLineAtOffset(0, -18);
                cs.showText("Apartment Community: " + bill.getApartmentName());
                cs.newLineAtOffset(0, -18);
                cs.showText("Household Unit: Flat " + bill.getFlatNumber());
                cs.newLineAtOffset(0, -18);
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.showText("Metered Consumption: " + bill.getMeteredConsumptionKl() + " kL");
                cs.newLineAtOffset(0, -15);
                cs.showText("Payment Due Date: " + bill.getDueDate());
                cs.endText();

                // Line Divider
                cs.setLineWidth(1f);
                cs.moveTo(50, 620);
                cs.lineTo(550, 620);
                cs.stroke();

                // Table Header
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                cs.newLineAtOffset(50, 600);
                cs.showText("Item Description");
                cs.newLineAtOffset(350, 0);
                cs.showText("Amount (INR)");
                cs.endText();

                int y = 575;
                if (bill.getItems() != null) {
                    for (BillItemDto item : bill.getItems()) {
                        cs.beginText();
                        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        cs.newLineAtOffset(50, y);
                        cs.showText(item.getDescription());
                        cs.newLineAtOffset(350, 0);
                        cs.showText("INR " + item.getTotalAmount());
                        cs.endText();
                        y -= 20;
                    }
                }

                // Line Divider
                cs.setLineWidth(1f);
                cs.moveTo(50, y);
                cs.lineTo(550, y);
                cs.stroke();
                y -= 25;

                // Total Payable
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                cs.newLineAtOffset(50, y);
                cs.showText("TOTAL AMOUNT DUE:");
                cs.newLineAtOffset(350, 0);
                cs.showText("INR " + bill.getTotalPayable());
                cs.endText();

                // Footer Payment Instructions
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 9);
                cs.newLineAtOffset(50, 100);
                cs.showText("Thank you for practicing responsible water conservation!");
                cs.newLineAtOffset(0, -15);
                cs.showText("Please pay before the due date via your Smart Water Resident Portal or Apartment Maintenance Desk.");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
