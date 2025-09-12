package net.mizukilab.pit.item;

import net.mizukilab.pit.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TestItem extends AbstractPitItem{
    @Override
    public String getInternalName() {
        return "TestItem";
    }

    @Override
    public String getItemDisplayName() {
        return "&c测试物品";
    }

    @Override
    public Material getItemDisplayMaterial() {
        return Material.BARRIER;
    }

    @Override
    public ItemStack toItemStack() {
        long expireTime = System.currentTimeMillis() + 10000;

        return
                new ItemBuilder(Material.BARRIER).name("&c测试").lore(ItemBuilder.formatExactTime(expireTime)).expireTime(expireTime).canTrade(true).canSaveToEnderChest(true).build();
    }

    @Override
    public void loadFromItemStack(ItemStack item) {

    }
}
