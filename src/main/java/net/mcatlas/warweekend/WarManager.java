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
import java.util.concurrent.TimeUnit;

public class WarManager {

    private Map<Player, WarPlayer> participants;
    private Map<Town, WarTown> warTowns;
    private Map<UUID, Long> recentlyJoinedTeam;

    public WarManager() {
        participants = new ConcurrentHashMap<>();
        warTowns = new ConcurrentHashMap<>();
        recentlyJoinedTeam = new HashMap<>();
    }

    public Collection<WarPlayer> getParticipants() {
        return participants.values();
    }

    public void putCooldownJoinTeam(Player player) {
        recentlyJoinedTeam.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isCooldownJoinTeam(Player player) {
        if (recentlyJoinedTeam.isEmpty() || !recentlyJoinedTeam.containsKey(player.getUniqueId())) return false;
        return recentlyJoinedTeam.get(player.getUniqueId()) + TimeUnit.MINUTES.toMillis(10) > System.currentTimeMillis();
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

    public boolean isInSameTown(Player player, Player player2) {
        TownBlock block1 = TownyAPI.getInstance().getTownBlock(player.getLocation());
        if (block1 == null) return false;
        TownBlock block2 = TownyAPI.getInstance().getTownBlock(player2.getLocation());
        if (block2 == null) return false;
        try {
            if (block1.getTown() == block2.getTown()) return true;
        } catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }

    public boolean onSameTeam(Player player, Player player2) {
        return getTeam(player) == getTeam(player2) && getTeam(player) != null && getTeam(player2) != null;
    }

    public void joinTeam(Player player, WarTeam warTeam) {
        replaceHelmet(player, warTeam);

        WarPlayer warPlayer = new WarPlayer(player, warTeam);
        participants.put(player, warPlayer);

        player.sendMessage(ChatColor.YELLOW + "You've joined: " + warTeam.getColor() + warTeam.getStylizedName());
    }

    public void replaceHelmet(Player player, WarTeam warTeam) {
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

    public WarTown getWarTown(String name) {
        for (WarTown warTown : warTowns.values()) {
            if (warTown.getTown().getName().equals(name)) {
                return warTown;
            }
        }
        return null;
    }

    public void addWarTown(WarTown warTown) {
        warTowns.put(warTown.getTown(), warTown);
    }

}
