package shticell.engine.expression.api.impl;

import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class MinusExpression implements Expression {

    private Expression left;
    private Expression right;

    public MinusExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue leftValue = left.eval(sheet, currentCoordinate);
        EffectiveValue rightValue = right.eval(sheet, currentCoordinate);
        // do some checking... error handling...


//        // Ensure both are numeric after evaluation
//        if (leftValue.getCellType() != CellType.NUMERIC || rightValue.getCellType() != CellType.NUMERIC) {
//            throw new IllegalArgumentException("Invalid argument types for MINUS function. Expected NUMERIC, but got "
//                    + leftValue.getCellType() + " and " + rightValue.getCellType());
//        }

        // Ensure both left and right are of numeric type
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

        //double result = (Double) leftValue.getValue() + (Double) rightValue.getValue();
        double result = leftNumeric - rightNumeric;

        return new EffectiveValueImpl(CellType.NUMERIC, result);


    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}