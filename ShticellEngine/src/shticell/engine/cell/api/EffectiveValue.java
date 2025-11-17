package shticell.engine.cell.api;

import shticell.engine.cell.impl.EffectiveValueImpl;

public interface EffectiveValue {
    CellType getCellType();

    Object getValue();
    <T> T extractValueWithExpectation(Class<T> type);

     EffectiveValueImpl clone() ;
    }