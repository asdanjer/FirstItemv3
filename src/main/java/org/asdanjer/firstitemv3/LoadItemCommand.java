package org.asdanjer.firstitemv3;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class LoadItemCommand implements CommandExecutor {
    private FirstItemV3 plugin;
    private String mcVersion;
    private boolean entrymode;

    public LoadItemCommand(FirstItemV3 pluign, String mcVersion, boolean entrymode) {
        this.plugin = pluign;
        this.mcVersion=mcVersion;
        this.entrymode=entrymode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!entrymode) {
            sender.sendMessage("Entrymode is disabled. Please enable it in the config.");
            return false;
        }
        if (sender instanceof Player && sender.hasPermission("firstitemv3.additems")) {
            Player player = (Player) sender;
            List<String> materialsInInventory = getMaterialsInInventory(player);
            saveMaterialsToConfig(materialsInInventory);
            player.sendMessage("Items in your inventory have been saved to the config.");
            return true;
        } else {
            sender.sendMessage("You are not a Player or do not have the permission to use this command.");
            return false;
        }
    }

    private List<String> getMaterialsInInventory(Player player) {
        List<String> materials = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if(!materials.contains(item.getType().toString())) materials.add(item.getType().toString());
            }
        }
        return materials;
    }

    private void saveMaterialsToConfig(List<String> newMaterials) {
        List<String> existingMaterials = plugin.getConfig().getStringList(mcVersion);
        existingMaterials.addAll(newMaterials);
        plugin.getConfig().set(mcVersion, existingMaterials);
        plugin.saveConfig();
    }
}