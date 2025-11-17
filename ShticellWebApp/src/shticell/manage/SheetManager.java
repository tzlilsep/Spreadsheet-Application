package shticell.manage;

import shticell.dto.PermissionRequestDto;
import shticell.engine.loadingXML.LoadingXML;
import shticell.engine.sheet.impl.SheetImpl;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class SheetManager {

    //username to list of sheets
    private final Map<String, SheetImpl> sheets = new HashMap<>();

    // sheetName -> list<Permission>
    private final Map<String, List<Permission>> permissions = new HashMap<>();

    // sheetName -> version
    public final Map<String, Integer> sheetVersions = new HashMap<>();


    // version counter
    private int version = 0;


    // Get the current version of the data
    public int getVersion() {
        return version;
    }

    // Increment the version whenever data changes
    private void incrementVersion() {
        version++;
    }


    public boolean hasPermissions(String userName, String sheetName, PermissionType type) {

       if(!permissions.containsKey(sheetName)) return false;

       List<Permission> permissionList = permissions.get(sheetName);

       return permissionList.stream().anyMatch(p -> p.getUserName().equals(userName) && p.getIsActive() && (
               p.getPermissionType() == type || p.getPermissionType() == PermissionType.OWNER
               ));
    }


    public void requestPermission(PermissionRequestDto dto) throws PermissionAlreadyRequested,SheetDoesntExist {

        String sheetName = dto.getSheetName(),
                userName = dto.getUserName();
        PermissionType permissionType = dto.getPermissionType();
        int perID = dto.getPermissionID();



        if(!permissions.containsKey(sheetName)) {
            permissions.put(sheetName, new ArrayList<>(List.of(new Permission(sheetName, userName , permissionType, false, "PENDING", perID))));
        }
        else {
            Permission permission = new Permission(sheetName, userName , permissionType, false, "PENDING", perID);
            permissions.get(sheetName).add(permission);
        }

        incrementVersion();
    }

    public void approvePermission(String sheetName, String username, int perID) {
        List<Permission> sheetPermissions = permissions.get(sheetName);

        if (sheetPermissions == null) {
            throw new IllegalArgumentException("Sheet not found");
        }

        for (Permission permission : sheetPermissions) {
            if (permission.getUserName().equals(username) && permission.getPermissionID() == perID) {
                permission.setActive(true);
                permission.setStatusMessage("APPROVED");
            } else if (permission.getUserName().equals(username)) {
                permission.setActive(false);
            }
        }
        incrementVersion(); // Ensure version increments on permission updates
    }



    public void rejectPermission(String sheetName, String username, int perID) {
        List<Permission> sheetPermissions = permissions.get(sheetName);

        if (sheetPermissions == null) {
            throw new IllegalArgumentException("Sheet not found");
        }

        for (Permission permission : sheetPermissions) {
            if (permission.getUserName().equals(username) && permission.getPermissionID() == perID) {
                permission.setStatusMessage("REJECTED ");
            }
        }
        incrementVersion(); // Ensure version increments on permission updates
    }



    public List<Permission> getPermissions(String sheetName) {
        List<Permission> sheetPermissions = permissions.get(sheetName);

        if(sheetPermissions == null) {
            sheetPermissions = new ArrayList<>();
        }

        return sheetPermissions;
    }



    public void addSheet(String sheetName, SheetImpl sheet, String ownerUserName) {
        sheet.setOwner(ownerUserName);  // Assuming setOwner is a method in SheetImpl

        // בדיקה אם כבר קיים גיליון עם אותו שם
        if (sheets.containsKey(sheetName)) {
            throw new IllegalArgumentException("Sheet with the name '" + sheetName + "' already exists.");
        } else {
            // הוספת הגיליון למפה
            sheets.put(sheetName, sheet);
            System.out.println("Sheet '" + sheetName + "' has been added successfully.");
            incrementVersion();
            sheetVersions.put(sheetName, sheet.getVersion());
        }

    }

    public Permission getPermission(String sheetName, String userName,int perID) {
        if(!permissions.containsKey(sheetName)) throw new SheetDoesntExist(sheetName);
        List<Permission> permissionList = permissions.get(sheetName);
        for(Permission permission : permissionList) {
            if(permission.getUserName().equals(userName)&& permission.getPermissionID() == perID) {
                return permission;
            }
        }
        return null;
    }

    public Map<String, SheetImpl> getAllSheets() {
        return sheets;
    }

    public SheetImpl getSheet(String sheetName) {
        return sheets.get(sheetName);
    }


    public Permission getLastApprovedPermission(String sheetName, String userName) {
        
        if (!permissions.containsKey(sheetName)) {
            return null;
        }

        List<Permission> permissionList = permissions.get(sheetName);

        for (Permission permission : permissionList) {
            if (permission.getUserName().equals(userName) && permission.getIsActive()) {
                return permission;
            }
            
        }
        return null;
    }

    public Map<String, Integer> getSheetVersions() {
        return sheetVersions;
    }

    public void setSheetVersions(String key, Integer value) {
        this.sheetVersions.put(key, value);
    }

    public void increaseSheetVersions(String key) {
        this.sheetVersions.put(key, this.sheetVersions.get(key) + 1);
    }
}
