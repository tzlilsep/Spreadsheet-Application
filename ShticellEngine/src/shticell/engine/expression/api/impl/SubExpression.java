package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class SubExpression implements Expression {

    private final Expression source;
    private final Expression startIndex;
    private final Expression endIndex;

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        this.source = source;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        EffectiveValue sourceValue = source.eval(sheet, currentCoordinate);
        EffectiveValue startValue = startIndex.eval(sheet, currentCoordinate);
        EffectiveValue endValue = endIndex.eval(sheet, currentCoordinate);

        if (sourceValue.getCellType() == CellType.STRING &&
                startValue.getCellType() == CellType.NUMERIC &&
                endValue.getCellType() == CellType.NUMERIC) {


            String sourceStr="";
            try
            {
                // Ensure both left and right are of numeric type
                sourceStr = sourceValue.extractValueWithExpectation(String.class);

            }
            catch (Exception e)
            {
                return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
            }


            // Convert start and end indexes to integers
            Integer startIdx = convertToInteger(startValue);
            Integer endIdx = convertToInteger(endValue);

            // Ensure indices are valid for the string length
            if (sourceStr == null || startIdx == null || endIdx == null ||
                    startIdx < 0 || endIdx >= sourceStr.length() || startIdx > endIdx) {
                return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!"); // Handle invalid indices or source string
            }

            // Extract substring using 0-based indexing
            String result = sourceStr.substring(startIdx, endIdx + 1);
            return new EffectiveValueImpl(CellType.STRING, result);
        }
        return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!"); // Return undefined if types mismatch
    }

    private Integer convertToInteger(EffectiveValue value) {
        if (value.getCellType() == CellType.NUMERIC) {
            Double numericValue = value.extractValueWithExpectation(Double.class);
            return numericValue != null ? numericValue.intValue() : null;
        }
        return null;
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}
