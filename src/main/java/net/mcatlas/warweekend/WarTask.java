package net.mcatlas.warweekend;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class WarTask implements Runnable {

    private WarWeekendPlugin warWeekendPlugin;

    public WarTask(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
    }

    public void run() {
        for (WarTown warTown : warWeekendPlugin.getWarManager().getWarTowns()) {
            warTown.getPlayerCounts().clear();
        }

        // for each team
        for (WarTeam warTeam : WarTeam.values()) {
            Map<Town, Integer> playerCounts = new HashMap<>();

            // for each member of the team
            for (WarPlayer warPlayer : warWeekendPlugin.getWarManager().getTeamMembers(warTeam)) {
                // lets get the town they are in
                Location location = warPlayer.getPlayer().getLocation();
                TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);
                if (townBlock == null || !townBlock.hasTown()) {
                    continue;
                }

                Town town;
                try {
                    town = townBlock.getTown();
                } catch (NotRegisteredException e) {
                    continue;
                }

                // lets add them to the sum of players in that town
                if (playerCounts.containsKey(town)) {
                    int count = playerCounts.get(town) + 1;
                    playerCounts.put(town, count);
                } else {
                    playerCounts.put(town, 1);
                }
            }

            for (Town town : playerCounts.keySet()) {
                if (warWeekendPlugin.getWarManager().getWarTown(town) == null) {
                    warWeekendPlugin.getWarManager().addWarTown(new WarTown(town));
                }

                WarTown warTown = warWeekendPlugin.getWarManager().getWarTown(town);
                warTown.setPlayerCountForTeam(warTeam, playerCounts.get(town));
            }
        }

        for (WarTown warTown : warWeekendPlugin.getWarManager().getWarTowns()) {
            WarTeam previousController = warTown.getController();
            WarTeam currentController = warTown.calculateCurrentController();

            if (previousController != currentController) {
                if (warTown.getCaptureScore() <= 1) {
                    warTown.resetScore();
                    warTown.setController(currentController);
                    Bukkit.broadcastMessage(currentController.getColor() + currentController.getStylizedName() + ChatColor.YELLOW + " have CAPTURED " + warTown.getTown().getName() + "!");
                    warWeekendPlugin.getMySQLWarStorage().saveCapture(warTown.getTown().getName(), currentController.name());
                } else {
                    warTown.decrementScore(warWeekendPlugin.getWarManager().getCaptureBoost(currentController));
                    Bukkit.broadcastMessage(currentController.getColor() + currentController.getStylizedName() + ChatColor.YELLOW + " are capturing " + warTown.getTown().getName() + " (" + warTown.getCaptureScore() + ").");
                }
            } else {
                warTown.resetScore();
            }
        }
    }

}
