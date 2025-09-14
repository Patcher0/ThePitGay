package net.mizukilab.pit.enchantment.menu.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class EnchantmentDisplayButton extends Button {

    private final int enchantIndex;

    public EnchantmentDisplayButton(int enchantIndex) {
        this.enchantIndex = enchantIndex;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        String enchantingItemStr = profile.getEnchantingItem();

        if (enchantingItemStr == null) {
            return getEmptySlot("enchantingItemStr为null");
        }

        ItemStack enchantingItem = InventoryUtil.deserializeItemStack(enchantingItemStr);
        if (enchantingItem == null || enchantingItem.getType() == Material.AIR) {
            return getEmptySlot("物品为null或AIR");
        }

        IMythicItem mythicItem = Utils.getMythicItem0(enchantingItem);
        if (mythicItem == null) {
            return getEmptySlot("mythicItem为null");
        }

        ItemStack refreshedItem = mythicItem.toItemStack();
        mythicItem = Utils.getMythicItem0(refreshedItem);
        
        if (mythicItem == null) {
            return getEmptySlot("刷新后mythicItem为null");
        }

        if (!mythicItem.isEnchanted()) {
            return getEmptySlot("物品未附魔");
        }

        Map<AbstractEnchantment, Integer> enchantments = mythicItem.getEnchantments();

        if (enchantments.isEmpty()) {
            return getEmptySlot("附魔列表为空");
        }
        int size = enchantments.size();
        Iterator<Map.Entry<AbstractEnchantment, Integer>> iterator = enchantments.entrySet().iterator();
        Map.Entry<AbstractEnchantment, Integer> enchantEntry = null;
        if(enchantIndex < size) {
            for (int i = 0; i < enchantIndex; i++) {
                enchantEntry = iterator.next();
            }
        }
        if (enchantEntry == null) {
            return getEmptySlot("索引超出范围: " + enchantIndex + "/" + size);
        }

        AbstractEnchantment enchant = enchantEntry.getKey();
        Integer level = enchantEntry.getValue();
        List<String> lore = new LinkedList<>();

        lore.add("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("&f✦ &7等级: " + getLevelColor(level) + RomanUtil.convert(level));
        lore.add("");

        String usefulnessLore = enchant.getUsefulnessLore(level);
        if (usefulnessLore != null && !usefulnessLore.isEmpty()) {
            lore.add("&6&l▍ &6附魔效果:");

            int start = 0;
            int len = usefulnessLore.length();
            for (int i = 0; i <= len; i++) {
                if (i == len || (i + 1 < len && usefulnessLore.charAt(i) == '/' && usefulnessLore.charAt(i + 1) == 's')) {
                    String line = usefulnessLore.substring(start, i).trim();
                    if (!line.isEmpty()) {
                        if (line.charAt(0) != '&') {
                            line = "&7" + line;
                        }
                        lore.add("  &8▸ " + line);
                    }
                    start = i + 2;
                    i++;
                }
            }
        }


        lore.add("");
        lore.add("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("&8&o来自神话之井中的物品");
        lore.add("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(enchant.getRarity().getPrefix() + "&9" + enchant.getEnchantName() + " " + RomanUtil.convert(level))
                .lore(lore)
                .build();
    }

    private ItemStack getEmptySlot(String reason) {
        return new ItemBuilder(Material.STAINED_GLASS_PANE)
                .durability(7)
                .name("&7附魔槽位 " + (enchantIndex + 1))
                .lore(
                        "&7在左侧放入已附魔的神话物品",
                        "&7以查看其附魔信息"
                )
                .build();
    }

    public String getLevelColor(int level) {
        return switch (level) {
            case 1 -> "§a";
            case 2 -> "§b";
            case 3 -> "§c";
            default -> "null";
        };
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {

    }
} 