package shticell.client.component.management;

public class PermissionRequestDto {
    private String sheetName;
    private String userName;
    private PermissionType permissionType;
    private int permissionID; // מזהה ייחודי לכל בקשה


    public PermissionRequestDto(String sheetName, String userName, PermissionType permissionType,int permissionID) {
        this.userName = userName;
        this.sheetName = sheetName;
        this.permissionType = permissionType;
        this.permissionID = permissionID;
    }

    public int getPermissionID() {
        return permissionID;
    }

    public PermissionRequestDto(){}


    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }
}
