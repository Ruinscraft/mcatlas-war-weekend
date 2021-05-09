package net.mcatlas.warweekend;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private WarWeekendPlugin warWeekendPlugin;

    public JoinQuitListener(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        warWeekendPlugin.getServer().getScheduler().runTaskLater(warWeekendPlugin, () -> {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                player.sendTitle(ChatColor.GOLD + "WEEKEND WAR", ChatColor.YELLOW + "MELONS VS PUMPKINS");
                player.sendMessage(ChatColor.LIGHT_PURPLE + "============ EMERGENCY ============");
                player.sendMessage(ChatColor.YELLOW + "MELONS VS PUMPKINS WAR");
                player.sendMessage(ChatColor.YELLOW + "JOIN IMMEDIATELY WITH /joinwar");
                player.sendMessage(ChatColor.YELLOW + "PLEASE READ HERE: https://ruinscraft.com/threads/mcatlas-weekend-war.12590/");
                player.sendMessage(ChatColor.LIGHT_PURPLE + "============ EMERGENCY ============");
            }

            if (warWeekendPlugin.getWarManager().isCooldownJoinTeam(player)) {
                player.sendMessage(ChatColor.RED + "You're currently on cooldown for rejoining a different team.");
                player.sendMessage(ChatColor.RED + "You won't be able to join a team until the 10 minute cooldown is over.");
            }
        }, 100L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        warWeekendPlugin.getWarManager().leaveTeam(event.getPlayer());
    }

}
