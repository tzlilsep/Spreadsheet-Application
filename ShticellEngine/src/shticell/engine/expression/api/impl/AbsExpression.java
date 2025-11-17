package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class AbsExpression implements Expression {

    private final Expression argument;

    public AbsExpression(Expression argument) {
        this.argument = argument;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue argValue = argument.eval(sheet,currentCoordinate);

        if (argValue.getCellType() == CellType.NUMERIC) {


            Double numericValue=0.0;

            try
            {
                // Ensure both left and right are of numeric type
                numericValue = argValue.extractValueWithExpectation(Double.class);

            }
            catch (Exception e)
            {
                return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
            }

            if (numericValue == null) {
                return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Handle invalid numeric values
            }

            double result = Math.abs(numericValue);
            return new EffectiveValueImpl(CellType.NUMERIC, result);
        }
        return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Return undefined if types mismatch
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
