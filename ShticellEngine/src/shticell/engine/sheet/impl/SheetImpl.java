package shticell.engine.sheet.impl;

import shticell.engine.cell.api.Cell;
import shticell.engine.cell.api.CellType;
import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.cell.impl.CellImpl;
import shticell.engine.cell.impl.EffectiveValueImpl;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.coordinate.CoordinateFactory;
import shticell.engine.expression.api.Expression;
import shticell.engine.expression.api.impl.IdentityExpression;
import shticell.engine.expression.parser.FunctionParser;
import shticell.engine.sheet.api.Sheet;

import java.util.*;

public class SheetImpl implements Sheet {

    private Map<Coordinate, Cell> activeCells;
    private String sheetName;
    private int numRows;
    private int numCols;
    private int columnWidth;
    private int rowHeight;
    private int version;
    public final VersionManager versionManager = new VersionManager(); // Initialize here
    private final Map<String, Range> ranges = new HashMap<>(); // Map to store ranges
    private String owner;
    //private String permissionType;


    public SheetImpl(int numRows, int numCols, String sheetName, int columnWidth, int rowHeight,String ownerUseerName) {
        this.activeCells = new HashMap<>();
        this.numRows = numRows;
        this.numCols = numCols;
        this.sheetName = sheetName;
        this.columnWidth = columnWidth;
        this.rowHeight = rowHeight;
        this.version = 1;
        this.owner=ownerUseerName;
        versionManager.saveVersion(activeCells); // Save the initial state
    }


    public SheetImpl() {
        this.activeCells = new HashMap<>();
        // versionManager is already initialized above
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public Map<Coordinate, Cell> getActiveCells() {
        return activeCells;
    }

    public void setActiveCells(Map<Coordinate, Cell> activeCells) {
        this.activeCells = activeCells;
    }
    public void addActiveCell(Coordinate coordinate, Cell cell) {this.activeCells.put(coordinate,cell);}

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public Cell getCell(int row, int column) {
        return activeCells.get(CoordinateFactory.createCoordinate(row, column));
    }


    @Override
    public void setCell(int row, int column, String value, boolean addVer) {
        versionManager.count++;

        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
        Cell cell = activeCells.get(coordinate);

        // If the cell doesn't exist yet, create a new one
        if (cell == null) {
            cell = new CellImpl(row, column, value, versionManager.getCurrentVersion());
            activeCells.put(coordinate, cell);
        }

        // Store the previous value in case we need to rollback after a failed calculation
        String previousOriginalValue = cell.getOriginalValue();

//        // Check if the value is numeric and has string dependencies (e.g., CONCAT function)
//        if (valueIsNumeric(value) && hasStringDependent(cell)) {
//            throw new IllegalArgumentException("Cell " + coordinate.toString() + " is involved in a string function and must be a string.");
//        }
//        if (!valueIsNumeric(value) && hasNumDependent(cell)) {
//            throw new IllegalArgumentException("Cell " + coordinate.toString() + " is involved in a numeric function and must be a number.");
//        }

        // Temporarily set the original value to the new value
        cell.setCellOriginalValue(value);

        try {
            // Try calculating the effective value
            cell.calculateEffectiveValue(this);

            // If everything works, recalculate dependent cells and manage versioning
            recalculate(cell, addVer);

            if (addVer) {
                cell.setVersion(versionManager.getCurrentVersion());
                versionManager.saveVersion(activeCells); // Save the current version state
                versionManager.incrementVersion(); // Increment version number once after all updates
                version = versionManager.getCurrentVersion() - 1;
                versionManager.count = 0;
            }

        } catch (IllegalArgumentException e) {
            // If calculation fails, rollback the original value to its previous state
            cell.setCellOriginalValue(previousOriginalValue);

            // Propagate the exception after rollback
            throw new IllegalArgumentException("Error on cell " + coordinate.toString() + ": " + e.getMessage(), e);
        }
    }



    @Override
    public void initializeCell(int row, int column, String value) {

        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
        Cell cell = new CellImpl(row, column, value, versionManager.getCurrentVersion());
        activeCells.put(coordinate, cell);

        CellType type;

        if (isBoolean(value)) {
            type = CellType.BOOLEAN;
        } else if (isNumeric(value)) {
            type = CellType.NUMERIC;
        } else {
            type = CellType.STRING;


        }

        EffectiveValueImpl effectiveValue = new EffectiveValueImpl(type,value);
        cell.setEffectiveValue(effectiveValue);

    }

    private boolean valueIsNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean hasStringDependent(Cell cell) {
        for (Cell dependentCell : cell.getInfluencingOn()) {
            if (dependentCell.getOriginalValue().contains("CONCAT")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNumDependent(Cell cell) {
        for (Cell dependentCell : cell.getInfluencingOn()) {
            if (dependentCell.getOriginalValue().contains("PLUS")) {
                return true;
            }
            if (dependentCell.getOriginalValue().contains("MINUS")) {
                return true;
            }

        }
        return false;
    }


    @Override
    public void recalculate(Cell updatedCell, boolean addVer) {


        Set<Cell> visitedCells = new HashSet<>();
        try {
            recalculate(updatedCell, visitedCells);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void recalculate(Cell updatedCell, Set<Cell> visitedCells) {
        // בדוק אם התא הנוכחי נמצא ברשימת התאים שבהם ביקרנו עד כה כדי לזהות תלות מעגלית אמיתית
        if (visitedCells.contains(updatedCell)) {
            return;
        }

        visitedCells.add(updatedCell);  // הוסף את התא הנוכחי לרשימת התאים המבוקרים

        // חישוב מחדש עבור כל התאים שתא זה תלוי בהם
        List<Cell> dependentCells = new ArrayList<>(updatedCell.getDependsOn());
        for (Cell dependentCell : dependentCells) {
            dependentCell.calculateEffectiveValue(this); // חישוב הערך האפקטיבי של התא התלוי
            recalculate(dependentCell, visitedCells);    // קריאה רקורסיבית לחישוב התאים התלויים
        }

        // עדכן את התאים המושפעים (זהו המקרה שבו תא משפיע על תא אחר)
        List<Cell> influencedCells = new ArrayList<>(updatedCell.getInfluencingOn());
        for (Cell influencedCell : influencedCells) {
            influencedCell.calculateEffectiveValue(this); // חישוב הערך האפקטיבי של התא המושפע
            // לא נבדוק את התאים המושפעים ברשימת התאים המבוקרים כי השפעה אינה יוצרת תלות מעגלית
            recalculate(influencedCell, new HashSet<>(visitedCells)); // שימוש בסט חדש עבור התאים המושפעים כדי למנוע זיהוי שגוי של תלות מעגלית
        }

        visitedCells.remove(updatedCell);  // הסר את התא הנוכחי מהרשימה לאחר סיום החישוב שלו
    }


    // Utility method to center text
    private String centerText(String text, int width) {
        if (text == null || text.isEmpty()) {
            text = " ";
        }
        int padding = (width - text.length()) / 2;
        int paddingRight = width - text.length() - padding;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, paddingRight));
    }
    public Map<Coordinate, Cell> revertToVersion(int version) {
        if (versionManager.hasVersion(version)) {
            // Return the snapshot of the selected version
            return versionManager.getVersion(version);
        } else {
            throw new IllegalArgumentException("Version " + version + " does not exist.");
        }
    }

    public void restoreOriginalState() {
        // Restore the state to the latest version
        setActiveCells(versionManager.getVersion(versionManager.getCurrentVersion()));
    }


    public String getSheetName() {
        return sheetName;
    }

    public int getNumRows()
    {
        return numRows;
    }

    public int getNumCols()
    {
        return numCols;
    }

   public int getColumnsWidth()
    {
        return columnWidth;
    }
    public int getRowsHight()
    {
        return rowHeight;
    }

    public boolean hasCircularReferences() {
        Set<Cell> visited = new HashSet<>();
        Set<Cell> recStack = new HashSet<>();  // Stack to keep track of the recursion path

        for (Cell cell : activeCells.values()) {
            if (hasCircularReferencesUtil(cell, visited, recStack)) {
                return true;  // A circular reference was found
            }
        }
        return false;  // No circular references found
    }

    private boolean hasCircularReferencesUtil(Cell cell, Set<Cell> visited, Set<Cell> recStack) {
        if (recStack.contains(cell)) {
            return true;  // Circular reference detected
        }

        if (visited.contains(cell)) {
            return false;  // Already processed, no circular reference here
        }

        visited.add(cell);
        recStack.add(cell);

        for (Cell dependentCell : cell.getInfluencingOn()) {
            if (hasCircularReferencesUtil(dependentCell, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(cell);
        return false;
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


    @Override
    public void defineRange(String rangeName, String fromCell, String toCell) {
        // Convert cell references to uppercase
        fromCell = fromCell.toUpperCase();
        toCell = toCell.toUpperCase();

        // Check if the range name already exists
        if (ranges.containsKey(rangeName)) {
            throw new IllegalArgumentException("Range name already exists: " + rangeName);
        }

        // Create the range using the provided Range class
        Range range = new Range(rangeName, fromCell, toCell);

        // Extract the from and to coordinates from the range
        Coordinate from = range.getFrom();
        Coordinate to = range.getTo();

        // Check if the range is within sheet boundaries
        if (from.getRow() < 0 || to.getRow() >= numRows || from.getColumn() < 0 || to.getColumn() >= numCols) {
            throw new IllegalArgumentException("Range is out of sheet bounds.");
        }

        // Check if 'to' is logically after 'from'
        if (to.getRow() < from.getRow() || (to.getRow() == from.getRow() && to.getColumn() < from.getColumn())) {
            throw new IllegalArgumentException("'To' cell must be after 'From' cell.");
        }

        // Store the valid range in the ranges map
        ranges.put(rangeName, range);
    }


    public Range getRange(String rangeName) {
        return ranges.get(rangeName);  // Return the range by name, or null if not found
    }


    @Override
    public String getEffectiveValue(int row, int col) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, col);
        Cell cell = activeCells.get(coordinate);

        if (cell != null) {
            EffectiveValue effectiveValue = cell.getEffectiveValue();

            if (effectiveValue != null) {
                // Check the CellType and return the appropriate value as a String
                switch (effectiveValue.getCellType()) {
                    case BOOLEAN:
                        return Boolean.parseBoolean(effectiveValue.getValue().toString()) ? "TRUE" : "FALSE";
                    case NUMERIC:
                        return effectiveValue.getValue().toString(); // Numeric values can be displayed as-is
                    case STRING:
                        return effectiveValue.getValue().toString(); // String values can be displayed as-is
                    default:
                        return "";
                }
            }
        }
        return ""; // Return an empty string if cell or effective value is null
    }

    public void removeRange(String name) {
        ranges.remove(name);
    }
    public Map<String, Range> getRanges() {
        return ranges;
    }


    // Getter and setter for the owner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
