package com.norcode.bukkit.saddlebags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.persistence.PersistenceException;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Saddlebags extends JavaPlugin implements Listener {

    private HashMap<UUID, Saddlebag> entitySaddlebagMap = new HashMap<UUID, Saddlebag>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        checkDatabase();
        loadData();
    }

    private void checkDatabase() {
        try {
            List<EntityInventory> entityInventories = getDatabase().find(EntityInventory.class).findList();
        } catch (PersistenceException ex) {
            installDDL();
        }
    }

    public void loadData() {
        List<EntityInventory> entityInventories = getDatabase().find(EntityInventory.class).findList();
        getLogger().info("loaded " + entityInventories.size() + " records");
        entitySaddlebagMap.clear();
        for (EntityInventory inv: entityInventories) {
            try {
                entitySaddlebagMap.put(inv.getId(), new Saddlebag(this, inv));
            } catch (NullPointerException ex) {
               
            }
        }
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Pig) {
            Pig pig = (Pig) event.getRightClicked();
            Player player = event.getPlayer();
            Material inHand = player.getItemInHand() != null ? player.getItemInHand().getType() : null;
            Saddlebag bag = null;
            if (getKeyItem().equals(inHand)) {
                if (entitySaddlebagMap.containsKey(pig.getUniqueId())) {
                    bag = entitySaddlebagMap.get(pig.getUniqueId());
                    if (!getConfig().getBoolean("owner-only") || player.getName().equals(bag.getOwner())) {
                        if (!getConfig().getBoolean("riding-only") || player.getVehicle() != null && player.getVehicle().equals(pig)) {
                            bag.open(player);
                            event.setCancelled(true);
                        }
                    }
                } else if (pig.hasSaddle()) {
                    bag = new Saddlebag(this, (Entity) pig, player);
                    if (player.getVehicle().equals(pig) || !getConfig().getBoolean("riding-only")) {
                        entitySaddlebagMap.put(pig.getUniqueId(), bag);
                        bag.open(player);
                        event.setCancelled(true);
                    }
                }
                
            } else if (!pig.hasSaddle() && Material.SADDLE.equals(inHand)) {
                bag = new Saddlebag(this, (Entity) pig, player);
                entitySaddlebagMap.put(pig.getUniqueId(), bag);
            }
        }
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Saddlebag) {
            ((Saddlebag) event.getInventory().getHolder()).onClosed(event.getViewers());
        }
    }

    private Material getKeyItem() {
        return Material.valueOf(getConfig().getString("key-item", "RED_MUSHROOM"));
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(EntityInventory.class);
        return classes;
    }
}
