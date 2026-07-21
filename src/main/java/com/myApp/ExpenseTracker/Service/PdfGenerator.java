package com.myApp.ExpenseTracker.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.myApp.ExpenseTracker.Dto.ExpenseResponse;
import com.myApp.ExpenseTracker.Dto.ReservedResponse;
import com.myApp.ExpenseTracker.Dto.TransactionResponse;
import com.myApp.ExpenseTracker.Model.ReportData;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {
    public static byte[] generate(ReportData reportData) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            Document document = new Document();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            document.add(new Paragraph("Expense Tracker Report"));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Account Summary"));
            document.add(new Paragraph("----------------------------------------"));
            document.add(new Paragraph("Total Balance : ₹" + reportData.getTotalBalance()));
            document.add(new Paragraph("Total Reserved : ₹" + reportData.getTotalReserved()));
            document.add(new Paragraph("Available Balance : ₹" + reportData.getAvailableBalance()));


            PdfPTable reserveTable = new PdfPTable(3);
            reserveTable.setWidthPercentage(100);
            reserveTable.setSpacingBefore(10f);
            reserveTable.setWidths(new float[]{3f, 2f, 5f});

            reserveTable.addCell(headerCell("Label"));
            reserveTable.addCell(headerCell("Amount"));
            reserveTable.addCell(headerCell("Note"));

            for (ReservedResponse reserve : reportData.getReserves()) {
                reserveTable.addCell(reserve.label());
                reserveTable.addCell(String.format("₹%,.2f", reserve.amount()));
                reserveTable.addCell(reserve.note());
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Reserved Amounts"));
            document.add(reserveTable);

            PdfPTable expenseTable = new PdfPTable(4);
            expenseTable.setWidthPercentage(100);
            expenseTable.setSpacingBefore(10f);
            expenseTable.setWidths(new float[]{2f, 3f, 2f, 5f});

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
            document.add(new Paragraph("Expense Details"));
            document.add(expenseTable);


            PdfPTable incomeTable = new PdfPTable(3);
            incomeTable.setWidthPercentage(100);
            incomeTable.setSpacingBefore(10f);
            incomeTable.setWidths(new float[]{2f, 2f, 6f});

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
            document.add(new Paragraph("Income Details"));
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
        return cell;
    }
}
