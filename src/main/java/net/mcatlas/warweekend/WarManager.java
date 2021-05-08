package net.mcatlas.warweekend;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarManager {

    private Map<Player, WarPlayer> participants;
    private Map<Town, WarTown> warTowns;

    public WarManager() {
        participants = new ConcurrentHashMap<>();
        warTowns = new ConcurrentHashMap<>();
    }

    public Collection<WarPlayer> getParticipants() {
        return participants.values();
    }

    public int getCaptureBoost(WarTeam warTeam) {
        Map<WarTeam, Integer> memberCounts = new HashMap<>();

        for (WarPlayer warPlayer : participants.values()) {
            if (memberCounts.containsKey(warPlayer.getWarTeam())) {
                int count = memberCounts.get(warPlayer.getWarTeam()) + 1;
                memberCounts.put(warPlayer.getWarTeam(), count);
            } else {
                memberCounts.put(warPlayer.getWarTeam(), 1);
            }
        }

        int mostMemberCount = 0;

        for (WarTeam _warTeam : memberCounts.keySet()) {
            if (memberCounts.get(_warTeam) > mostMemberCount) {
                mostMemberCount = memberCounts.get(_warTeam);
            }
        }

        return mostMemberCount / memberCounts.get(warTeam);
    }

    public Set<WarPlayer> getTeamMembers(WarTeam warTeam) {
        Set<WarPlayer> warPlayers = new HashSet<>();

        for (WarPlayer warPlayer : participants.values()) {
            if (warPlayer.getWarTeam().equals(warTeam)) {
                warPlayers.add(warPlayer);
            }
        }

        return warPlayers;
    }

    public void joinTeam(Player player, WarTeam warTeam) {
        ItemStack helmet = player.getInventory().getHelmet();

        // put existing helmet in inventory (if it wasnt a team helmet)
        if (helmet != null && helmet.getType() != Material.AIR) {
            boolean teamHelm = false;

            for (WarTeam _warTeam : WarTeam.values()) {
                if (helmet.getType() == _warTeam.getHat()) {
                    teamHelm = true;
                }
            }

            if (!teamHelm) {
                helmet = helmet.clone();
                player.getInventory().setHelmet(null);
                player.getInventory().addItem(helmet);
                player.sendMessage(ChatColor.GOLD + "You've put on your team helmet. Your existing helmet was put in your inventory.");
            }
        }

        // put team helmet on
        ItemStack teamHelmet = new ItemStack(warTeam.getHat(), 1);
        player.getInventory().setHelmet(teamHelmet);

        WarPlayer warPlayer = new WarPlayer(player, warTeam);
        participants.put(player, warPlayer);

        player.sendMessage(ChatColor.YELLOW + "You've joined: " + warTeam.getColor() + warTeam.getStylizedName());
    }

    public void leaveTeam(Player player) {
        participants.remove(player);
    }

    public WarTeam getTeam(Player player) {
        if (participants.containsKey(player)) {
            return participants.get(player).getWarTeam();
        } else {
            return null;
        }
    }

    public void loadWarTownsFromStorage(WarWeekendPlugin warWeekendPlugin) {
        warWeekendPlugin.getLogger().info("Loading town captures from storage...");

        warWeekendPlugin.getMySQLWarStorage().queryCaptures().thenAccept(storedCaptures -> {
            for (MySQLWarStorage.StoredCapture storedCapture : storedCaptures) {
                Town town;
                try {
                    town = TownyAPI.getInstance().getDataSource().getTown(storedCapture.townName);
                } catch (NotRegisteredException e) {
                    return;
                }

                WarTeam warTeam;
                try {
                    warTeam = WarTeam.valueOf(storedCapture.teamName);
                } catch (Exception e) {
                    return;
                }

                WarTown warTown = new WarTown(town);
                warTown.setController(warTeam);

                warTowns.put(town, warTown);
            }

            warWeekendPlugin.getLogger().info("Loaded town captures! " + storedCaptures.size());
        });
    }

    public Collection<WarTown> getWarTowns() {
        return warTowns.values();
    }

    public WarTown getWarTown(Town town) {
        return warTowns.get(town);
    }

    public void addWarTown(WarTown warTown) {
        warTowns.put(warTown.getTown(), warTown);
    }

}
