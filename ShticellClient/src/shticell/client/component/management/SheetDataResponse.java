package shticell.client.component.management;

import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

public class SheetDataResponse {
    private String sheetName;
    private String owner;
    private List<List<String>> data;
    private double columnsWidth;  // New field
    private double rowsHeight;    // New field
    private int numRows;          // New field
    private int numColumns;
    private PermissionType permissionType; // הוספת סוג ההרשאה
    private int version;

    public SheetDataResponse(String sheetName, String owner, List<List<String>> data, double columnsWidth, double rowsHeight, int numRows, int numColumns, PermissionType permissionType, int version) {
        this.sheetName = sheetName;
        this.owner=owner;
        this.data = data.stream()
                .map(ArrayList::new) // Create a new ArrayList for each inner list
                .collect(Collectors.toList());
        this.columnsWidth = columnsWidth;
        this.rowsHeight = rowsHeight;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.permissionType = permissionType;
        this.version = version;

    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCell(int row, int col) {
        return data.get(row).get(col);
    }

    public int getVersion()
    {
        return version;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    // Getters and Setters
    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public double getColumnsWidth() {
        return columnsWidth;
    }

    public void setColumnsWidth(double columnsWidth) {
        this.columnsWidth = columnsWidth;
    }

    public double getRowsHeight() {
        return rowsHeight;
    }

    public void setRowsHeight(double rowsHeight) {
        this.rowsHeight = rowsHeight;
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public void setCell(int row, int col, String value) {
        // Ensure row and column indices are within bounds
        if (row >= 0 && row < data.size() && col >= 0 && col < data.get(row).size()) {
            data.get(row).set(col, value);
        } else {
            throw new IndexOutOfBoundsException("Row or column index is out of bounds.");
        }
    }


}
