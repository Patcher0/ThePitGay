package net.mizukilab.pit.listener;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

    public void startExpireCheckTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("ThePitUltimate"),
                this::checkAllPlayers,
                0,
                600
        );
    }

    private void checkAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkInventory(player.getInventory());
            checkInventory(player.getEnderChest()); // 检查末影箱
        }
    }

    // 修复核心：从extra子标签中读取expireTime
    private long getExpireTime(ItemStack item) {
        if (item == null) {
            return 0; // 空物品直接返回0
        }

        // 转换为NMS物品
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null || !nmsItem.hasTag()) {
            return 0; // 没有NBT标签，返回0
        }

        // 1. 获取根标签
        NBTTagCompound rootTag = nmsItem.getTag();
        // 2. 获取extra子标签（ItemBuilder中存储数据的地方）
        NBTTagCompound extraTag = rootTag.getCompound(EXTRA_TAG);
        if (extraTag == null) {
            return 0; // 没有extra子标签，返回0
        }

        // 3. 从extra子标签中读取expireTime
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
            // 过期时间>0 且 当前时间已超过过期时间 → 移除物品
            if (expireTime > 0 && System.currentTimeMillis() > expireTime) {
                inv.setItem(slot, null); // 移除物品
                // 通知玩家（如果是玩家的 inventory）
                if (inv.getHolder() instanceof Player) {
                    Player player = (Player) inv.getHolder();
                    player.sendMessage(ChatColor.RED + "你的一件物品已过期并被移除!");
                }
            }
        }
    }
}
