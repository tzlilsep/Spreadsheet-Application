package shticell.engine.sheet.impl;

import shticell.engine.cell.api.Cell;
import shticell.engine.coordinate.Coordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VersionManager {

    private int currentVersion;
    private final Map<Integer, Map<Coordinate, Cell>> versionHistory;
    private final Map<Integer, Integer> versionChangeCount;
    public int count;

    public VersionManager() {
        this.currentVersion = 1;
        this.versionHistory = new HashMap<>();
        this.versionChangeCount = new HashMap<>();
        this.count=-1;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public Map<Coordinate, Cell> getCurrentVersionMap() {
        return getVersion(currentVersion-1);
    }

    public void incrementVersion() {
        currentVersion++;
    }
    public void saveVersion(Map<Coordinate, Cell> activeCells) {

        Map<Coordinate, Cell> snapshot = new HashMap<>();
        for (Map.Entry<Coordinate, Cell> entry : activeCells.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().clone());
        }
        versionHistory.put(currentVersion, snapshot);
        versionChangeCount.put(currentVersion,count);

    }

    public Map<Coordinate, Cell> getVersion(int version) {
        Map<Coordinate, Cell> versionSnapshot = versionHistory.get(version);

        if (versionSnapshot == null) {
            return null;
        }

        // Create a deep clone of the version snapshot to prevent modification of original data
        Map<Coordinate, Cell> clonedSnapshot = new HashMap<>();
        for (Map.Entry<Coordinate, Cell> entry : versionSnapshot.entrySet()) {
            clonedSnapshot.put(entry.getKey(), entry.getValue().clone());
        }

        return clonedSnapshot;
    }



    public boolean hasVersion(int version) {
        return versionHistory.containsKey(version);
    }

    public int getChangedCellsCount(int version) {
        return versionChangeCount.getOrDefault(version, 0);
    }

    public Set<Integer> getAllVersions() {
        return versionHistory.keySet();
    }
}

