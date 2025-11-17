package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class ConcatExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public ConcatExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue leftValue = left.eval(sheet,currentCoordinate);
        EffectiveValue rightValue = right.eval(sheet,currentCoordinate);

        if(leftValue.getValue().equals("!UNDEFINED!")||rightValue.getValue().equals("!UNDEFINED!")) {
            return new EffectiveValueImpl(CellType.STRING,"!UNDEFINED!");

        }



        // Concatenate the strings
        String result = "";
        try
        {
            // Ensure both left and right are of numeric type
            result = leftValue.extractValueWithExpectation(String.class) + rightValue.extractValueWithExpectation(String.class);

        }
        catch (Exception e)
        {
            return new EffectiveValueImpl(CellType.STRING,"!UNDEFINED!");
        }



        return new EffectiveValueImpl(CellType.STRING, result);




    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}
