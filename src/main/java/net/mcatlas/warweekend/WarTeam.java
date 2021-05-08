package net.mcatlas.warweekend;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum WarTeam {

    MELONS(Material.MELON, ChatColor.GREEN, "THE MELONS"),
    PUMPKINS(Material.PUMPKIN, ChatColor.GOLD, "THE PUMPKINS");

    Material hat;
    ChatColor color;
    String stylizedName;

    WarTeam(Material hat, ChatColor color, String stylizedName) {
        this.hat = hat;
        this.color = color;
        this.stylizedName = stylizedName;
    }

    public Material getHat() {
        return hat;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getStylizedName() {
        return stylizedName;
    }

}
