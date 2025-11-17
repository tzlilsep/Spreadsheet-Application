package shticell.engine.manage;

import shticell.engine.loadingXML.LoadingXML;
import shticell.engine.sheet.impl.SheetImpl;

import java.util.HashMap;
import java.util.Map;

public class SheetManager {
    private final Map<String, SheetImpl> sheets = new HashMap<>();

    public SheetImpl createSheet(String sheetName, int numRows, int numCols, int columnWidth, int rowHeight) {
        SheetImpl sheet = new SheetImpl(numRows, numCols, sheetName, columnWidth, rowHeight,"username");
        sheets.put(sheetName, sheet);
        return sheet;
    }

    public SheetImpl loadSheetFromXml(String xmlFilePath, String sheetName) {
        LoadingXML loader = new LoadingXML();
        SheetImpl sheet = loader.loadSpreadsheet(xmlFilePath);
        sheets.put(sheetName, sheet);
        return sheet;
    }

    public SheetImpl getSheet(String sheetName) {
        return sheets.get(sheetName);
    }

    public void deleteSheet(String sheetName) {
        sheets.remove(sheetName);
    }

    public Map<String, SheetImpl> getAllSheets() {
        return sheets;
    }
}
