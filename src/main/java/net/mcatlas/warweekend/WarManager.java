package net.mcatlas.warweekend;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    public double getCaptureBoost(WarTeam warTeam, WarWeekendPlugin warWeekendPlugin) {
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

        int teamScore = warWeekendPlugin.getScoreKeeperTask().getScoreCache().get(warTeam);
        int highestScore = 0;

        for (WarTeam _warTeam : warWeekendPlugin.getScoreKeeperTask().getScoreCache().keySet()) {
            if (warWeekendPlugin.getScoreKeeperTask().getScoreCache().get(_warTeam) > highestScore) {
                highestScore = warWeekendPlugin.getScoreKeeperTask().getScoreCache().get(_warTeam);
            }
        }

        double scoreBoost = highestScore / (double) teamScore;
        return (mostMemberCount / (double) memberCounts.get(warTeam)) * scoreBoost;
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

    public Set<WarPlayer> getWarPlayersOpposingPlayer(Player player) {
        Set<WarPlayer> warPlayers = new HashSet<>();

        WarTeam team = getTeam(player);
        if (team == null) return warPlayers;
        for (WarTeam otherTeam : WarTeam.values()) {
            if (otherTeam == team) continue;
            warPlayers.addAll(getTeamMembers(otherTeam));
        }

        return warPlayers;
    }

    public Set<WarPlayer> getOpposingPlayersInTown(Player player) {
        Set<WarPlayer> warPlayers = new HashSet<>();

        TownBlock playerBlock = TownyAPI.getInstance().getTownBlock(player.getLocation());
        if (playerBlock == null) return warPlayers;

        for (WarPlayer warPlayer : getWarPlayersOpposingPlayer(player)) {
            TownBlock block = TownyAPI.getInstance().getTownBlock(warPlayer.getPlayer().getLocation());
            if (block == null) continue;
            try {
                Town town = block.getTown();
                Town town2 = playerBlock.getTown();
                if (town.equals(town2)) {
                    warPlayers.add(warPlayer);
                    continue;
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return warPlayers;
    }

    // get closest member of the opposing team to the player who is within the town
    public Player getClosestOpposerInTown(Player player) {
        Player farthest = null;

        for (WarPlayer warPlayer : getOpposingPlayersInTown(player)) {
            Player opposer = warPlayer.getPlayer();
            if (farthest == null || farthest.getLocation().distance(player.getLocation()) > opposer.getLocation().distance(player.getLocation())) {
                farthest = opposer;
            }
        }

        return farthest;
    }

    // get farthest member of the opposing team to the player who is within the town
    public Player getFarthestOpposerInTown(Player player) {
        Player farthest = null;

        for (WarPlayer warPlayer : getOpposingPlayersInTown(player)) {
            Player opposer = warPlayer.getPlayer();
            if (farthest == null || farthest.getLocation().distance(player.getLocation()) < opposer.getLocation().distance(player.getLocation())) {
                farthest = opposer;
            }
        }

        return farthest;
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

    public CompletableFuture<Void> loadWarTownsFromStorage(WarWeekendPlugin warWeekendPlugin) {
        warWeekendPlugin.getLogger().info("Loading town captures from storage...");

        return warWeekendPlugin.getMySQLWarStorage().queryCaptures().thenAccept(storedCaptures -> {
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
