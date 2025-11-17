package shticell.manage;

public class SheetDoesntExist extends RuntimeException {

    public SheetDoesntExist(String sheetName) { super("Sheet doesn't exist " + sheetName     );}
}
