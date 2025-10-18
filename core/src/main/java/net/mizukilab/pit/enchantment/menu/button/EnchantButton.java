package net.mizukilab.pit.enchantment.menu.button;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.StartEnchantLogicEvent;
import cn.charlotte.pit.events.genesis.GenesisTeam;
import cn.hutool.core.lang.func.Consumer3;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.mizukilab.pit.data.operator.SuPromise;
import net.mizukilab.pit.enchantment.info.EnchantRequest;
import net.mizukilab.pit.enchantment.menu.MythicWellMenu;
import net.mizukilab.pit.handlers.AnimationHandler;
import net.mizukilab.pit.event.PitPlayerEnchantEvent;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.item.type.MythicEnchantingTable;
import net.mizukilab.pit.menu.shop.button.AbstractShopButton;
import net.mizukilab.pit.util.FuncsKt;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.PlusPlayer;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.chat.ChatComponentBuilder;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/11 16:51
 */
@Skip
public class EnchantButton extends Button {

    private final ItemStack item;
    private final MythicWellMenu menu;
    public EnchantButton(ItemStack item, MythicWellMenu menu) {
        this.item = item;
        this.menu = menu;
    }

    private int getPrice(Player player, int level, MythicColor color) {
        int price;
        if (color == MythicColor.DARK) {
            price = switch (level) {
                case 1 -> 10000;
                case 2 -> 60000;
                default -> 99999;
            };
        } else {
            price = switch (level) {
                case 1 -> 1000;
                case 2 -> 4000;
                case 3 -> 8000;
                default -> 9999;
            };
        }
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (ThePit.getInstance().getPitConfig().isGenesisEnable() && profile.getGenesisData().getTeam() == GenesisTeam.DEMON && profile.getGenesisData().getTier() >= 3) {
            return (int) (0.35 * AbstractShopButton.getDiscountPrice(player, price));
        }
        return AbstractShopButton.getDiscountPrice(player, price);
    }

    public IMythicItem getMythicItem(ItemStack item) {
        return Utils.getMythicItem(item);
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        try {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            String enchantingItemStr = profile.getEnchantingItem();
            
            ItemStack currentItem = null;
            if (enchantingItemStr != null) {
                currentItem = InventoryUtil.deserializeItemStack(enchantingItemStr);
            }
            
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                currentItem = this.item;
            }
            
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                return getDefaultDisplayItem();
            }
            
            IMythicItem mythicItem = Utils.getMythicItem0(currentItem);
            MythicColor color = MythicColor.valueOf(ItemUtil.getItemStringData(mythicItem.toItemStack(), "mythic_color").toUpperCase());
            int level = mythicItem.getTier();

            List<String> lines = new LinkedList<>();
            if (level < (color == MythicColor.DARK ? 2 : 3)) {
                lines.add("&7升级至: &a" + RomanUtil.convert(level + 1) + " 阶");
                lines.add("&7价格: &6" + getPrice(player, level + 1, color) + " 硬币" + (level == (color == MythicColor.DARK ? 1 : 2) ? " &7+ " + color.getChatColor() + color.getDisplayName() + "色神话之甲" : ""));
                lines.add(" ");
                if (profile.getCoins() >= getPrice(player, level + 1, color)) {
                    if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "Mythicism") < 4 && level == 2) {
                        lines.add("&c天赋 &6神话附魔师 &c等级 &eIV &c后解锁此功能!");
                    } else {
                        String sinceItem = profile.getEnchantingScience();
                        ItemStack item = InventoryUtil.deserializeItemStack(sinceItem);
                        if ((item == null || item.getType() == Material.AIR) && level == 2) {
                            lines.add("&e选择背包内的神话之甲作为材料以继续...");
                        } else {
                            lines.add("&e点击进行附魔!");
                        }
                    }
                } else {
                    lines.add("&c你的硬币不足!");
                }
            } else {
                lines.add("&a此附魔物品已被提升至最大等级!");
            }
            if ((color == MythicColor.DARK || color == MythicColor.RAGE) && !PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
                if (!PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
                    lines.clear();
                    lines.add("&c请先解锁精通天赋 &6邪术 &c后重试!");
                }
                if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "heresy_perk") < 3 && level == 1) {
                    lines.clear();
                    lines.add("&c天赋 &6邪术 &c等级 &eIII &c后解锁此功能!");
                }
            }
            return new ItemBuilder(Material.ENCHANTMENT_TABLE)
                    .name("&d神话之井")
                    .lore(lines)
                    .build();
        } catch (Exception e) {
            CC.printError(player, e);
        }
        return getDefaultDisplayItem();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        // 从profile中获取最新的物品数据，而不是使用构造函数传入的旧数据
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        String enchantingItemStr = profile.getEnchantingItem();
        
        ItemStack actualItem = null;
        if (enchantingItemStr != null) {
            actualItem = InventoryUtil.deserializeItemStack(enchantingItemStr);
        }
        
        // 如果profile中没有物品或物品为空，则使用传入的item作为备用
        if (actualItem == null || actualItem.getType() == Material.AIR) {
            actualItem = this.item;
        }
        
        if (actualItem == null || actualItem.getType() == Material.AIR) {
            return;
        }

        IMythicItem mythicItem = Utils.getMythicItem(actualItem);

        if (mythicItem == null) return;

        final String mythicColor = ItemUtil.getItemStringData(actualItem, "mythic_color");
        if (mythicColor == null) {
            return;
        }

        MythicColor color = MythicColor.valueOf(mythicColor.toUpperCase());
        int level = mythicItem.getTier();

        if ((color == MythicColor.DARK || color == MythicColor.RAGE) && !PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
            if (!PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
                return;
            }
            if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "heresy_perk") < 3 && level == 1) {
                return;
            }
        }
        if (level >= (color == MythicColor.DARK ? 2 : 3)) {
            return;
        }
        if (profile.getCoins() < getPrice(player, level + 1, color)) {
            return;
        }
        if (level == (color == MythicColor.DARK ? 1 : 2)) {
            if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "Mythicism") < 4) {
                return;
            }
            if (!removeMythicLegWithColor(player, color)) {
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(CC.translate("&c请放入一条额外的 " + color.getChatColor() + color.getDisplayName() + "色神话之甲 &c才能附魔!"));
                return;
            }
        }
        profile.setCoins(profile.getCoins() - getPrice(player, level + 1, color));

        menu.getAnimationData().setFinished(false);
        menu.getAnimationData().setStartEnchanting(true);
        doEnchant(actualItem, player,profile, mythicItem,menu.getAnimationData());

        menu.getAnimationData().setAnimationTick(0);

        menu.setClosedByMenu(true);
        menu.openMenu(player);
    }

    /**
     * 面对过程式 附魔, 害得我键盘也给附魔了
     *
     * @param item
     * @param player
     * @param mythicItem
     */
    private void doEnchant(ItemStack item, Player player, PlayerProfile profile, IMythicItem mythicItem, AnimationHandler.AnimationData data) {

        StartEnchantLogicEvent startEnchantLogicEvent = new StartEnchantLogicEvent(player);
        startEnchantLogicEvent.callEvent();
        Consumer3<ItemStack, AbstractPitItem, Player> consumer = startEnchantLogicEvent.getConsumer();
        if(consumer != null){
            consumer.accept(item,mythicItem,player);
            if(!startEnchantLogicEvent.isCancelled()) {
                new PitPlayerEnchantEvent(player, mythicItem, mythicItem).callEvent();
                end(player, mythicItem);
            }
            return;
        }
        if(!startEnchantLogicEvent.isAllowEnchant()){
            return;
        }

        if(startEnchantLogicEvent.isCancelled()){
            return;
        }

        boolean useBook = Utils.canUseMythicBook(player, item);
        menu.getAnimationData().setEnd(false);
        SuPromise<EnchantRequest> consumers = ThePit.getInstance().getEnchTable().enchantItem(player, profile, mythicItem, useBook);
        consumers.promise(i -> {
            if(i.isFail()){
                player.sendMessage(CC.translate("&cUnable to enchant this item, it seems impossible"));
            }
            boolean announcement = i.isAnnouncement();
            new PitPlayerEnchantEvent(player, mythicItem, mythicItem).callEvent();
            if (mythicItem.getPrefix() != null) {
                announcement = true;
            }
            if (announcement) {
                beginAnnounceAsync(player, mythicItem, profile);
            }
            end(player, mythicItem);
            menu.getAnimationData().setEnd(true);
        });
    }
    private static BaseComponent[] toEmptyHover(IMythicItem mythicItem) {
        net.minecraft.server.v1_8_R3.ItemStack nms = Utils.toNMStackQuick(mythicItem.toItemStack());
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nms.save(nbtTagCompound);
        return new BaseComponent[]{
                new TextComponent(nbtTagCompound.toString())
        };
    }

    private void beginAnnounceAsync(Player player, IMythicItem mythicItem, PlayerProfile profile) {
        new BukkitRunnable() {
            final Cooldown cooldown = new Cooldown(10, TimeUnit.SECONDS);

            public void run() {
                if (menu.getAnimationData().isFinished()) {
                    this.cancel();
                    BaseComponent[] hoverEventComponents = toEmptyHover(mythicItem);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!FuncsKt.isSpecial(p)) {
                            if (!PlusPlayer.getPlusPlayer().contains(player.getName())) {
                                p.spigot().sendMessage(new ChatComponentBuilder(CC.translate("&d&l稀有附魔! &7" + profile.getFormattedNameWithRoman() + " &7在神话之井中获得了稀有物品: " + mythicItem.toItemStack().getItemMeta().getDisplayName() + " &e[查看]"))
                                        .setCurrentHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents)).create());
                                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                            }
                        }

                    }
                } else if (cooldown.hasExpired()) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(), 50, 5);
    }

    private static void end(Player player, IMythicItem mythicItem) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        profile.setEnchantingItem(InventoryUtil.serializeItemStack(mythicItem.toItemStack()));
    }


    public int max(Integer num1,int num2){
        if(num1 != null){
            return Math.max(num1,num2);
        }
        return num2;
    }

    @Override
    public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
        return true;
    }

    public ItemStack getDefaultDisplayItem() {
        return ((MythicEnchantingTable) FuncsKt.getInstance(ThePit.getInstance().getItemFactor().getItemMap().get("enchant_table_mobile"))).toItemStack();
    }

    /**
     * 移除玩家指定颜色的裤子
     *
     * @param player 玩家
     * @param color  需要移除的颜色
     * @return 是否移除，返回false为移除失败
     */
    private boolean removeMythicLegWithColor(Player player, MythicColor color) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getEnchantingScience() == null) return false;
        ItemStack itemStack = InventoryUtil.deserializeItemStack(profile.getEnchantingScience());
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        String mythic_color = ItemUtil.getItemStringData(itemStack, "mythic_color");
        for (MythicColor mythicColor : MythicColor.values()) {
            if (mythicColor.getInternalName().equals(mythic_color)) {
                profile.setEnchantingScience(InventoryUtil.serializeItemStack(new ItemStack(Material.AIR)));
                return true;
            }
        }

        return false;
    }
}
