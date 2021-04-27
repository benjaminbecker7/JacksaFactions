package com.bmbecker.plugin.guis;

import com.bmbecker.plugin.commands.FactionCommands;
import com.bmbecker.plugin.utilities.Cache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

// Based on implementation in spigot forums (https://www.spigotmc.org/wiki/creating-a-gui-inventory/)
public class InviteGui implements Listener {
    private final JavaPlugin plugin;
    private final Inventory inv;
    private final Player player;

    public InviteGui(JavaPlugin plugin, Player p) {
        this.plugin = plugin;
        player = p;

        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        this.inv = Bukkit.createInventory(null, 27, "Nearby Players");

        // Put the items into the inventory
        initializeItems();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // You can call this whenever you want to put the items in
    public void initializeItems() {


        for (Player online : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(online.getLocation()) <= 10) {
                String faction = Cache.getFaction(online.getUniqueId());
                if (faction == null) {
                    addGuiItem(online, "Click to invite " + online.getName(), "to your faction!");
                }
            }
        }

    }

    // Nice little method to create a gui item with a custom name, and description
    protected void addGuiItem(final Player player, final String... lore) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(player);
        meta.setDisplayName(player.getName());
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        inv.addItem(item);
    }

    // You can open the inventory with this
    public void openInventory() {
        player.openInventory(inv);
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != inv) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        FactionCommands.invite(p, clickedItem.getItemMeta().getDisplayName());
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory() == inv) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        HandlerList.unregisterAll(this);
    }
}