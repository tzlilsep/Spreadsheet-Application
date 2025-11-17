package shticell.engine.cell.impl;

import shticell.engine.cell.api.Cell;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.coordinate.CoordinateFactory;
import shticell.engine.coordinate.CoordinateImpl;
import shticell.engine.expression.api.Expression;
import shticell.engine.expression.parser.FunctionParser;
import shticell.engine.sheet.impl.Range;
import shticell.engine.sheet.impl.SheetImpl;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CellImpl implements Cell, Cloneable  {

    private final Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    private  List<Cell> dependsOn;
    private  List<Cell> influencingOn;
    private boolean calculated;  // New field to track if the value is calculated


    private String textColor;  // צבע טקסט של התא
    private String backgroundColor;  // צבע רקע של התא

    public CellImpl(int row, int column, String originalValue, int version) {
        this.coordinate = new CoordinateImpl(row, column);
        this.originalValue = originalValue;
        this.version = version;
        this.dependsOn = new ArrayList<>();
        this.influencingOn = new ArrayList<>();
        this.calculated = false;  // Initially, the value is not calculated


        this.textColor = "#000000";  // צבע טקסט ברירת מחדל (שחור)
        this.backgroundColor = "#FFFFFF";  // צבע רקע ברירת מחדל (לבן)

    }

    @Override
    public void printCellInfo() {
        System.out.println("Cell Information:");
        System.out.println("Coordinate: " + coordinate);
        System.out.println("Original Value: " + (originalValue != null ? originalValue : "N/A"));
        System.out.println("Effective Value: " + (effectiveValue != null ? effectiveValue : "N/A"));
    }
        @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public void setCellOriginalValue(String value) {
        this.originalValue = value;
        this.calculated = false;  // Reset the calculated flag when the original value is changed

    }
    @Override
    // New method to check if the cell's value is calculated
    public boolean isCalculated() {
        return calculated;
    }

    @Override
    public EffectiveValue getEffectiveValue() {
        return effectiveValue;
    }

    @Override
    public void calculateEffectiveValue(SheetImpl sheet) {
        // Clear current dependencies before recalculating
        clearDependencies(sheet);

        // Detect circular reference
        if (sheet.hasCircularReferences()) {
            throw new IllegalArgumentException("Circular reference detected involving cell: " + this.coordinate);
        }

        // Parse the expression from the original value
        Expression expression = FunctionParser.parseExpression(originalValue, coordinate, sheet);

        // Evaluate the expression
        EffectiveValue effectiveValue = expression.eval(sheet, this.coordinate);

        // Set the effective value if the type is valid
        this.effectiveValue = effectiveValue;

        // Mark this cell as calculated
        this.calculated = true;

        // Update the dependencies list
        this.dependsOn = getDependencies(sheet);

        // Handle range-based dependencies
        if (originalValue.toUpperCase().contains("SUM")  || originalValue.toUpperCase().contains("AVERAGE")) {
            updateRangeDependencies(sheet);
        }

        // Update influencing relationships
        for (Cell dependentCell : dependsOn) {
            ((CellImpl) dependentCell).addInfluencingOn(this);
        }

        // Update influencing relationships
        for (Cell influencCell : influencingOn) {
            this.addInfluencingOn((CellImpl) influencCell);
        }
    }



    @Override
    public List<Cell> getDependsOn() {
        return dependsOn;
    }

    public List<Cell> getDependencies(SheetImpl sheet) {


//        List<Cell> dependencies = new ArrayList<>();
        String expression = getOriginalValue();

        // Regex to identify references within curly braces
        if (expression.contains("REF")) {
            String[] parts = expression.split("[,{}]");
            for (String part : parts) {
                part = part.trim();
                if (part.matches("[A-Z]+[0-9]+")) {  // Match patterns like "2B"
                    Coordinate coordinate = parseCellId(part);
                    if (coordinate != null) {
                        Cell dependentCell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());

                        if (dependentCell != null) {
                            if (!dependsOn.contains(dependentCell))
                                dependsOn.add(dependentCell);
                        } else {
                            sheet.setCell(coordinate.getRow(), coordinate.getColumn(), "", false);
                            dependentCell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());
                            sheet.addActiveCell(coordinate, dependentCell);
                            if (!dependsOn.contains(dependentCell))
                                dependsOn.add(dependentCell);
                        }
                    }


                }
            }
        }
        return dependsOn;
    }


    // Implement parseCellId in CellImpl class
    private Coordinate parseCellId(String cellId) {
        try {
            // Extract the column (letters) and row (numbers) from the cell ID
            String columnPart = cellId.replaceAll("\\d", ""); // Extract alphabetic part (column)
            String rowPart = cellId.replaceAll("\\D", ""); // Extract numeric part (row)

            // Convert row and column to zero-based indices
            int row = Integer.parseInt(rowPart) - 1; // Convert to zero-based index
            int column = columnPart.toUpperCase().charAt(0) - 'A'; // Convert 'A' to 0, 'B' to 1, etc.

            // Create and return the coordinate
            return CoordinateFactory.createCoordinate(row, column);
        } catch (Exception e) {
            // Handle any parsing errors
            System.out.println("Error parsing cell ID: " + cellId);
            return null; // Return null if parsing fails
        }
    }


    @Override
    public int getVersion() {
        return version;
    }


    @Override
    public List<Cell> getInfluencingOn() {
        return influencingOn;
    }

    @Override
    public String toString() {
        return coordinate.toString();
    }


    @Override
    public CellImpl clone() {
        try {
            CellImpl cloned = (CellImpl) super.clone();
            // Deep copy of the lists to ensure independence of the clones
            cloned.dependsOn = new ArrayList<>(this.dependsOn);
            cloned.influencingOn = new ArrayList<>(this.influencingOn);
            // Ensure that EffectiveValue is cloned properly
            if (this.effectiveValue != null) {
                cloned.effectiveValue = this.effectiveValue.clone(); // Assuming EffectiveValueImpl implements Cloneable
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported for CellImpl", e);
        }
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    public void clearDependencies(SheetImpl sheet) {
        for (Cell cell : dependsOn) {
            ((CellImpl) cell).removeInfluencingOn(this);
        }
        dependsOn.clear();  // Clear the dependencies after removing influences
    }

    public void addDependencyOn(CellImpl cell) {
        if (!dependsOn.contains(cell)) {
            dependsOn.add(cell);
        }
    }

    public void addInfluencingOn(CellImpl cell) {
        if (!influencingOn.contains(cell)) {
            influencingOn.add(cell);
        }
    }

    public void removeInfluencingOn(CellImpl cell) {
        influencingOn.remove(cell);
    }

    @Override
    public void setEffectiveValue(EffectiveValue effectiveValue)
    {
        this.effectiveValue = effectiveValue;
    }

    public String getValue() {
        return effectiveValue != null ? effectiveValue.getValue().toString() : originalValue;
    }





    @Override

    // צבע טקסט
    public String getTextColor() {
        return textColor;
    }
    @Override

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }
    @Override

    // צבע רקע
    public String getBackgroundColor() {
        return backgroundColor;
    }
    @Override

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    private void updateRangeDependencies(SheetImpl sheet) {
        // Check for range-based functions in the original value
        if (originalValue.toUpperCase().contains("SUM") || originalValue.toUpperCase().contains("AVERAGE")) {
            // Extract the range name from the function
            String rangeName = extractRangeNameFromExpression(originalValue);

            // Get the Range object from the sheet
            Range range = sheet.getRange(rangeName);
            if (range == null) {
                throw new IllegalArgumentException("Range " + rangeName + " does not exist.");
            }

            // Iterate over all cells in the range and add them as dependencies
            for (Coordinate coordinate : range.getAllCellsInRange()) {
                Cell dependentCell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());
                if (dependentCell != null) {
                    addDependencyOn((CellImpl) dependentCell); // Add dependency on each cell in the range
                } else {
                    // Handle cases where cells might not exist yet
                    sheet.setCell(coordinate.getRow(), coordinate.getColumn(), "", false);
                    dependentCell = sheet.getCell(coordinate.getRow(), coordinate.getColumn());
                    sheet.addActiveCell(coordinate, dependentCell);
                    addDependencyOn((CellImpl) dependentCell);
                }
            }
        }
    }

    // Utility function to extract the range name from the function
    private String extractRangeNameFromExpression(String expression) {
        // Assumes the expression is in the format {FUNCTION,rangeName}
        String[] parts = expression.split("[,{}]");
        if (parts.length > 1) {
            return parts[2].trim(); // Return the range name
        }
        throw new IllegalArgumentException("Invalid expression for range-based function.");
    }

}
