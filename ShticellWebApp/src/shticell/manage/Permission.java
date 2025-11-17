package shticell.manage;

public class Permission {

    private String sheetName;
    private String userName;
    private PermissionType permissionType;
    private boolean isActive;
    private String statusMessage;
    private int permissionID; // מזהה ייחודי לכל בקשה



    public Permission(String sheetName, String userName, PermissionType type, boolean isActive, String statusMessage, int permissionID) {
        this.sheetName = sheetName;
        this.userName = userName;
        this.permissionType = type;
        this.isActive = isActive;
        this.statusMessage = statusMessage;
        this.permissionID = permissionID;

    }

    public boolean getIsActive() {
        return isActive;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getUserName() {
        return userName;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public int getPermissionID() {
        return permissionID;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
