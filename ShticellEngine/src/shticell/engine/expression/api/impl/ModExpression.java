package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class ModExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public ModExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue leftValue = left.eval(sheet,currentCoordinate);
        EffectiveValue rightValue = right.eval(sheet,currentCoordinate);

        if (leftValue.getCellType() == CellType.NUMERIC && rightValue.getCellType() == CellType.NUMERIC) {


            Double leftNumeric=0.0;
            Double rightNumeric=0.0;
            try
            {
                // Ensure both left and right are of numeric type
                leftNumeric = leftValue.extractValueWithExpectation(Double.class);
                rightNumeric = rightValue.extractValueWithExpectation(Double.class);

            }
            catch (Exception e)
            {
                return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
            }

            if (leftNumeric == null || rightNumeric == null || rightNumeric == 0.0) {
                return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Handle division by zero or invalid values
            }

            double result = leftNumeric % rightNumeric;
            return new EffectiveValueImpl(CellType.NUMERIC, result);
        }
        return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Return undefined if types mismatch
    }
    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
