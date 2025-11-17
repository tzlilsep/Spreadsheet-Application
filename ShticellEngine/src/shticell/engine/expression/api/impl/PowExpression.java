package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class PowExpression implements Expression {

    private final Expression base;
    private final Expression exponent;

    public PowExpression(Expression base, Expression exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue baseValue = base.eval(sheet,currentCoordinate);
        EffectiveValue exponentValue = exponent.eval(sheet,currentCoordinate);

        if (baseValue.getCellType() == CellType.NUMERIC && exponentValue.getCellType() == CellType.NUMERIC) {

            Double baseNumeric=0.0;
            Double exponentNumeric=0.0;
            try
            {
                // Ensure both left and right are of numeric type
                baseNumeric = baseValue.extractValueWithExpectation(Double.class);
                exponentNumeric = exponentValue.extractValueWithExpectation(Double.class);
            }
            catch (Exception e)
            {
                return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
            }

            if (baseNumeric == null || exponentNumeric == null) {
                return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Handle invalid numeric values
            }

            double result = Math.pow(baseNumeric, exponentNumeric);
            return new EffectiveValueImpl(CellType.NUMERIC, result);
        }
        return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Return undefined if types mismatch
    }
    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
