package net.mizukilab.pit.listener;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.mizukilab.pit.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
/*
 * author: APNF
 * date: 2025/9/12
 */

public class ExpireListener implements Listener {
    private static final String NBT_KEY = "expireTime";
    private static final String EXTRA_TAG = "extra";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        checkInventory(event.getWhoClicked().getInventory());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        checkInventory(event.getWhoClicked().getInventory());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkInventory(event.getPlayer().getInventory());
        checkInventory(event.getPlayer().getEnderChest());
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        checkInventory(event.getEntity().getInventory());
    }


    // 修复核心：从extra子标签中读取expireTime
    private long getExpireTime(ItemStack item) {
        if (item == null) {
            return 0;
        }

        // 转换为NMS物品
        // 草泥马的是不是脑残?
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = Utils.toNMStackQuick(item);
        if (nmsItem == null || !nmsItem.hasTag()) {
            return 0;
        }


        NBTTagCompound rootTag = nmsItem.getTag();
        NBTTagCompound extraTag = rootTag.getCompound(EXTRA_TAG);
        if (extraTag == null) {
            return 0;
        }

        return extraTag.hasKey(NBT_KEY) ? extraTag.getLong(NBT_KEY) : 0;
    }

    private void checkInventory(Inventory inv) {
        if (inv == null) return;

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null) {
                continue;
            }

            long expireTime = getExpireTime(item);
            if (expireTime > 0 && System.currentTimeMillis() > expireTime) {
                inv.setItem(slot, null); // 移除物品
                if (inv.getHolder() instanceof Player player) {
                    player.sendMessage(ChatColor.RED + "你的一件物品已过期并被移除!");
                }
            }
        }
    }
}
