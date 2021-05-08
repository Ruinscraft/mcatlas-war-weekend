package net.mcatlas.warweekend;

import com.palmergames.bukkit.towny.object.Town;

import java.util.HashMap;
import java.util.Map;

public class WarTown {

    private Town town;
    private WarTeam controller;
    private transient Map<WarTeam, Integer> playerCounts;
    private transient double captureScore;

    public WarTown(Town town) {
        this.town = town;
        playerCounts = new HashMap<>();
        resetScore();
    }

    public Town getTown() {
        return town;
    }

    public boolean hasController() {
        return controller != null;
    }

    public WarTeam getController() {
        return controller;
    }

    public void setController(WarTeam controller) {
        this.controller = controller;
    }

    public Map<WarTeam, Integer> getPlayerCounts() {
        return playerCounts;
    }

    public int getPlayerCountForTeam(WarTeam warTeam) {
        return playerCounts.get(warTeam);
    }

    public WarTeam calculateCurrentController() {
        WarTeam controller = this.controller;

        int previousControllerCount = 0;
        if (playerCounts.get(this.controller) != null) {
            previousControllerCount = playerCounts.get(controller);
        }

        for (WarTeam warTeam : playerCounts.keySet()) {
            if (playerCounts.get(warTeam) > previousControllerCount) {
                controller = warTeam;
            }
        }

        return controller;
    }

    public void setPlayerCountForTeam(WarTeam warTeam, int playerCount) {
        playerCounts.put(warTeam, playerCount);
    }

    public double getCaptureScore() {
        return captureScore;
    }

    public void decrementScore(double multipler) {
        captureScore = captureScore - (1 * multipler);
    }

    public void resetScore() {
        captureScore = 10.0f;
    }

}
