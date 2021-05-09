package net.mcatlas.warweekend;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ChooseTeamGUI implements Listener {

    private WarWeekendPlugin warWeekendPlugin;
    private Inventory inventory;

    public ChooseTeamGUI(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
        inventory = warWeekendPlugin.getServer().createInventory(null, 9, "CHOOSE TEAM");
        inventory.setItem(0, createGuiItem(Material.MELON, "JOIN " + WarTeam.MELONS.getColor() + WarTeam.MELONS.getStylizedName()));
        inventory.setItem(1, createGuiItem(Material.PUMPKIN, "JOIN " + WarTeam.PUMPKINS.getColor() + WarTeam.PUMPKINS.getStylizedName()));
        inventory.setItem(2, createGuiItem(Material.STONE, "SPECTATE (play as normal)"));
    }

    private ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        event.setCancelled(true);

        switch (event.getRawSlot()) {
            case 0: // MELON
                warWeekendPlugin.getWarManager().joinTeam(player, WarTeam.MELONS);
                warWeekendPlugin.getWarManager().putCooldownJoinTeam(player);
                player.closeInventory();
                return;
            case 1: // PUMPKIN
                warWeekendPlugin.getWarManager().joinTeam(player, WarTeam.PUMPKINS);
                warWeekendPlugin.getWarManager().putCooldownJoinTeam(player);
                player.closeInventory();
                return;
            case 2: // SPECTATE
                player.closeInventory();
                warWeekendPlugin.getWarManager().leaveTeam(player);
                warWeekendPlugin.getWarManager().putCooldownJoinTeam(player);
            default:
                return;
        }
    }

}
