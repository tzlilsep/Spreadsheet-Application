package shticell.engine.cell.api;

import shticell.engine.cell.impl.CellImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.sheet.impl.SheetImpl;

import java.util.List;

public interface Cell {

        Coordinate getCoordinate();
    String getOriginalValue();
    void setCellOriginalValue(String value);
    EffectiveValue getEffectiveValue();
    void calculateEffectiveValue(SheetImpl sheet);
    int getVersion();
    List<Cell> getDependsOn();
    List<Cell> getInfluencingOn();
     CellImpl clone();
     void setVersion(int version);
     boolean isCalculated() ;
     void setEffectiveValue(EffectiveValue effectiveValue);

     void printCellInfo();

    // צבע טקסט
     String getTextColor();
     void setTextColor(String textColor);

    // צבע רקע
     String getBackgroundColor();
     void setBackgroundColor(String backgroundColor) ;
}