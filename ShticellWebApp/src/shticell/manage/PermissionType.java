package shticell.manage;

public enum PermissionType {
    READ, WRITE, OWNER, NONE;
    public String toString() {
        if(this == READ) return  "READ";
        if(this == WRITE) return  "WRITE";
        if(this == OWNER) return  "OWNER";
        if(this == NONE) return  "NONE";

        throw new RuntimeException("Invalid PermissionType");
    }
}
