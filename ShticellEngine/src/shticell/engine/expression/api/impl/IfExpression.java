package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class IfExpression implements Expression {

    private final Expression condition;
    private final Expression thenExpr;
    private final Expression elseExpr;

    public IfExpression(Expression condition, Expression thenExpr, Expression elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue conditionValue = condition.eval(sheet, currentCoordinate);

        if (conditionValue.getCellType() != CellType.BOOLEAN) {

            return new EffectiveValueImpl(CellType.BOOLEAN, "UNKNOWN");
        }

        Boolean conditionResult = conditionValue.extractValueWithExpectation(Boolean.class);









        EffectiveValue result = conditionResult ? thenExpr.eval(sheet, currentCoordinate) : elseExpr.eval(sheet, currentCoordinate);



        // Validate that both branches return the same type
        EffectiveValue thenResult = thenExpr.eval(sheet, currentCoordinate);
        EffectiveValue elseResult = elseExpr.eval(sheet, currentCoordinate);

        if (thenResult.getCellType() != elseResult.getCellType()) {
            return new EffectiveValueImpl(CellType.BOOLEAN, "UNKNOWN");
        }

        return result;
    }

    @Override
    public CellType getFunctionResultType() {
        EffectiveValue thenResult = thenExpr.eval(null, null);
        return thenResult.getCellType(); // Assuming 'then' and 'else' return the same type.
    }
}
