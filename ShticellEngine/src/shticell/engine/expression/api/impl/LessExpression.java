package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class LessExpression implements Expression {

    private final Expression arg1;
    private final Expression arg2;

    public LessExpression(Expression arg1, Expression arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue val1 = arg1.eval(sheet, currentCoordinate);
        EffectiveValue val2 = arg2.eval(sheet, currentCoordinate);



        Double num1=0.0;
        Double num2=0.0;
        try
        {
            // Ensure both left and right are of numeric type
            num1 = val1.extractValueWithExpectation(Double.class);
            num2 = val2.extractValueWithExpectation(Double.class);
        }
        catch (Exception e)
        {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, num1 <= num2);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
