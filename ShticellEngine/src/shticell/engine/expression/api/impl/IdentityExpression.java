package shticell.engine.expression.api.impl;

import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class IdentityExpression implements Expression {

    private final Object value;
    private final CellType type;

    public IdentityExpression(Object value, CellType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        return new EffectiveValueImpl(type, value);
    }

    @Override
    public CellType getFunctionResultType() {
        return type;
    }
}