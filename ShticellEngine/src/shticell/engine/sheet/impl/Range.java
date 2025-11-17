package shticell.engine.sheet.impl;

import shticell.engine.coordinate.Coordinate;
import shticell.engine.coordinate.CoordinateFactory;

import java.util.ArrayList;
import java.util.List;

public class Range {
    private final String name;
    private final Coordinate from;
    private final Coordinate to;

    public Range(String name, String fromCell, String toCell) {
        this.name = name;
        this.from = parseCoordinate(fromCell);
        this.to = parseCoordinate(toCell);
    }

    public String getName() {
        return name;
    }

    public Coordinate getFrom() {
        return from;
    }

    public Coordinate getTo() {
        return to;
    }

    public boolean containsCell(Coordinate coordinate) {
        return coordinate.getRow() >= from.getRow() && coordinate.getRow() <= to.getRow() &&
                coordinate.getColumn() >= from.getColumn() && coordinate.getColumn() <= to.getColumn();
    }

    public List<Coordinate> getAllCellsInRange() {
        List<Coordinate> cells = new ArrayList<>();
        for (int row = from.getRow(); row <= to.getRow(); row++) {
            for (int col = from.getColumn(); col <= to.getColumn(); col++) {
                cells.add(CoordinateFactory.createCoordinate(row, col));
            }
        }
        return cells;
    }

    private Coordinate parseCoordinate(String cellReference) {
        if (cellReference == null || cellReference.length() < 2) {
            return null;
        }
        int row = Integer.parseInt(cellReference.substring(1)) - 1;
        char colChar = cellReference.charAt(0);
        int col = colChar - 'A';
        return CoordinateFactory.createCoordinate(row, col);
    }


}
