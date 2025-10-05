package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.config.PitWorldConfig;
import net.mizukilab.pit.data.operator.PackedOperator;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.item.type.*;
import net.mizukilab.pit.item.type.mythic.MagicFishingRod;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.util.aabb.AABB;
import net.mizukilab.pit.util.arithmetic.IntegerUtils;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
@Skip
public class Utils {

    public static void sendRedstoneParticle(
            Player player,
            Location location,
            float r,
            float g,
            float b
    ) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        World world = craftPlayer.getHandle().world;
        if(world instanceof WorldServer e){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                    EnumParticle.REDSTONE,
                    true,
                    (float) location.getX(),
                    (float) location.getY(),
                    (float) location.getZ(),
                    r / 255,
                    g / 255,
                    b / 255,
                    1.0f,
                    0
            );
            EntityTrackerEntry entityTrackerEntry = e.tracker.trackedEntities.get(craftPlayer.getEntityId());
            entityTrackerEntry.broadcastIncludingSelf(packet);
        }
    }
    /**
     * 需要Paper支持。
     *
     * @return
     */
    public static net.minecraft.server.v1_8_R3.ItemStack toNMStackQuick(ItemStack item) {
        return PublicUtil.toNMStackQuick(item);
    }
    public static long toUnsignedInt(int data){
        return data & 0xFFFFFFFFL;
    }


    public static boolean shouldTick(long tick, int b) {
        return tick % Math.max(1, b) == 0;
    }
    /**
     * 随机color
     */
    private static final ChatColor[] CHAT_COLORS = ChatColor.values();
    public static ChatColor randomColor() {
        return CHAT_COLORS[ThreadLocalRandom.current().nextInt(Math.max(0, CHAT_COLORS.length - 1))];
    }

    /**
     * 标记GC and Exit
     *
     * @param projectile current Entity
     */
    public static void pointMetadataAndRemove(Entity projectile, int later, String... metadata) {
        ThePit instance = ThePit.getInstance();
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            for (String metadatum : metadata) {
                projectile.removeMetadata(metadatum, instance);
            }
        }, later);
    }

    /**
     * 超级快，nano respond o(":".length())
     * 0 - len
     * 原理: Enchantment NBT always prepend number at the last char
     */
    @SneakyThrows
    public static void readEnchantments(Object2IntMap<AbstractEnchantment> ment, NBTTagList nbtTagList) {
        int size = nbtTagList.size();
        for (int i = 0; i < size; i++) {
            String s = nbtTagList.getString(i);
            int length = s.length();
            int splitIndex = s.lastIndexOf(':',length - 2);

            ThePit instance = ThePit.getInstance();
            if (splitIndex != -1) {
                String enchantmentName = s.substring(0, splitIndex);
                try {
                    int level = IntegerUtils.fastParse0(s,splitIndex + 1,length);
                    AbstractEnchantment enchantment = instance
                            .getEnchantmentFactor()
                            .getEnchantmentMap()
                            .get(enchantmentName);

                    if (enchantment != null) {
                        ment.put(enchantment, level);
                    }
                } catch (NumberFormatException e) {
                    String levelString = s.substring(splitIndex + 1);
                    instance.getLogger().warning("Can't serialize level: " + levelString);
                }
            } else {
                instance.getLogger().warning("Can't serialize level: " + s);
            }
        }
    }


    /**
     * 超级高效的split方法。
     *
     * @param line string
     * @return a array of strings
     */
    public static String[] splitByCharAt(final String line, final char delimiter) {
        return PublicUtil.splitByCharAt(line, delimiter);
    }

    /**
     * 返回-1为没有
     *
     * @param item
     * @param enchantName
     * @return
     */
    public static int getEnchantLevel(ItemStack item, String enchantName) {
        final IMythicItem mythicItem = getMythicItem(item);
        if (mythicItem == null) {
            return -1;
        }

        return getEnchantLevel(mythicItem, enchantName);
    }

    public static int getEnchantLevel(ItemStack item, AbstractEnchantment enchObj) {
        final IMythicItem mythicItem = getMythicItem(item);
        if (mythicItem == null) {
            return -1;
        }
        return mythicItem.getEnchantments().getInt(enchObj);
    }

    public static int getEnchantLevel(AbstractPitItem item, AbstractEnchantment enchObj) {
        if (item == null) {
            return -1;
        }

        return item.getEnchantmentLevel(enchObj);
    }

    public static int getEnchantLevel(AbstractPitItem item, String enchantName) {
        if (item == null) {
            return -1;
        }
        return item.getEnchantmentLevel(enchantName);
    }

    public static String dumpNBTOnString(ItemStack stack) {
        NBTTagCompound tag = Utils.toNMStackQuick(stack).getTag();
        return tag.toString();
    }

    public static IMythicItem getMythicItem(ItemStack item) {
        ThePit instance = ThePit.getInstance();
        if (instance != null) {
            ItemFactory itemFactory = (ItemFactory) instance.getItemFactory();
            if (itemFactory != null) {
                return itemFactory.getIMythicItem(item);
            }
        }
        return getMythicItem0(item);
    }
    public static void playBlockBreak(Location location, Material material) {
        PacketPlayOutWorldEvent ppowe = new PacketPlayOutWorldEvent(2001, new BlockPosition(location.getX(), location.getY(), location.getZ()), material.getId(), false);
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(ppowe));
    }
    public static PackedOperator constructUnsafeOperator(String searchName) {
        PlayerProfile playerProfile = PlayerProfile.loadPlayerProfileByName(searchName);
        PackedOperator packedOperator = new PackedOperator(ThePit.getInstance());
        if (playerProfile == null) {
            return packedOperator;
        }
        packedOperator.loadAs(playerProfile);
        return packedOperator;
    }

    public static IMythicItem getMythicItem0(ItemStack item, String internalName) {
        IMythicItem mythicItem = null;
        if (internalName == null) { //提前skip, 不需要name。
            return null;
        }
        switch (internalName) {
            case "mythic_sword" -> mythicItem = new MythicSwordItem();
            case "mythic_bow" -> mythicItem = new MythicBowItem();
            case "mythic_leggings" -> mythicItem = new MythicLeggingsItem();
            case "angel_chestplate" -> mythicItem = new AngelChestplate();
            case "armageddon_boots" -> mythicItem = new ArmageddonBoots();
            case "kings_helmet" -> mythicItem = new GoldenHelmet();
            case "lucky_chestplate" -> mythicItem = new LuckyChestplate();
            case "jewel_sword" -> mythicItem = new JewelSword();
            case "magic_fishing_rod" -> mythicItem = new MagicFishingRod();
            default -> {
                return null;
            }
        }

        mythicItem.loadFromItemStack(item);

        return mythicItem;
    }

    public static IMythicItem getMythicItem0(ItemStack item) {
        final String internalName = ItemUtil.getInternalName(item);
        return getMythicItem0(item, internalName);
    }
    public static boolean canUseGem(@NotNull ItemStack item) {

        return canUseGem0(false,item);
    }
    public static boolean canUseGem0(boolean rare, ItemStack item){
        final IMythicItem mythicItem = Utils.getMythicItem(item);
        if (mythicItem == null || !mythicItem.isEnchanted() || mythicItem.isBoostedByGem() || mythicItem.isBoostedByGlobalGem()) {
            return false;
        }

        if (mythicItem.getColor() == MythicColor.DARK) {
            return false;
        }

        return canUseGemSeries(rare,mythicItem);
    }
    private static boolean canUseGemSeries(boolean rare, IMythicItem mythicItem) {
        Object2IntMap.FastEntrySet<AbstractEnchantment> entries = mythicItem.getEnchantments().object2IntEntrySet();
        for (Object2IntMap.Entry<AbstractEnchantment> entry : entries) {
            int level = entry.getIntValue();
            AbstractEnchantment key = entry.getKey();
            boolean rareResult = rare == (key.getRarity().getParentType() == EnchantmentRarity.RarityType.RARE);
            if (rareResult && key.getMaxEnchantLevel() > level) {
                return true;
            }
        }
        return false;
    }

    public static boolean canUseGlobalAttGem(ItemStack item) {
        if (item == null) {
            return false;
        }
        //?? same method?
        return canUseGem0(true,item);
    }

    public static boolean isNPC(org.bukkit.entity.Entity entity) {
        return PlayerUtil.isNPC(entity);
    }
    public static void serializePlayer(Player player){
        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(copy(inventory.getArmorContents()));
        inventory.setContents(copy(inventory.getContents()));
    }

    private static ItemStack[] copy(ItemStack[] armorContents) {
        ItemStack[] itemStacks = new ItemStack[armorContents.length];
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorContent = armorContents[i];
            if(armorContent != null){
                IMythicItem mythicItem = Utils.getMythicItem(armorContent);
                if(mythicItem != null){
                    itemStacks[i] = mythicItem.toItemStack();
                } else {
                    itemStacks[i] = armorContent;
                }
            }
        }
        return itemStacks;
    }

    public static ItemStack subtractLive(ItemStack item) {
        if (item == null) {
            return null;
        }
        return subtractLive(((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItemSync(item));
    }

    public static ItemStack subtractLive(IMythicItem item) {
        if (item == null) return null;
        if (item.isEnchanted()) {
            if (item.getLive() <= 1) {
                return new ItemStack(Material.AIR);
            } else {
                item.setLive(item.getLive() - 1);
                return item.toItemStack();
            }
        }
        return item.toItemStack();
    }

    public static boolean check(Material material) {
        return material == Material.HOPPER || material == Material.ENDER_CHEST;
    }

    public static boolean isInArena(Entity player) {
        PitWorldConfig config = ThePit.getInstance().getPitConfig();
        final AABB aabb = new AABB(config.getPitLocA().getX(), config.getPitLocA().getY(), config.getPitLocA().getZ(), config.getPitLocB().getX(), config.getPitLocB().getY(), config.getPitLocB().getZ());

        Location location = player.getLocation();
        final AABB playerAABB = new AABB(location.getX(), location.getY(), location.getZ(), location.getX() + 0.8, location.getY() + 2, location.getZ() + 0.8);
        final boolean inArena = !aabb.intersectsWith(playerAABB);
        return inArena;
    }

    //获取玩家是否可在该物品上使用神话之书 可则返回书物品 否则返回null
    public static boolean canUseMythicBook(Player player, ItemStack item) {
        //设置神话之书物品
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        IMythicItem mythicItem = getMythicItem(item);

        if (mythicItem.getColor() == MythicColor.RAGE) {
            return false;
        }

        if (mythicItem.boostedByBook) {
            return false;
        }

        if (profile.getEnchantingBook() != null) {
            return "mythic_reel".equals(ItemUtil.getInternalName(profile.getEnchantingBookItemStackFormed()));
        }
        return false;
    }
}