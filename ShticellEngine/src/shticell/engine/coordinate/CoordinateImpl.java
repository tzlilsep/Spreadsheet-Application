package shticell.engine.coordinate;

public class CoordinateImpl implements Coordinate {
    private final int row;
    private final int column;

    public CoordinateImpl(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateImpl that = (CoordinateImpl) o;

        if (row != that.row) return false;
        return column == that.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String toString() {
        // Convert the column number to a letter (A=0, B=1, C=2, etc.)
        char columnLetter = (char) ('A' + column);
        // Convert the row number to 1-based index for display (e.g., row 0 becomes 1)
        int displayRow = row + 1;
        // Return the formatted string
        return "" + displayRow+ columnLetter ;
    }
}
