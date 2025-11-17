package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.CellType;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.sheet.impl.SheetImpl;

public class DivideExpression implements Expression {

    private final Expression numerator;
    private final Expression denominator;

    public DivideExpression(Expression numerator, Expression denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {

        EffectiveValue numeratorValue = numerator.eval(sheet,currentCoordinate);
        EffectiveValue denominatorValue = denominator.eval(sheet,currentCoordinate);

        Double num=0.0;
        Double denom=0.0;
        try
        {
            // Ensure both left and right are of numeric type
            num = numeratorValue.extractValueWithExpectation(Double.class);
            denom = denominatorValue.extractValueWithExpectation(Double.class);

        }
        catch (Exception e)
        {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }
        if ( denom == 0.0) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN); // Handle division by zero or invalid values
        }
        else {

            double result = num / denom;
            return new EffectiveValueImpl(CellType.NUMERIC, result);
        }


    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
