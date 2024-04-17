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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;

public class LoadItemCommand implements CommandExecutor {
    private FirstItemV3 plugin;
    private String mcVersion;

    public LoadItemCommand(FirstItemV3 pluign) {
        this.plugin = pluign;
        String serverVersion = getServer().getVersion();
        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(serverVersion);
        mcVersion = "unknown";
        if (matcher.find()) {
            mcVersion = matcher.group(1);
        }
        mcVersion=mcVersion.replace(".", "-");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("firstitemv3.additems")) {
            Player player = (Player) sender;
            List<String> materialsInInventory = getMaterialsInInventory(player);
            saveMaterialsToConfig(materialsInInventory);
            return true;
        } else {
            Bukkit.getLogger().info("This command can only be run by a player.");
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