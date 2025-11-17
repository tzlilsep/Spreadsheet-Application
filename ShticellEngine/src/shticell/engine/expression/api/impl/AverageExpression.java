package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.Range;
import shticell.engine.sheet.impl.SheetImpl;

import java.util.List;

public class AverageExpression implements Expression {

    private final String rangeName;

    public AverageExpression(String rangeName) {
        this.rangeName = rangeName;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        Range range = sheet.getRange(rangeName);

        if (range == null) {
            throw new IllegalArgumentException("Range " + rangeName + " does not exist.");
        }

        List<Coordinate> coordinates = range.getAllCellsInRange();
        double sum = 0;
        int count = 0;

        for (Coordinate coordinate : coordinates) {
            EffectiveValue cellValue = sheet.getCell(coordinate.getRow(), coordinate.getColumn()).getEffectiveValue();

            if (cellValue.getCellType() == CellType.NUMERIC) {

                Double numericValue = (Double) cellValue.getValue();
                if (!Double.isNaN(numericValue)) {

                    sum += cellValue.extractValueWithExpectation(Double.class);
                    count++;
                }
            }
        }

        if (count == 0) {
            throw new IllegalArgumentException("Range " + rangeName + " contains no numeric cells.");
        }

        double average = sum / count;
        return new EffectiveValueImpl(CellType.NUMERIC, average);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
