package com.jala.backend.export.util;

import com.jala.backend.reports.dto.response.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

public class PdfExportUtil {

    private PdfExportUtil() {
    }

    private static void addTitle(
            Document document,
            String title)
            throws DocumentException {

        Font titleFont =
                new Font(Font.HELVETICA, 18, Font.BOLD);

        Paragraph paragraph =
                new Paragraph(title, titleFont);

        paragraph.setAlignment(Element.ALIGN_CENTER);

        paragraph.setSpacingAfter(20);

        document.add(paragraph);
    }

    private static void addSection(
            Document document,
            String key,
            String value)
            throws DocumentException {

        Font font =
                new Font(Font.HELVETICA, 12);

        Paragraph paragraph =
                new Paragraph(
                        key + " : " + value,
                        font);

        paragraph.setSpacingAfter(8);

        document.add(paragraph);
    }

    public static byte[] exportRevenue(
            RevenueReportResponse report) {

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4);

            PdfWriter.getInstance(
                    document,
                    out);

            document.open();

            addTitle(
                    document,
                    "Revenue Report");

            addSection(
                    document,
                    "Site",
                    report.getSiteName());

            addSection(
                    document,
                    "From",
                    report.getFromDate().toString());

            addSection(
                    document,
                    "To",
                    report.getToDate().toString());

            addSection(
                    document,
                    "Harvest Count",
                    String.valueOf(
                            report.getHarvestCount()));

            addSection(
                    document,
                    "Total Harvest",
                    report.getTotalHarvestKg().toString());

            addSection(
                    document,
                    "Average Harvest",
                    report.getAverageHarvestKg().toString());

            addSection(
                    document,
                    "Average Price",
                    report.getAverageSellingPrice().toString());

            addSection(
                    document,
                    "Total Revenue",
                    report.getTotalRevenue().toString());

            document.close();

            return out.toByteArray();

        } catch (Exception ex) {

            throw new RuntimeException(ex);
        }
    }

    public static byte[] exportFeed(
            FeedReportResponse report) {

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, out);

            document.open();

            addTitle(document, "Feed Report");

            PdfPTable table = new PdfPTable(7);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{
                    2f,1f,1f,1.5f,1.5f,2f,3f
            });

            addHeader(table,"Feed Date");
            addHeader(table,"Cycle");
            addHeader(table,"Session");
            addHeader(table,"Feed Size");
            addHeader(table,"Quantity");
            addHeader(table,"Created By");
            addHeader(table,"Remarks");

            for (FeedReportItemResponse detail : report.getDetails()) {

                table.addCell(detail.getFeedDate().toString());

                table.addCell(
                        String.valueOf(
                                detail.getCycleNumber()));

                table.addCell(
                        String.valueOf(
                                detail.getSessionNumber()));

                table.addCell(detail.getFeedSize());

                table.addCell(
                        detail.getFeedQuantityKg().toString());

                table.addCell(detail.getCreatedBy());

                table.addCell(
                        detail.getRemarks() == null
                                ? ""
                                : detail.getRemarks());
            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception ex) {

            throw new RuntimeException(ex);
        }
    }

    public static byte[] exportMedicine(
            MedicineReportResponse report) {

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, out);

            document.open();

            addTitle(document, "Medicine Report");

            PdfPTable table = new PdfPTable(7);

            table.setWidthPercentage(100);

            addHeader(table,"Created");
            addHeader(table,"Cycle");
            addHeader(table,"Quantity");
            addHeader(table,"Unit");
            addHeader(table,"Created By");
            addHeader(table,"Remarks");
            addHeader(table,"Photos");

            for (MedicineReportItemResponse detail
                    : report.getDetails()) {

                table.addCell(
                        detail.getCreatedAt().toString());

                table.addCell(
                        String.valueOf(
                                detail.getCycleNumber()));

                table.addCell(
                        detail.getQuantity().toString());

                table.addCell(detail.getUnit());

                table.addCell(detail.getCreatedBy());

                table.addCell(
                        detail.getRemarks() == null
                                ? ""
                                : detail.getRemarks());

                table.addCell(
                        String.valueOf(
                                detail.getPhotos().size()));
            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception ex) {

            throw new RuntimeException(ex);
        }
    }

    private static void addHeader(
            PdfPTable table,
            String text) {

        Font font =
                new Font(Font.HELVETICA, 11, Font.BOLD);

        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        table.addCell(cell);
    }

}