package excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelComparatorAndCopying {

	static List<String> listOfDifferences = new ArrayList<>();
	static List<? extends Name> listOfNames = new ArrayList<>();

	private CellStyle style_yellow;
	private CellStyle style_red;
	private CellStyle style_normal;

	public ExcelComparatorAndCopying(File file1, File file2) throws Exception {

		// serialization of file1 and file2
		FileInputStream inFile1 = new FileInputStream(file1);
		FileInputStream inFile2 = new FileInputStream(file2);
		// creation of the books
		Workbook wb1 = WorkbookFactory.create(inFile1);

		Workbook wb2 = WorkbookFactory.create(inFile2);

		this.style_yellow = wb2.createCellStyle();
		style_yellow.setAlignment(HorizontalAlignment.CENTER);
		style_yellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style_yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		this.style_red = wb2.createCellStyle();
		style_red.setAlignment(HorizontalAlignment.CENTER);
		style_red.setFillForegroundColor(IndexedColors.RED.getIndex());
		style_red.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		this.style_normal = wb2.createCellStyle();
		style_normal.setAlignment(HorizontalAlignment.CENTER);

		/*
		 * // comparison of sheets and writing differences in the difference list for
		 * (String d : CompareSheets(wb1, wb2)) { System.out.println(d); }
		 */

		// finding the same IDs in both workbooks and coping of differences in file2
		for (int b = 0; b < wb1.getNumberOfSheets(); b++) {
			for (int c = 0; c < wb2.getNumberOfSheets(); c++) {
				if (wb1.getSheetName(b).equals(wb2.getSheetName(c))) {
					String Wb1Rule = getRuleBySheetName(wb1.getSheet("Übersicht"), wb1.getSheetName(b));
					String Wb2Rule = getRuleBySheetName(wb2.getSheet("Übersicht"), wb1.getSheetName(c));
					if (Wb1Rule.equals(Wb2Rule) && Wb1Rule != "" && Wb2Rule != "") {
						findEqualIDbyColumn(wb1.getSheetAt(b), wb2.getSheetAt(c));
						break;
					}
				}
			}
		}

		inFile1.close();
		inFile2.close();

		FileOutputStream outFile = new FileOutputStream(file2);
		wb2.write(outFile);
		outFile.close();

		wb2.close();
		wb1.close();

	}

	// Comparison of sheets properties
	public List<String> CompareSheets(Workbook wb1, Workbook wb2) {

		compareNumberOfSheets(wb1, wb2);
		compareSheetNames(wb1, wb2);

		return listOfDifferences;
	}

	// finding the number of the column, where id of the coin is
	int findColumnInSheetByName(Sheet sheet, String cellContent) {
		for (Row row : sheet) {
			for (Cell cell : row) {
				if (cell.getCellTypeEnum() == CellType.STRING) {
					if (cell.getRichStringCellValue().getString().trim().equals(cellContent)) {
						return cell.getColumnIndex();
					}
				}
			}
		}
		return 0;
	}

	void findEqualIDbyColumn(Sheet ChangedSheet, Sheet OriginSheet) {
		// finding the column with id and type of the coin
		int IDColumn = findColumnInSheetByName(OriginSheet, "?coin");
		int TypeColumn = findColumnInSheetByName(OriginSheet, "?type");
		int erledigtColumn = findColumnInSheetByName(ChangedSheet, "erledigt");
		if (IDColumn != 0 && TypeColumn != 0 && erledigtColumn != 0) {
			System.out.println("Name of Sheet ID and Type: " + OriginSheet.getSheetName() + " ID: " + IDColumn
					+ " Type: " + TypeColumn);
			if (findColumnInSheetByName(ChangedSheet, "?coin") == IDColumn
					&& findColumnInSheetByName(ChangedSheet, "?type") == TypeColumn) {
				// iterations on the rows
				for (int m = 1; m < OriginSheet.getPhysicalNumberOfRows(); m++) {
					for (int n = 1; n < ChangedSheet.getPhysicalNumberOfRows(); n++) {
						try {
							if (ChangedSheet.getRow(n).getCell(IDColumn, MissingCellPolicy.CREATE_NULL_AS_BLANK)
									.toString()
									.equals(OriginSheet.getRow(m)
											.getCell(IDColumn, MissingCellPolicy.CREATE_NULL_AS_BLANK).toString())
									&& ChangedSheet.getRow(n)
											.getCell(TypeColumn, MissingCellPolicy.CREATE_NULL_AS_BLANK).toString()
											.equals(OriginSheet.getRow(m)
													.getCell(TypeColumn, MissingCellPolicy.CREATE_NULL_AS_BLANK)
													.toString())) {
								// copy the content of the rows with the equal ID and type
								copyCellsInRowFromColumnNr(ChangedSheet.getRow(n), OriginSheet.getRow(m),
										erledigtColumn);
							}
						} catch (NullPointerException e) {
							System.out.print("Caught the NullPointerException");
						}

					}
				}
			}
		} else {
			if (findColumnInSheetByName(ChangedSheet, "?coin") == IDColumn && erledigtColumn != 0) {

				System.out.println("Name of Sheet ID: " + OriginSheet.getSheetName() + " ID: " + IDColumn + " Type: "
						+ TypeColumn);

				// iterations on the rows
				for (int m = 1; m < OriginSheet.getPhysicalNumberOfRows(); m++) {
					for (int n = 1; n < ChangedSheet.getPhysicalNumberOfRows(); n++) {
						if (ChangedSheet.getRow(n).getCell(IDColumn).toString()
								.equals(OriginSheet.getRow(m).getCell(IDColumn).toString())) {
							// copy the content of the rows with the equal ID and type
							copyCellsInRowFromColumnNr(ChangedSheet.getRow(n), OriginSheet.getRow(m), erledigtColumn);
						}
					}
				}
			}
		}

	}

	String getRuleBySheetName(Sheet OverViewSheet, String SheetName) {
		int NameColumn = findColumnInSheetByName(OverViewSheet, "Name");
		int RuleColumn = findColumnInSheetByName(OverViewSheet, "Anfrage");
		for (int n = 0; n < OverViewSheet.getPhysicalNumberOfRows(); n++) {
			if (OverViewSheet.getRow(n).getCell(NameColumn).toString().equals(SheetName)) {
				return OverViewSheet.getRow(n).getCell(RuleColumn).toString();
			}
		}
		return "";

	}

	// copy only the missing cells in the origin sheet
	void copyCellsInRowFromColumnNr(Row RowInChangedSheet, Row RowInOriginSheet, int FromColumnNr) {
		// Collisions
		ArrayList<String> ListOfOriginCell = new ArrayList<>();
		for (int s = FromColumnNr; s < RowInChangedSheet.getPhysicalNumberOfCells(); s++) {
			ListOfOriginCell.add(RowInOriginSheet.getCell(s, MissingCellPolicy.CREATE_NULL_AS_BLANK).toString());
		}
		for (int k = FromColumnNr; k < RowInChangedSheet.getPhysicalNumberOfCells(); k++) {
			Cell OriginCell = RowInOriginSheet.getCell(k, MissingCellPolicy.CREATE_NULL_AS_BLANK);		
			Cell ChangedCell = RowInChangedSheet.getCell(k, MissingCellPolicy.CREATE_NULL_AS_BLANK);		
			if (containAllElements(ListOfOriginCell, "")&&(OriginCell.toString() == "")) {
				OriginCell.setCellValue(ChangedCell.toString());
				OriginCell.setCellStyle(style_normal);
			} else {
				if (!(OriginCell.toString() == "")&&!(ChangedCell.toString() == "")) {
					OriginCell.setCellStyle(style_red);
				} else if (!containAllElements(ListOfOriginCell, "")&&!(ChangedCell.toString() == "")) {
					OriginCell.setCellValue(ChangedCell.toString());
					OriginCell.setCellStyle(style_yellow);
				}
			}
		}
	}

	boolean containAllElements(ArrayList<String> List, String Content) {
		for (String cell : List) {
			if (!(cell.toString() == Content)) {
				return false;
			}
		}
		return true;
	}

	// comparison number of sheets
	void compareNumberOfSheets(Workbook wb1, Workbook wb2) {
		int num1 = wb1.getNumberOfSheets();
		int num2 = wb2.getNumberOfSheets();
		if (num1 != num2) {
			String str = String.format(Locale.ROOT, "%s\nworkbook1 [%d] != workbook2 [%d]",
					"Number of Sheets do not match ::", num1, num2);

			listOfDifferences.add(str);

		}
	}

	// comparison names of sheets
	void compareSheetNames(Workbook wb1, Workbook wb2) {
		for (int i = 0; i < wb1.getNumberOfSheets(); i++) {
			String name1 = wb1.getSheetName(i);
			String name2 = (wb2.getNumberOfSheets() > i) ? wb2.getSheetName(i) : "";

			if (!name1.equals(name2)) {
				String str = String.format(Locale.ROOT, "%s\nworkbook1 -> %s [%d] != workbook2 -> %s [%d]",
						"Name of the sheets do not match ::", name1, i + 1, name2, i + 1);
				listOfDifferences.add(str);
			}
		}
	}

}