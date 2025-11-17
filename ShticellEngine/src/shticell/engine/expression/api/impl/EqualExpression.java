package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

public class EqualExpression implements Expression {

    private final Expression arg1;
    private final Expression arg2;

    public EqualExpression(Expression arg1, Expression arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue val1 = arg1.eval(sheet, currentCoordinate);
        EffectiveValue val2 = arg2.eval(sheet, currentCoordinate);

        // Check if types match
        if (val1.getCellType() != val2.getCellType()) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false); // Return false if types don't match
        }

        // Compare the values based on type
        switch (val1.getCellType()) {
            case NUMERIC:
                Double num1 = val1.extractValueWithExpectation(Double.class);
                Double num2 = val2.extractValueWithExpectation(Double.class);
                return new EffectiveValueImpl(CellType.BOOLEAN, num1.equals(num2));
            case STRING:
                String str1 = val1.extractValueWithExpectation(String.class);
                String str2 = val2.extractValueWithExpectation(String.class);
                return new EffectiveValueImpl(CellType.BOOLEAN, str1.equals(str2));
            case BOOLEAN:
                Boolean bool1 = val1.extractValueWithExpectation(Boolean.class);
                Boolean bool2 = val2.extractValueWithExpectation(Boolean.class);
                return new EffectiveValueImpl(CellType.BOOLEAN, bool1.equals(bool2));
            default:
                throw new IllegalArgumentException("Unsupported type for EQUAL function.");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
