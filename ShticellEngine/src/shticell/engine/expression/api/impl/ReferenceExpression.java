package shticell.engine.expression.api.impl;

import shticell.engine.cell.api.Cell;
import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.CellImpl;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.expression.api.Expression;
import shticell.engine.sheet.impl.SheetImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static shticell.engine.expression.parser.FunctionParser.parseCellId;

public class ReferenceExpression implements Expression {

    private final String cellId;
    private CellType resultType; // Field to store the result type

    public ReferenceExpression(String cellId) {
        this.cellId = cellId;
        this.resultType = null; // Initially set to null to indicate it's not determined yet
    }

    @Override
    public EffectiveValue eval(SheetImpl sheet, Coordinate currentCoordinate) {
        Coordinate coordinate = parseCellId(cellId);

        if (coordinate == null) {
            this.resultType = CellType.STRING; // Set resultType explicitly here
            return new EffectiveValueImpl(CellType.STRING, "!REF!"); // Invalid reference

        }

        // Check for circular reference using the set of visited cells
        if (currentCoordinate != null && isCircularReference(sheet, currentCoordinate, coordinate, new HashSet<>())) {
            throw new IllegalStateException("Circular reference on cells " + cellId + ", " + currentCoordinate.toString());
        }

        Cell currentCell = sheet.getCell(currentCoordinate.getRow(), currentCoordinate.getColumn());

        Cell referencedCell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());

        if (referencedCell == null) {

            if (currentCell.getOriginalValue().toUpperCase().contains("CONCAT") || currentCell.getOriginalValue().toUpperCase().contains("SUB")) {
                this.resultType = CellType.STRING; // Set resultType explicitly here
                return new EffectiveValueImpl(CellType.STRING, "!REF!"); // Invalid reference
            }
            else if (currentCell.getOriginalValue().toUpperCase().contains("PLUS") || currentCell.getOriginalValue().toUpperCase().contains("MINUS") || currentCell.getOriginalValue().toUpperCase().contains("TIMES") || currentCell.getOriginalValue().toUpperCase().contains("DIVIDE") || currentCell.getOriginalValue().toUpperCase().contains("MOD") || currentCell.getOriginalValue().toUpperCase().contains("POW") || currentCell.getOriginalValue().toUpperCase().contains("ABS")) {
                {
                    sheet.setCell(coordinate.getRow(), coordinate.getColumn(), "0.0", false);
                    return new EffectiveValueImpl(CellType.NUMERIC, 0.0); // Invalid reference
                }

             } else{
                sheet.setCell(coordinate.getRow(), coordinate.getColumn(), "", false);
                return new EffectiveValueImpl(CellType.NUMERIC, ""); // Invalid reference
            }
        }



        // Add the referenced cell as a dependency for the current cell
        ((CellImpl) currentCell).addDependencyOn((CellImpl) referencedCell); // Add dependency
        ((CellImpl) referencedCell).addInfluencingOn((CellImpl) currentCell); // Track influence


        // Retrieve the effective value of the referenced cell
        EffectiveValue referencedValue = referencedCell.getEffectiveValue();





        // Dynamically set the resultType based on the referenced cell's value
        this.resultType = referencedValue.getCellType();


        return referencedValue;
    }

    private boolean isCircularReference(SheetImpl sheet, Coordinate startCoordinate, Coordinate targetCoordinate, Set<Coordinate> visited) {
        if (visited.contains(targetCoordinate)) {
            return true; // Circular reference detected
        }

        visited.add(targetCoordinate);

        Cell targetCell = sheet.getCell(targetCoordinate.getRow(), targetCoordinate.getColumn());
        if (targetCell instanceof CellImpl) {
            List<Cell> dependencies = ((CellImpl) targetCell).getDependsOn();
            for (Cell dependency : dependencies) {
                if (dependency != null && dependency.getCoordinate().equals(startCoordinate)) {
                    return true; // Direct circular reference detected
                }
                if (isCircularReference(sheet, startCoordinate, dependency.getCoordinate(), visited)) {
                    return true; // Indirect circular reference detected
                }
            }
        }

        return false;
    }

    @Override
    public CellType getFunctionResultType() {

        if (this.resultType == null) {
            // Ensure resultType is never null when accessed
            throw new IllegalStateException("Result type is not determined yet for REF expression: " + cellId);
        }
        return this.resultType; // Return the dynamically determined type
    }


    private boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}


