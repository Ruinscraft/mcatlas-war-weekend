package net.mcatlas.warweekend;

import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class ScoreKeeperTask implements Runnable, Listener, CommandExecutor {

    private WarWeekendPlugin warWeekendPlugin;
    private Map<WarTeam, Integer> scoreCache;
    private BossBar bossBar;

    public ScoreKeeperTask(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
        scoreCache = new HashMap<>();
        bossBar = warWeekendPlugin.getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        bossBar.addPlayer(event.getPlayer());
    }

    public void onQuit(PlayerQuitEvent event) {
        bossBar.removePlayer(event.getPlayer());
    }

    @Override
    public void run() {
        int totalCaptured = 0;
        WarTeam winningTeam = null;

        for (WarTeam warTeam : scoreCache.keySet()) {
            if (winningTeam == null) {
                winningTeam = warTeam;
            }

            int score = scoreCache.get(warTeam);

            totalCaptured += score;

            if (score > scoreCache.get(winningTeam)) {
                winningTeam = warTeam;
            }
        }

        if (winningTeam != null) {
            float pctWinning = scoreCache.get(winningTeam) / (float) totalCaptured;

            String bossTitle = winningTeam.getColor() + winningTeam.stylizedName + " are winning with " + (int) (pctWinning * 100) + "%";
            bossBar.setTitle(bossTitle);
            bossBar.setProgress(pctWinning);

            if (winningTeam == WarTeam.MELONS) {
                bossBar.setColor(BarColor.GREEN);
            } else if (winningTeam == WarTeam.PUMPKINS) {
                bossBar.setColor(BarColor.YELLOW);
            }
        }

        // update score cache async
        warWeekendPlugin.getMySQLWarStorage().queryCaptures().thenAccept(storedCaptures -> {
            Map<WarTeam, Integer> teamScores = new HashMap<>();

            for (MySQLWarStorage.StoredCapture storedCapture : storedCaptures) {
                WarTeam warTeam;
                try {
                    warTeam = WarTeam.valueOf(storedCapture.teamName);
                } catch (Exception e) {
                    continue;
                }

                if (teamScores.containsKey(warTeam)) {
                    int score = teamScores.get(warTeam) + 1;
                    teamScores.put(warTeam, score);
                } else {
                    teamScores.put(warTeam, 1);
                }
            }

            scoreCache = teamScores;
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "==== WAR STATS ====");
        for (WarTeam warTeam : scoreCache.keySet()) {
            sender.sendMessage(warTeam.getColor() + warTeam.getStylizedName() + ChatColor.YELLOW + " have " + scoreCache.get(warTeam) + " towns captured.");
        }
        sender.sendMessage(ChatColor.YELLOW + "There are a total of " + TownyAPI.getInstance().getDataSource().getTowns().size() + " towns on the server.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "==== WAR STATS ====");
        return true;
    }

}
