package shticell.engine.sheet.api;

import shticell.engine.cell.api.Cell;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.sheet.impl.VersionManager;

import java.util.Map;

public interface Sheet {
    int getVersion();

    Cell getCell(int row, int column);

    public void setActiveCells(Map<Coordinate, Cell> activeCells);

    public Map<Coordinate, Cell> getActiveCells();

    public Map<Coordinate, Cell> revertToVersion(int version);

    public void recalculate(Cell updatedCell, boolean addVer);

    public VersionManager getVersionManager();

    public void setCell(int row, int column, String value, boolean addVer);

    public void initializeCell(int row, int column, String value);

    String getSheetName();

    int getNumRows();

    int getNumCols();

    int getColumnsWidth();

    int getRowsHight();


     String getEffectiveValue(int row, int col);

     void defineRange(String name, String fromCell, String toCell);

}

