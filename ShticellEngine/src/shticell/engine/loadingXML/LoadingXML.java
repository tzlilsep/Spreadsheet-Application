package shticell.engine.loadingXML;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import shticell.engine.loadingXML.schema.*;
import shticell.engine.sheet.impl.SheetImpl;

import java.io.File;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class LoadingXML {

    public SheetImpl loadSpreadsheetFromReader(Reader reader) {

        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            STLSheet spreadsheetData = (STLSheet) unmarshaller.unmarshal(reader);

            if (spreadsheetData == null) {
                return null;
            }

            if (!validateSpreadsheet(spreadsheetData)) {
                return null;
            }

            SheetImpl sheet = createSheet(spreadsheetData);

            return sheet;

        } catch (JAXBException e) {
            System.out.println("Error unmarshalling the XML file: " + e.getMessage());
            return null;
        }
    }
    public SheetImpl loadSpreadsheet(String xmlFilePath) {
        if (!validateFilePath(xmlFilePath)) {
            throw new IllegalArgumentException("Invalid file path or file type. Please provide a valid XML file.");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File file = new File(xmlFilePath);
            STLSheet spreadsheetData = (STLSheet) unmarshalFile(file, unmarshaller);

            if (spreadsheetData == null) {
                return null;
            }

            if (!validateSpreadsheet(spreadsheetData)) {
                return null;
            }

            SheetImpl sheet = createSheet(spreadsheetData);

            return sheet;

        } catch (JAXBException e) {
            System.out.println("Error unmarshalling the XML file: " + e.getMessage());
            return null;
        }
    }

    private boolean validateFilePath(String xmlFilePath) {
        File file = new File(xmlFilePath);
        return file.exists() && file.isFile() && xmlFilePath.endsWith(".xml");
    }

    private Object unmarshalFile(File file, Unmarshaller unmarshaller) {
        try {
            return unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            System.out.println("Error unmarshalling the XML file: " + e.getMessage());
            return null;
        }
    }

    private boolean validateSpreadsheet(STLSheet spreadsheetData) {
        int numRows = spreadsheetData.getSTLLayout().getRows();
        int numCols = spreadsheetData.getSTLLayout().getColumns();

        if (numRows < 1 || numRows > 50 || numCols < 1 || numCols > 20) {
            throw new IllegalArgumentException("Invalid sheet dimensions. Rows should be between 1 and 50, columns between 1 and 20.");
        }

        // Validate each cell position
        for (STLCell cell : spreadsheetData.getSTLCells().getSTLCell()) {
            int row = cell.getRow() - 1;
            int columnIndex = cell.getColumn().charAt(0) - 'A';

            if (row < 0 || row >= numRows || columnIndex < 0 || columnIndex >= numCols) {
                throw new IllegalArgumentException("Invalid cell position: " + cell.getColumn() + cell.getRow());
            }
        }

        // Validate ranges
        if (!validateRanges(spreadsheetData.getSTLRanges(), numRows, numCols)) {
            return false;
        }

        return true;
    }

    private boolean validateRanges(STLRanges ranges, int numRows, int numCols) {
        if (ranges == null) return true; // No ranges to validate

        Set<String> rangeNames = new HashSet<>();
        for (STLRange range : ranges.getSTLRange()) {
            String rangeName = range.getName();
            if (rangeNames.contains(rangeName)) {
                throw new IllegalArgumentException("Duplicate range name: " + rangeName);
            }
            rangeNames.add(rangeName);

            // Validate boundaries
            String fromCell = range.getSTLBoundaries().getFrom();
            String toCell = range.getSTLBoundaries().getTo();

            if (!isValidCellReference(fromCell, numRows, numCols) || !isValidCellReference(toCell, numRows, numCols)) {
                throw new IllegalArgumentException("Invalid range boundaries for range: " + rangeName);
            }
        }
        return true;
    }

    private boolean isValidCellReference(String cellRef, int numRows, int numCols) {
        if (cellRef == null || cellRef.length() < 2) return false;

        int row = Integer.parseInt(cellRef.substring(1)) - 1;
        char colChar = cellRef.charAt(0);
        int col = colChar - 'A';

        return row >= 0 && row < numRows && col >= 0 && col < numCols;
    }

    private SheetImpl createSheet(STLSheet spreadsheetData) {
        String sheetName = spreadsheetData.getName();
        int numRows = spreadsheetData.getSTLLayout().getRows();
        int numCols = spreadsheetData.getSTLLayout().getColumns();
        int columnWidth = spreadsheetData.getSTLLayout().getSTLSize().getColumnWidthUnits();
        int rowHeight = spreadsheetData.getSTLLayout().getSTLSize().getRowsHeightUnits();


        SheetImpl sheet = new SheetImpl(numRows, numCols, sheetName, columnWidth, rowHeight,"username");


        int row=0;
        int columnIndex=0;
        String originalValue="";

        try {

            // Process ranges
            processRanges(sheet, spreadsheetData.getSTLRanges());


            // Process cells
            for (STLCell cellData : spreadsheetData.getSTLCells().getSTLCell()) {
                row = cellData.getRow() - 1;
                columnIndex = cellData.getColumn().charAt(0) - 'A';
                originalValue = cellData.getSTLOriginalValue();

                sheet.setCell(row, columnIndex, originalValue, false);
            }



            sheet.setCell(row, columnIndex, originalValue, true);  // Finalize changes after processing

            // Check for circular references after all cells and ranges are set
            if (sheet.hasCircularReferences()) {
                throw new IllegalArgumentException("Circular reference detected in the sheet.");
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException( e.getMessage());
        }

        System.out.println("Spreadsheet loaded successfully from XML.");
        return sheet;
    }

    private void processRanges(SheetImpl sheet, STLRanges ranges) {
        if (ranges == null) return;

        for (STLRange range : ranges.getSTLRange()) {
            String rangeName = range.getName();
            String fromCell = range.getSTLBoundaries().getFrom();
            String toCell = range.getSTLBoundaries().getTo();

            // Handle range definitions in SheetImpl
            sheet.defineRange(rangeName, fromCell, toCell);
        }
    }
}
