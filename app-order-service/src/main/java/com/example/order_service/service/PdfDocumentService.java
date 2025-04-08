package com.example.order_service.service;

import com.example.order_lib.dto.AnalyticDto;
import com.example.order_service.model.enums.Status;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

@UtilityClass
public class PdfDocumentService {

    @SneakyThrows
    public static byte[] createDocument(AnalyticDto analytic, LocalDateTime currentTime) {
        LocalDate currentDate = LocalDate.of(currentTime.getYear(), currentTime.getMonth(), currentTime.getDayOfMonth());

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yPosition = PDRectangle.A4.getHeight() - margin;
            float leading = 20;

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            PDType1Font textFont = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

            contentStream.beginText();
            contentStream.setFont(titleFont, 24);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Report: ");
            contentStream.endText();
            yPosition -= leading * 2;

            contentStream.beginText();
            contentStream.setFont(textFont, 14);
            contentStream.newLineAtOffset(margin, yPosition);

            addTextLine(contentStream, "Report date: " + currentDate, 0, -leading);
            yPosition -= leading;
            addTextLine(contentStream, "Order number due the day: " + analytic.orderCount(), 0, -leading);
            yPosition -= leading;
            addTextLine(contentStream, "Average processing time: " + analytic.avrProcessTime() + " sec.", 0, -leading);
            yPosition -= leading;
            addTextLine(contentStream, "Order number in progress: " +
                    analytic.orderCountGroupByStatus().get(Status.IN_PROGRESS.toString()), 0, -leading);
            yPosition -= leading;
            addTextLine(contentStream, "Order number processed: " +
                    analytic.orderCountGroupByStatus().get(Status.PROCESSED.toString()), 0, -leading);
            yPosition -= leading;
            yPosition -= leading * 2;
            addTextLine(contentStream, "Number of orders group by customers:", 0, -leading);
            yPosition -= leading;

            for (var entry : analytic.orderCountGroupByCustomers().entrySet()) {
                addTextLine(contentStream, entry.getKey() + ": " + entry.getValue(), 0, -leading);
                yPosition -= leading;
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    @SneakyThrows
    private static void addTextLine(PDPageContentStream contentStream, String text, float x, float y) {
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
    }
}