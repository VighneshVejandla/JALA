package com.jala.backend.export.util;

import com.jala.backend.reports.dto.response.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    private static CellStyle createHeaderStyle(
            Workbook workbook) {

        Font font = workbook.createFont();

        font.setBold(true);

        font.setFontHeightInPoints((short) 12);

        CellStyle style = workbook.createCellStyle();

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.LIGHT_BLUE.getIndex());

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private static CellStyle createCellStyle(
            Workbook workbook) {

        CellStyle style = workbook.createCellStyle();

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private static int createTitle(
            Sheet sheet,
            String title,
            int rowNum,
            Workbook workbook) {

        Font font = workbook.createFont();

        font.setBold(true);

        font.setFontHeightInPoints((short) 16);

        CellStyle style = workbook.createCellStyle();

        style.setFont(font);

        Row row = sheet.createRow(rowNum);

        Cell cell = row.createCell(0);

        cell.setCellValue(title);

        cell.setCellStyle(style);

        return rowNum + 2;
    }

    private static int createHeader(
            Sheet sheet,
            String[] headers,
            int rowNum,
            CellStyle style) {

        Row row = sheet.createRow(rowNum);

        for (int i = 0; i < headers.length; i++) {

            Cell cell = row.createCell(i);

            cell.setCellValue(headers[i]);

            cell.setCellStyle(style);
        }

        return rowNum + 1;
    }

    private static void autoSize(
            Sheet sheet,
            int columns) {

        for (int i = 0; i < columns; i++) {

            sheet.autoSizeColumn(i);
        }
    }


    public static byte[] exportRevenue(
            RevenueReportResponse report) {

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet("Revenue");

            CellStyle header =
                    createHeaderStyle(workbook);

            int rowNum = 0;

            Row row = sheet.createRow(rowNum++);

            Cell cell = row.createCell(0);

            cell.setCellValue("Revenue Report");

            cell.setCellStyle(header);

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "Site : "
                                    + report.getSiteName());

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "From : "
                                    + report.getFromDate());

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "To : "
                                    + report.getToDate());

            rowNum++;

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "Harvest Count");

            sheet.getRow(rowNum - 1)
                    .createCell(1)
                    .setCellValue(
                            report.getHarvestCount());

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "Total Harvest");

            sheet.getRow(rowNum - 1)
                    .createCell(1)
                    .setCellValue(
                            report.getTotalHarvestKg()
                                    .doubleValue());

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "Average Harvest");

            sheet.getRow(rowNum - 1)
                    .createCell(1)
                    .setCellValue(
                            report.getAverageHarvestKg()
                                    .doubleValue());

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "Average Price");

            sheet.getRow(rowNum - 1)
                    .createCell(1)
                    .setCellValue(
                            report.getAverageSellingPrice()
                                    .doubleValue());

            sheet.createRow(rowNum++)
                    .createCell(0)
                    .setCellValue(
                            "Total Revenue");

            sheet.getRow(rowNum - 1)
                    .createCell(1)
                    .setCellValue(
                            report.getTotalRevenue()
                                    .doubleValue());

            sheet.autoSizeColumn(0);

            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (IOException ex) {

            throw new RuntimeException(ex);
        }
    }

    public static byte[] exportFeed(
            FeedReportResponse report) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Feed Report");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle cellStyle = createCellStyle(workbook);

            int rowNum = 0;

            rowNum = createTitle(
                    sheet,
                    "Feed Report",
                    rowNum,
                    workbook);

            String[] headers = {
                    "Feed Date",
                    "Cycle",
                    "Session",
                    "Feed Size",
                    "Quantity (Kg)",
                    "Created By",
                    "Remarks"
            };

            rowNum = createHeader(
                    sheet,
                    headers,
                    rowNum,
                    headerStyle);

            for (FeedReportItemResponse detail : report.getDetails()) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(detail.getFeedDate().toString());

                row.createCell(1)
                        .setCellValue(detail.getCycleNumber());

                row.createCell(2)
                        .setCellValue(detail.getSessionNumber());

                row.createCell(3)
                        .setCellValue(detail.getFeedSize());

                row.createCell(4)
                        .setCellValue(detail.getFeedQuantityKg().doubleValue());

                row.createCell(5)
                        .setCellValue(detail.getCreatedBy());

                row.createCell(6)
                        .setCellValue(
                                detail.getRemarks() == null
                                        ? ""
                                        : detail.getRemarks());

                for (int i = 0; i < 7; i++) {
                    row.getCell(i).setCellStyle(cellStyle);
                }
            }

            autoSize(sheet, 7);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (IOException ex) {

            throw new RuntimeException(ex);
        }
    }

    public static byte[] exportMedicine(
            MedicineReportResponse report) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Medicine Report");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle cellStyle = createCellStyle(workbook);

            int rowNum = 0;

            rowNum = createTitle(
                    sheet,
                    "Medicine Report",
                    rowNum,
                    workbook);

            String[] headers = {
                    "Created At",
                    "Cycle",
                    "Quantity",
                    "Unit",
                    "Created By",
                    "Remarks",
                    "Photos"
            };

            rowNum = createHeader(
                    sheet,
                    headers,
                    rowNum,
                    headerStyle);

            for (MedicineReportItemResponse detail : report.getDetails()) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(detail.getCreatedAt().toString());

                row.createCell(1)
                        .setCellValue(detail.getCycleNumber());

                row.createCell(2)
                        .setCellValue(detail.getQuantity().doubleValue());

                row.createCell(3)
                        .setCellValue(detail.getUnit());

                row.createCell(4)
                        .setCellValue(detail.getCreatedBy());

                row.createCell(5)
                        .setCellValue(
                                detail.getRemarks() == null
                                        ? ""
                                        : detail.getRemarks());

                row.createCell(6)
                        .setCellValue(detail.getPhotos().size());

                for (int i = 0; i < 7; i++) {
                    row.getCell(i).setCellStyle(cellStyle);
                }
            }

            autoSize(sheet, 7);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}