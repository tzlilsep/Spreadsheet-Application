package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class OrExpression implements Expression {

    private final Expression arg1;
    private final Expression arg2;

    public OrExpression(Expression arg1, Expression arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue val1 = arg1.eval(sheet, currentCoordinate);
        EffectiveValue val2 = arg2.eval(sheet, currentCoordinate);

        Boolean bool1 = Boolean.FALSE;
        Boolean bool2 = Boolean.FALSE;
        try
        {
            // Ensure both left and right are of numeric type
            bool1 = val1.extractValueWithExpectation(Boolean.class);
            bool2 = val2.extractValueWithExpectation(Boolean.class);
        }
        catch (Exception e)
        {
            return new EffectiveValueImpl(CellType.BOOLEAN, "UNKNOWN");
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, bool1 || bool2);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
