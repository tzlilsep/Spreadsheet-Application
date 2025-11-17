package shticell.engine.expression.api;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.sheet.impl.SheetImpl;

public interface Expression {
    EffectiveValue eval(SheetImpl currentSheet, Coordinate currentCoordinate);
    CellType getFunctionResultType();
}