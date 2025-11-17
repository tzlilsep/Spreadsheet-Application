package shticell.dto;


import shticell.manage.PermissionType;

import java.util.List;

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
        this.data = data;
        this.columnsWidth = columnsWidth;
        this.rowsHeight = rowsHeight;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.permissionType = permissionType;
        this.version = version;

    }

    public int getVersion()
    {
        return version;
    }

    // Getters and setters
    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
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
    public int getNumCols() {
        return numColumns;
    }

    public String getOwner()
    {
      return owner;
    }


    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }
}
