package shticell.client.component.management;

public class PermissionDto {

    private String sheetName;
    private String userName;
    private PermissionType permissionType;
    private boolean isActive;
    private String statusMessage;
    private int permissionID; // מזהה ייחודי לכל בקשה


    public PermissionDto(String sheetName, String userName, PermissionType type, boolean isActive, String statusMessage, int permissionID) {
        this.sheetName = sheetName;
        this.userName = userName;
        this.permissionType = type;
        this.isActive = isActive;
        this.statusMessage = statusMessage;
        this.permissionID = permissionID;

    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getPermissionID() {
        return permissionID;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getUserName() {
        return userName;
    }
    public boolean isActive() {
        return isActive;
    }
}