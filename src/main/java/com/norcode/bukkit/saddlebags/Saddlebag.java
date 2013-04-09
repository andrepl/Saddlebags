package com.norcode.bukkit.saddlebags;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class Saddlebag implements InventoryHolder {
    private Inventory inventory;
    private EntityInventory data;
    private Saddlebags plugin;
    public Saddlebag(Saddlebags plugin, EntityInventory data) {
        this.data = data;
        this.plugin = plugin;
    }

    public Saddlebag(Saddlebags plugin, Entity pig, Player player) {
        this.plugin = plugin;
        this.data = new EntityInventory();
        this.data.setId(pig.getUniqueId());
        this.data.setLastUpdated(new Date());
        this.data.setOwner(player.getName());
        this.data.setCapacity(getPlayerCapacity(player));
        plugin.getDatabase().save(this.data);
    }

    public int getPlayerCapacity(Player player) {
        if (player.hasPermission("saddlebags.capacity.81")) {
            return 81;
        } else if (player.hasPermission("saddlebags.capacity.72")) {
            return 72;
        } else if (player.hasPermission("saddlebags.capacity.63")) {
            return 63;
        } else if (player.hasPermission("saddlebags.capacity.54")) {
            return 54;
        } else if (player.hasPermission("saddlebags.capacity.45")) {
            return 45;
        } else if (player.hasPermission("saddlebags.capacity.36")) {
            return 36;
        } else if (player.hasPermission("saddlebags.capacity.27")) {
            return 27;
        } else if (player.hasPermission("saddlebags.capacity.18")) {
            return 18;
        } else if (player.hasPermission("saddlebags.capacity.9")) {
            return 9;
        }
        return plugin.getConfig().getInt("default-capacity", 27);
    }

    public String getOwner() {
        return data.getOwner();
    }

    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = plugin.getServer().createInventory(this, this.data.getCapacity(), "Saddlebags");
            this.loadInventory();
        }
        return this.inventory;
    }

    private void loadInventory() {
        if (this.data.getInventory() != null) { 
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> maplist = deserialize(this.data.getInventory());
            ItemStack[] items = SerializationUtil.deserializeItemList(maplist).toArray(new ItemStack[0]);
            this.inventory.setContents(items);
        }
    }

    private List<Map<String, Object>> deserialize(String i) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64Coder.decode(i));
            ObjectInputStream decoder = new ObjectInputStream(bais);
            List<Map<String, Object>> data = (List<Map<String, Object>>) decoder.readObject();
            return data;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private String serialize(List<Map<String, Object>> data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.close();
            String encoded = new String(Base64Coder.encode(baos.toByteArray()));
            return encoded;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void saveInventory() {
        ConfigurationSerializable[] items = (ConfigurationSerializable[]) getInventory().getContents();
        List<Map<String, Object>> maplist = SerializationUtil.serializeItemList(Arrays.asList(items));
        this.data.setInventory(serialize(maplist));
        this.data.setLastUpdated(new Date());
        this.plugin.getDatabase().update(this.data);
    }

    public void open(Player player) {
        Inventory i = getInventory();
        int capacity = getPlayerCapacity(player);
        if (this.data.getCapacity() != capacity) {
            resizeInventory(capacity);
        }
        player.openInventory(getInventory());
    }

    private void resizeInventory(int capacity) {
        this.data.setCapacity(capacity);
        this.data.setLastUpdated(new Date());
        this.plugin.getDatabase().update(this.data);
        this.inventory = null;
    }

    public void onClosed(List<HumanEntity> viewers) {
        this.saveInventory();
    }
}