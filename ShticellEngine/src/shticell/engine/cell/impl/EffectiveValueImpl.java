package shticell.engine.cell.impl;

import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.api.CellType;

public class EffectiveValueImpl implements EffectiveValue, Cloneable {

    private CellType cellType;
    private Object value;

    public EffectiveValueImpl(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
    }

    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public <T> T extractValueWithExpectation(Class<T> expectedClass) {
        if (expectedClass.isInstance(value)) {
            return expectedClass.cast(value);
        } else {

            throw new IllegalArgumentException("Expected value of type " + expectedClass.getSimpleName() + " but got " + value.getClass().getSimpleName());
        }
    }

    @Override
    public EffectiveValueImpl clone() {
        try {
            return (EffectiveValueImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported for EffectiveValueImpl", e);
        }
    }

    @Override
    public String toString() {
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            // בדיקה אם הערך הוא מספר שלם
            if (doubleValue == (long) doubleValue) {
                return String.valueOf((long) doubleValue);
            } else {
                // עיצוב המספר עם 2 ספרות אחרי הנקודה
                return String.format("%.2f", doubleValue);
            }
        }

        // בדיקה אם הערך הוא Boolean והמרה לאותיות גדולות
        if (value instanceof Boolean) {
            return ((Boolean) value).toString().toUpperCase(); // Convert boolean to uppercase
        }

        return String.valueOf(value);
    }

    public void setEffectiveValue(Object value, CellType cellType)
    {
        this.value = value;
        this.cellType = cellType;

    }



}
