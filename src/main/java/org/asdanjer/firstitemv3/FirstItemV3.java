package org.asdanjer.firstitemv3;

import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FirstItemV3 extends JavaPlugin implements Listener {
    private List<String> itemsForCurrentVersion;
    String mcVersion;
    boolean entrymode;


    public static class FoundItem {
        private final OfflinePlayer player;
        private final LocalDateTime foundTime;
        private final Location location;

        public FoundItem(OfflinePlayer player, LocalDateTime foundTime, Location location) {
            this.player = player;
            this.foundTime = foundTime;
            this.location = location;
        }

        public OfflinePlayer getPlayer() {
            return player;
        }

        public LocalDateTime getFoundTime() {
            return foundTime;
        }

        public Location getLocation() {
            return location;
        }
    }

    private Map<Material, FoundItem> firstFoundItems;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        mcVersion = getConfig().getString("mcVersion");
        entrymode = getConfig().getBoolean("entrymode");
        try {
            getCommand("additems").setExecutor(new LoadItemCommand(this,mcVersion,entrymode));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!entrymode)
        {
        firstFoundItems = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        itemsForCurrentVersion = getConfig().getStringList(mcVersion);
        System.out.println(itemsForCurrentVersion);
        if(!itemsForCurrentVersion.isEmpty()){
            Bukkit.getLogger().info("FirstItemV3 has been enabled on " +mcVersion);
            getServer().getScheduler().runTaskTimer(this, this::savelist, 6000, 72000);
            String loadingstring = getConfig().getString("ItemFound " + mcVersion);
            if(loadingstring!=null&&!loadingstring.isEmpty()){
                firstFoundItems = loaditems(loadingstring);
            }

        }
        else {
            Bukkit.getLogger().info("FirstItemV3 could not find a list of items for the current version: " + mcVersion);
            Bukkit.getLogger().info("Disabling FirstItemV3...");
            getServer().getPluginManager().disablePlugin(this);
        }

    }}

    @Override
    public void onDisable() {
        savelist();
    }
    public void savelist(){
        getConfig().set("ItemFound "+ mcVersion, getfoundstring());
        saveConfig();
        backupConfigFile();
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        checkitem(event.getPlayer(), event.getItem().getItemStack().getType());
    }

    @EventHandler
    public void onPlayerCraftItem(CraftItemEvent event) {
        checkitem(Bukkit.getOfflinePlayer(event.getWhoClicked().getUniqueId()), event.getRecipe().getResult().getType());
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            Material currentMaterial = event.getCurrentItem().getType();
            if (event.getInventory().getType() != InventoryType.PLAYER && event.getAction() != InventoryAction.NOTHING && !currentMaterial.isAir()) {
                checkitem(Bukkit.getOfflinePlayer(event.getWhoClicked().getUniqueId()), currentMaterial);
            }
        }
    }

    public void checkitem(OfflinePlayer player, Material item){
        if (itemsForCurrentVersion.contains(item.name()) && !firstFoundItems.containsKey(item)){
            Location location = null;
            if (player.isOnline()) {
                location = player.getPlayer().getLocation();
            }
            firstFoundItems.put(item, new FoundItem(player, LocalDateTime.now(), location));
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE +player.getName() + ChatColor.RESET + " found the first " + ChatColor.BLUE + item.name().toLowerCase().replace("_", " ") + ChatColor.RESET + ". They better rename it to keep track of it :)");
            Bukkit.getLogger().info(player.getName() + " found " + item.name() + " at " + LocalDateTime.now() + " at location " + location);
        }
    }
    public String getfoundstring(){
        StringBuilder foundItemsString = new StringBuilder();
        if(firstFoundItems==null ||	firstFoundItems.isEmpty()) return "";
        for (Map.Entry<Material, FoundItem> entry : firstFoundItems.entrySet()) {
            String locationString = entry.getValue().getLocation().getWorld() + "," + (int)entry.getValue().getLocation().getX() + "," + (int)entry.getValue().getLocation().getY() + "," + (int)entry.getValue().getLocation().getZ();
            foundItemsString.append(entry.getKey().name()).append(" : ").append(entry.getValue().getPlayer().getName()).append(" : ").append(entry.getValue().getFoundTime()).append(" : ").append(locationString).append("\n");
        }
        return foundItemsString.toString();
    }
    public Map<Material, FoundItem> loaditems(String savedItemsString) {
        Map<Material, FoundItem> loadeditems = new HashMap<>();
        if (savedItemsString != null && !savedItemsString.isEmpty()) {
            String[] lines = savedItemsString.split("\n");
            for (String line : lines) {
                String[] parts = line.split(" : ");
                Material item = Material.getMaterial(parts[0]);
                OfflinePlayer player = Bukkit.getOfflinePlayer(parts[1]);
                LocalDateTime foundTime = LocalDateTime.parse(parts[2]);
                Location location = null;
                if (parts.length > 3) {
                    String[] locationParts = parts[3].split(",");
                    String worldName = locationParts[0].split("=")[1].replace("}", "");
                    World world = Bukkit.getWorld(worldName);
                    double x = Double.parseDouble(locationParts[1]);
                    double y = Double.parseDouble(locationParts[2]);
                    double z = Double.parseDouble(locationParts[3]);
                    float pitch = 0;
                    float yaw = 0;
                    location = new Location(world, x, y, z, yaw, pitch);
                }
                loadeditems.put(item, new FoundItem(player, foundTime, location));
            }
        }
        return loadeditems;
    }
    public void backupConfigFile() {
        Path source = Paths.get(getDataFolder().getAbsolutePath(), "config.yml");
        Path destination = Paths.get(getDataFolder().getAbsolutePath(), "config_backup.yml");

        try {
            if(Files.exists(destination)) {
                Files.delete(destination);
            }
            Files.copy(source, destination);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to create config file backup.");
            e.printStackTrace();
        }
    }
}