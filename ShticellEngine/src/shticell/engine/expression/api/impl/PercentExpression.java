package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class PercentExpression implements Expression {

    private final Expression part;
    private final Expression whole;

    public PercentExpression(Expression part, Expression whole) {
        this.part = part;
        this.whole = whole;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue partValue = part.eval(sheet, currentCoordinate);
        EffectiveValue wholeValue = whole.eval(sheet, currentCoordinate);


        Double partNumeric =Double.NaN;
        Double wholeNumeric = Double.NaN;


        try
        {
            // Ensure both left and right are of numeric type
            partNumeric = partValue.extractValueWithExpectation(Double.class);
            wholeNumeric = wholeValue.extractValueWithExpectation(Double.class);
        }
        catch (Exception e)
        {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }


        if (wholeNumeric == 0) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Return NaN if division by zero
        }

        double result = (partNumeric * wholeNumeric) / 100;

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
