package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class NotExpression implements Expression {

    private final Expression exp;

    public NotExpression(Expression exp) {
        this.exp = exp;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue val = exp.eval(sheet, currentCoordinate);

        Boolean bool = Boolean.FALSE;
        try
        {
            // Ensure both left and right are of numeric type
            bool = val.extractValueWithExpectation(Boolean.class);
        }
        catch (Exception e)
        {
            return new EffectiveValueImpl(CellType.BOOLEAN,"UNKNOWN");
        }


        return new EffectiveValueImpl(CellType.BOOLEAN, !bool);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
