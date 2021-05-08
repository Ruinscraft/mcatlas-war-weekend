package net.mcatlas.warweekend;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PVPListener implements Listener {

    private WarWeekendPlugin warWeekendPlugin;

    public PVPListener(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPVP(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();

        boolean damagerInWar = warWeekendPlugin.getWarManager().getTeam(damager) != null;
        boolean damagedInWar = warWeekendPlugin.getWarManager().getTeam(damaged) != null;

        if (damagedInWar && damagerInWar) {
            // check if same team
            if (warWeekendPlugin.getWarManager().getTeam(damager).equals(warWeekendPlugin.getWarManager().getTeam(damaged))) {
                event.setCancelled(true);
                return;
            }

            double damage = warWeekendPlugin.getWarManager().getCaptureBoost(warWeekendPlugin.getWarManager().getTeam(damager))
                    * event.getDamage();
            event.setDamage(damage);
            event.setCancelled(false);
            return;
        } else if (!damagedInWar && !damagerInWar) {
            // if both NOT in war, allow pvp as normal
            return;
        }

        if (damagerInWar) {
            damager.sendMessage(ChatColor.RED + damaged.getName() + " is not in war. You cannot harm them");
        } else if (damagedInWar) {
            damager.sendMessage(ChatColor.RED + " you must be at war to harm " + damaged.getName() + ". Use /joinwar");
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (warWeekendPlugin.getWarManager().getTeam(killer) != null) {
            warWeekendPlugin.getMySQLWarStorage().incrementKillCount(warWeekendPlugin.getWarManager().getTeam(killer).name());
        }
    }

}