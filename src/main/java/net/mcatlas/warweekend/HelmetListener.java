package net.mcatlas.warweekend;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HelmetListener implements Listener {

    private WarWeekendPlugin warWeekendPlugin;

    public HelmetListener(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (warWeekendPlugin.getWarManager().getTeam(player) == null) {
            return;
        }

        if (event.getRawSlot() == 5) {
            event.setCancelled(true);
        }
    }

}
