package com.myApp.ExpenseTracker.Service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.myApp.ExpenseTracker.Dto.ExpenseResponse;
import com.myApp.ExpenseTracker.Dto.ReservedResponse;
import com.myApp.ExpenseTracker.Dto.TransactionResponse;
import com.myApp.ExpenseTracker.Model.ReportData;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class PdfGenerator {
    public static byte[] generate(ReportData reportData) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            Document document = new Document();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    20
            );

            Paragraph title = new Paragraph("ETrace Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);
            Paragraph subTitle = new Paragraph(
                    "Generated on: " + LocalDate.now()
            );
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(20f);
            document.add(title);
            document.add(subTitle);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10f);
            summaryTable.setSpacingAfter(15f);
            summaryTable.setWidths(new float[]{3f, 2f});

            // Section Title
            summaryTable.addCell(sectionCell("Account Summary", 2));

            // Column Headers
            summaryTable.addCell(headerCell("Details"));
            summaryTable.addCell(headerCell("Amount"));

            // Total Balance
            addSummaryRow(summaryTable, "Total Balance", reportData.getTotalBalance(), false);


            // Total Reserved
            addSummaryRow(summaryTable, "Total Reserved", reportData.getTotalReserved(), true);

            // Available Balance
            addSummaryRow(summaryTable, "Available Balance", reportData.getAvailableBalance(), false);
            document.add(summaryTable);

            document.add(new Paragraph(" "));

            PdfPTable reserveTable = new PdfPTable(3);
            reserveTable.setWidthPercentage(100);
            reserveTable.setSpacingBefore(10f);
            reserveTable.setWidths(new float[]{3f, 2f, 5f});

            reserveTable.addCell(sectionCell("Reserved Funds", 3));
            reserveTable.addCell(headerCell("Label"));
            reserveTable.addCell(headerCell("Amount"));
            reserveTable.addCell(headerCell("Note"));

            for (ReservedResponse reserve : reportData.getReserves()) {
                reserveTable.addCell(reserve.label());
                reserveTable.addCell(String.format("₹%,.2f", reserve.amount()));
                reserveTable.addCell(reserve.note());
            }

            document.add(new Paragraph(" "));
            document.add(reserveTable);

            PdfPTable expenseTable = new PdfPTable(4);
            expenseTable.setWidthPercentage(100);
            expenseTable.setSpacingBefore(10f);
            expenseTable.setWidths(new float[]{2f, 3f, 2f, 5f});

            expenseTable.addCell(sectionCell("Expense Details", 4));
            expenseTable.addCell( headerCell("Date") );
            expenseTable.addCell(headerCell("Category"));
            expenseTable.addCell(headerCell("Amount"));
            expenseTable.addCell(headerCell("Description"));

            for (ExpenseResponse expense : reportData.getExpenses()) {

                expenseTable.addCell(expense.expenseDate().toString());
                expenseTable.addCell(expense.categoryName());
                expenseTable.addCell(
                        String.format("₹%,.2f", expense.amount())
                );
                expenseTable.addCell(expense.note());
            }

            document.add(new Paragraph(" "));
            document.add(expenseTable);


            PdfPTable incomeTable = new PdfPTable(3);
            incomeTable.setWidthPercentage(100);
            incomeTable.setSpacingBefore(10f);
            incomeTable.setWidths(new float[]{2f, 2f, 6f});

            incomeTable.addCell(sectionCell("Income Details", 3));
            incomeTable.addCell(headerCell("Date"));
            incomeTable.addCell(headerCell("Amount"));
            incomeTable.addCell(headerCell("Description"));

            for (TransactionResponse income : reportData.getIncomes()) {

                incomeTable.addCell(income.date().toString());
                incomeTable.addCell(
                        String.format("₹%,.2f", income.amount())
                );
                incomeTable.addCell(income.desc());
            }

            document.add(new Paragraph(" "));
            document.add(incomeTable);

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF.", e);
        }
        return outputStream.toByteArray();
    }

    private static PdfPCell headerCell(String text) {
        Font font = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                12
        );

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setPadding(8f);

        return cell;
    }
    private static PdfPCell sectionCell(String title, int colspan) {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                14
        );

        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(200, 220, 255));
        cell.setPadding(10f);

        return cell;
    }
    private static PdfPCell dataCell(String value, boolean alternate, int alignment) {

        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(alignment);

        if (alternate) {
            cell.setBackgroundColor(new Color(245, 245, 245));
        }

        return cell;
    }
    private static void addSummaryRow(
            PdfPTable table,
            String label,
            BigDecimal amount,
            boolean alternate) {

        table.addCell(dataCell(label, alternate, Element.ALIGN_LEFT));

        table.addCell(dataCell(
                String.format("₹%,.2f", amount),
                alternate,
                Element.ALIGN_RIGHT));
    }
}
