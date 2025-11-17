package shticell.dto;

import shticell.engine.cell.api.Cell;

import java.util.ArrayList;
import java.util.List;

public class CellDto {
    public int row;
    public int col;
    private String originalValue;
    private String effectiveValue;
    private String version;
    private String BackgroundColor;
    private String TextColor;
    public List<CellDto> dependsOn;  // Changed to List<CellDto>
    public List<CellDto> influencingOn;  // Changed to List<CellDto>
    public int sheetVersion;

    public CellDto(int row, int col, String originalValue, String effectiveValue, String version, String BackgroundColor, String TextColor, int sheetVersion) {
        this.row = row;
        this.col = col;
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.version = version;
        this.BackgroundColor = BackgroundColor;
        this.TextColor = TextColor;
        this.dependsOn = new ArrayList<>(); // Empty list by default
        this.influencingOn = new ArrayList<>(); // Empty list by default
        this.sheetVersion = sheetVersion;
    }


    public void setDependsOn(List<CellDto> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public void setInfluencingOn(List<CellDto> influencingOn) {
        this.influencingOn = influencingOn;
    }

    // Getters and Setters
    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getEffectiveValue() {
        return effectiveValue;
    }

    public void setEffectiveValue(String effectiveValue) {
        this.effectiveValue = effectiveValue;
    }
//
//    public List<CellDto> getDependsOn() {
//        return dependsOn;
//    }
//
//    public void setDependsOn(List<CellDto> dependsOn) {
//        this.dependsOn = dependsOn;
//    }
//
//    public List<CellDto> getInfluencingOn() {
//        return influencingOn;
//    }
//
//    public void setInfluencingOn(List<CellDto> influencingOn) {
//        this.influencingOn = influencingOn;
//    }

    // Method to print cell information
    public void printCellInfo() {
        System.out.println("Cell Information:");
        System.out.println("Original Value: " + (originalValue != null ? originalValue : "N/A"));
        System.out.println("Effective Value: " + (effectiveValue != null ? effectiveValue : "N/A"));
//
//        // Print dependencies
//        System.out.println("Depends On:");
//        if (dependsOn != null && !dependsOn.isEmpty()) {
//            dependsOn.forEach(dep -> System.out.println(" - " + dep.getEffectiveValue()));
//        } else {
//            System.out.println(" - None");
//        }
//
//        // Print influencing cells
//        System.out.println("Influencing On:");
//        if (influencingOn != null && !influencingOn.isEmpty()) {
//            influencingOn.forEach(inf -> System.out.println(" - " + inf.getEffectiveValue()));
//        } else {
//            System.out.println(" - None");
//        }
    }

}
