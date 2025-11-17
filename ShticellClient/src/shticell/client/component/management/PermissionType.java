package shticell.client.component.management;

public enum PermissionType {
    READ, WRITE, OWNER,NONE;
    public String toString() {
        if(this == READ) return  "read";
        if(this == WRITE) return  "write";
        if(this == OWNER) return  "owner";

        throw new RuntimeException("Invalid PermissionType");
    }
}
