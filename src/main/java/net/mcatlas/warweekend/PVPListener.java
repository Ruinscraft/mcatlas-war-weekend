package net.mcatlas.warweekend;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
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
        Player damager = null;

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            damager = (Player) arrow.getShooter();
        }

        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        }

        if (damager == null) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();

        boolean damagerInWar = warWeekendPlugin.getWarManager().getTeam(damager) != null;
        boolean damagedInWar = warWeekendPlugin.getWarManager().getTeam(damaged) != null;

        if (damagedInWar && damagerInWar) {
            // check if same team
            if (warWeekendPlugin.getWarManager().getTeam(damager).equals(warWeekendPlugin.getWarManager().getTeam(damaged))) {
                event.setCancelled(true);
                return;
            }

            double boost = warWeekendPlugin.getWarManager().getCaptureBoost(warWeekendPlugin.getWarManager().getTeam(damager), warWeekendPlugin);

            if (boost > 1.0) {
                boost = boost / 2;
            }

            double damage = boost * event.getDamage();
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
