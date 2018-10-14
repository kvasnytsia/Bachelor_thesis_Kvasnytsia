/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import com.hp.hpl.jena.query.QuerySolution;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.hp.hpl.jena.query.ResultSet;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 *
 * @author karsten
 */
public class WriteSheet {

    private static String FILE_NAME;
    private XSSFWorkbook WORKBOOK = new XSSFWorkbook();

    private CellStyle style_green;
    private CellStyle style_yellow;
    private CellStyle style_red;
    private CellStyle style_normal;
    private CellStyle style_header;
    private CellStyle style_comment;

    public WriteSheet() {
        this("C:\\Users\\Kate\\Documents\\DataQualityCheck_2018_07_08.xlsx");
    }

    public WriteSheet(String filename) {
        WriteSheet.FILE_NAME = filename;
        init();

    }

    private void init() {
        this.style_green = WORKBOOK.createCellStyle();
        style_green.setAlignment(HorizontalAlignment.CENTER);
        style_green.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style_green.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.style_yellow = WORKBOOK.createCellStyle();
        style_yellow.setAlignment(HorizontalAlignment.CENTER);
        style_yellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style_yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.style_red = WORKBOOK.createCellStyle();
        style_red.setAlignment(HorizontalAlignment.CENTER);
        style_red.setFillForegroundColor(IndexedColors.RED.getIndex());
        style_red.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.style_normal = WORKBOOK.createCellStyle();
        style_normal.setAlignment(HorizontalAlignment.CENTER);

        this.style_header = WORKBOOK.createCellStyle();
        style_header.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = WORKBOOK.createFont();
        headerFont.setBold(true);
        style_header.setFont(headerFont);
        
        this.style_comment = WORKBOOK.createCellStyle();
        style_comment.setAlignment(HorizontalAlignment.CENTER);
        style_comment.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style_comment.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font commentFont = WORKBOOK.createFont();
        commentFont.setBold(true);
        style_comment.setFont(commentFont);

    }

    /**
     *
     * @param sheet_name
     * @param rs
     * @param attributes
     * @return
     */
    public boolean write(String sheet_name, ResultSet rs, String[] attributes) {
        boolean b = true;
        XSSFSheet sheet = WORKBOOK.createSheet(sheet_name);

        int rowNum = 0;
        int nrNum = 1;
        int colNum = 0;
        Row row = sheet.createRow(rowNum++);
        QuerySolution qs;
        Iterator it;

        // write the attributes first
        Cell cell = row.createCell(colNum++);
        cell.setCellValue("NR");
        cell.setCellStyle(style_header);

        int coll = 0;
        sheet.setColumnWidth(coll++, 256 * 6); // NR
        for (String attr : attributes) {
            if (coll == 1) sheet.setColumnWidth(coll++, 256 * 45); else
            sheet.setColumnWidth(coll++, 256 * 20);
            cell = row.createCell(colNum++);
            cell.setCellValue(attr);
            cell.setCellStyle(style_header);
        }
        
        sheet.setColumnWidth(coll++, 256 * 20);
        cell = row.createCell(colNum++);
        cell.setCellValue("erledigt");
        cell.setCellStyle(style_comment);
        
        sheet.setColumnWidth(coll++, 256 * 20);
        cell = row.createCell(colNum++);
        cell.setCellValue("in Bearbeitung");
        cell.setCellStyle(style_comment);
        
        sheet.setColumnWidth(coll++, 256 * 20);
        cell = row.createCell(colNum++);
        cell.setCellValue("unauffindbar");
        cell.setCellStyle(style_comment);
        
        sheet.setColumnWidth(coll++, 256 * 20);
        cell = row.createCell(colNum++);
        cell.setCellValue("kein Fehler");
        cell.setCellStyle(style_comment);

        // write the values
        while (rs.hasNext()) {
            qs = rs.nextSolution();
            row = sheet.createRow(rowNum++);
            colNum = 0;
            // add NR
            cell = row.createCell(colNum++);
            cell.setCellStyle(style_normal);
            cell.setCellValue("" + nrNum);
            nrNum++;
            for (String attr : attributes) {
                String value = "";
                if (qs.get(attr) != null) {
                    value = qs.get(attr).toString();
                    if (value.indexOf("?") > 0) {
                        // only leave the important information
                        value = value.substring(value.indexOf("?") + 1);
                    }
                    if (value.indexOf("#") > 0 && value.indexOf("^^") < 0) {
                        // only leave the important information
                        value = value.substring(value.indexOf("#") + 1);
                    }
                    if (value.indexOf("#") > 0 && value.indexOf("^^") > 0) {
                        // only leave the important information
                        // value = value.substring(0, value.indexOf("^^"));
                        // convert to integer
                        value = qs.get(attr).asLiteral().getString();
                    }
                    if (value.startsWith("http://nomisma.org/id/")) {
                        value = value.replaceAll("http://nomisma.org/id/", "nm:");
                    }
                }
                cell = row.createCell(colNum++);
                cell.setCellValue(value);
                cell.setCellStyle(style_normal);
            }

        }

        try {
            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
            WORKBOOK.write(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done");

        return b;
    }

    public void closeWorkbook() {
        try {
            WORKBOOK.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean writeOverview(String sheet_name, Object[][] data) {
        boolean b = true;
        XSSFSheet sheet = WORKBOOK.createSheet(sheet_name);

        //set column widths, the width is measured in units of 1/256th of a character width
        /* data[0][0] = "Regel";
         data[0][1] = "Name";
         data[0][2] = "Beschreibung";
         data[0][3] = "Anzahl Fälle";
         data[0][4] = "Reference Query";
         data[0][5] = "Reference Size";
         data[0][6] = "Fallquotient"; 
         data[0][7] = "Anfrage"   */
        
        sheet.setColumnWidth(0, 256 * 6);
        sheet.setColumnWidth(1, 256 * 40);
        sheet.setColumnWidth(2, 256 * 90);
        sheet.setColumnWidth(3, 256 * 13);
        sheet.setColumnWidth(4, 256 * 17);
        sheet.setColumnWidth(5, 256 * 15);
        sheet.setColumnWidth(6, 256 * 13);
        sheet.setColumnWidth(7, 256 * 13);
        sheet.setColumnWidth(8, 256 * 150);
        sheet.setZoom(100); //100% scale

        int rowNum = 0;

        for (Object[] data_r : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : data_r) {
                Cell cell = row.createCell(colNum++);
                if (rowNum != 1) {
                    cell.setCellStyle(style_normal);
                } else {
                    cell.setCellStyle(style_header);
                }
                if (field instanceof String) {
                    String value = (String) field;
                    cell.setCellValue(value);
                    switch (value) {
                        case "Inconsistent":
                            cell.setCellStyle(style_red);
                            break;
                        case "Missing":
                            cell.setCellStyle(style_yellow);
                            break; 
                        case "Outlier":
                            cell.setCellStyle(style_green);
                            break;
                    }
                    
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                } else if (field instanceof Double) {
                    Double d = (Double) field;
                    cell.setCellValue(d);

                    if (d < 0.009) {
                        cell.setCellStyle(style_green);
                    } else {
                        if (d < 0.05) {
                            cell.setCellStyle(style_yellow);
                        } else {
                            cell.setCellStyle(style_red);
                        }
                    }

                }
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
            WORKBOOK.write(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done w");
        return b;
    }

}
