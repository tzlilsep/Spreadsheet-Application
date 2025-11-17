package shticell.manage;

public class PermissionAlreadyRequested extends Exception {

    public PermissionAlreadyRequested(String sheetName, String userName) { super("Permission already requested for sheet " + sheetName  + " User "+ userName      );}
}
