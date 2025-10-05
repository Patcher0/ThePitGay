package net.mizukilab.pit.enchantment;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.data.operator.SuPromise;
import net.mizukilab.pit.enchantment.info.EnchantRequest;
import net.mizukilab.pit.enchantment.param.item.*;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.enchantment.type.dark_normal.SomberEnchant;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.type.mythic.MagicFishingRod;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.util.Einstein;
import net.mizukilab.pit.util.exception.IllegalEnchantInputException;
import net.mizukilab.pit.util.functions.Func3;
import net.mizukilab.pit.util.random.RandomUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EnchantmentTable {
    EnchantmentFactor factor;
    ConcurrentLinkedQueue<EnchantRequest> enchantQueues = new ConcurrentLinkedQueue<EnchantRequest>();
    Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> rodEnchant = new Object2ObjectOpenHashMap<>();
    Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> itemEnchant = new Object2ObjectOpenHashMap<>();
    Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> swordEnchant = new Object2ObjectOpenHashMap<>();
    Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> bowEnchant = new Object2ObjectOpenHashMap<>();
    Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> armorEnchant = new Object2ObjectOpenHashMap<>();
    Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> enchantments = new Object2ObjectOpenHashMap<>();

    public EnchantmentTable(EnchantmentFactor factor) {
        this.factor = factor;
        reload();
    }

    public void reload() {
        rodEnchant.clear();
        itemEnchant.clear();
        swordEnchant.clear();
        bowEnchant.clear();
        armorEnchant.clear();
        enchantments.clear();
        factor.getEnchantments().forEach(i -> {
            enchantments.computeIfAbsent(i.getRarity(), b -> new ObjectArrayList<>()).add(i);
            if (i.getClass().isAnnotationPresent(ArmorOnly.class)) {
                armorEnchant.computeIfAbsent(i.getRarity(), b -> new ObjectArrayList<>()).add(i);
            }
            if (i.getClass().isAnnotationPresent(WeaponOnly.class)) {
                swordEnchant.computeIfAbsent(i.getRarity(), b -> new ObjectArrayList<>()).add(i);
            }
            if (i.getClass().isAnnotationPresent(BowOnly.class)) {
                bowEnchant.computeIfAbsent(i.getRarity(), b -> new ObjectArrayList<>()).add(i);
            }
            if (i.getClass().isAnnotationPresent(RodOnly.class)) {
                rodEnchant.computeIfAbsent(i.getRarity(), b -> new ObjectArrayList<>()).add(i);
            }
            if (i.getClass().isAnnotationPresent(ItemOnly.class)) {
                itemEnchant.computeIfAbsent(i.getRarity(), b -> new ObjectArrayList<>()).add(i);
            }
        });
    }

    public void asyncTick() {
        while (true) {
            EnchantRequest poll = this.enchantQueues.poll();
            if (poll == null) {
                break;
            }
            IMythicItem mythic = poll.getMythic();
            try {
                Tier tier = Tier.ofTierUnsigned(mythic);
                tier.doEnchant(this, poll);
            } catch (Throwable e){
                poll.fail();
                e.printStackTrace();
            }
        }
    }

    public SuPromise<EnchantRequest> enchantItem(Player player, PlayerProfile profile, IMythicItem item, boolean hasBook) {
        SuPromise<EnchantRequest> runnables = new SuPromise<>();
        profile.disallow();
        EnchantRequest enchantRequest = new EnchantRequest(player, profile, item, hasBook, runnables);
        this.enchantQueues.add(enchantRequest);
        return runnables;
    }

    @Getter
    @AllArgsConstructor
    enum Tier {
        TIER_1(1,
                (chance, enchMap, mythicItem) -> {
                    if (mythicItem.isDark()) { // 黑裤 必出Somber
                        var enchByClass = ThePit.getInstance().getEnchantmentFactor().getEnchByClass(SomberEnchant.class);
                        mythicItem.getEnchantments().put(enchByClass, 1);
                        return false;
                    } else if (mythicItem.isRage()) {
                        var rareRage = enchMap.get(EnchantmentRarity.RAGE_RARE);
                        var rage = enchMap.get(EnchantmentRarity.RAGE);
                        AbstractEnchantment abstractEnchantment = RandomUtil.randEnch(chance, rareRage, rage);
                        mythicItem.getEnchantments().put(abstractEnchantment, RandomUtil.rand(2, 1));
                        return abstractEnchantment.getRarity() == EnchantmentRarity.RAGE_RARE;
                    } else {
                        var abstractEnchantments = enchMap.get(EnchantmentRarity.NORMAL);
                        RandomUtil.randEnchMultipleApply(2, abstractEnchantments, mythicItem.getEnchantments()
                                , (a, i) -> i.getMaxEnchantLevel() > a);
                        return false;
                    }
                },
                (chance, enchMap, mythicItem) -> {
                    if (mythicItem.isRage() || mythicItem.isDark()) {
                        throw new IllegalEnchantInputException("Can't enchant RD mythicItem with mythBook");
                    }
                    var abstractEnchantments = enchMap.get(EnchantmentRarity.RARE);
                    RandomUtil.chooseAndApplyChecked(true, abstractEnchantments, mythicItem.getEnchantments(), 3,3, AbstractEnchantment::getMaxEnchantLevel);
                    return true;
                }
                , (item) -> {
            if (item.isDark()) {
                item.maxLive = RandomUtil.helpMeToChooseOneInt(20, 25, 30, 35);
            } else {
                item.maxLive = RandomUtil.rand(10, 3);
            }
        }),
        TIER_2(2,
                (chance, enchMap, mythicItem) -> {
                    if (mythicItem.isDark()) {
                        Object2IntOpenHashMap<AbstractEnchantment> enchantments1 = mythicItem.getEnchantments();
                        ObjectArrayList<AbstractEnchantment> rage = enchMap.get(EnchantmentRarity.DARK_RARE);
                        ObjectArrayList<AbstractEnchantment> normal = enchMap.get(EnchantmentRarity.DARK_NORMAL);
                        AbstractEnchantment abstractEnchantment = RandomUtil.chooseAndApplyMultipleTypeRN(chance, true, enchantments1, 1, normal, rage);
                        return abstractEnchantment.getRarity() == EnchantmentRarity.DARK_RARE;
                    }
                    Object2IntOpenHashMap<AbstractEnchantment> enchantments1 = mythicItem.getEnchantments();
                    ObjectArrayList<AbstractEnchantment> rare = enchMap.get(EnchantmentRarity.RARE);
                    ObjectArrayList<AbstractEnchantment> normal = enchMap.get(EnchantmentRarity.NORMAL);
                    int count = 0;
                    for (Integer value : enchantments1.values()) {
                        count += value;
                    }
                    //9
                    int clampi = Einstein.clampi(9 - count, 1, 3);
                    int min = Math.min(3,clampi);
                    List<AbstractEnchantment>[] normals;
                    List<AbstractEnchantment>[] rares;
                    if(mythicItem.isRage()){
                        ObjectArrayList<AbstractEnchantment> rareRage = enchMap.get(EnchantmentRarity.RAGE_RARE);
                        ObjectArrayList<AbstractEnchantment> normalRage = enchMap.get(EnchantmentRarity.RAGE);
                        normals = new List[]{normalRage,normal};
                        rares = new List[]{rareRage,rareRage};
                    } else {
                        normals = new List[]{normal};
                        rares = new List[]{rare};
                    }
                    var result = RandomUtil.randEnchMultipleApplyRNMultiple(3,chance, min, clampi, enchantments1,
                            (a, i) -> i.getMaxEnchantLevel() > a,rares,normals);
                    return result.stream().anyMatch(i -> i.getRarity().getParentType() == EnchantmentRarity.RarityType.RARE);
                },
                (chance, enchMap, mythicItem) -> TIER_1.useBook.invoke(chance, enchMap, mythicItem),
                (item) -> {
                    if (item.isDark()) {
                        if (RandomUtil.hasSuccessfullyByChance(0.01)) {
                            item.maxLive = 135;
                        } else {
                            item.maxLive = RandomUtil.helpMeToChooseOneInt(40, 45, 50, 55, 60);
                        }
                    } else  {
                        item.maxLive = RandomUtil.rand(20, 11);
                    }
                }),
        TIER_3(3,
                (chance, enchMap, mythicItem) -> TIER_2.normal.invoke(chance, enchMap, mythicItem),
                (chance, enchMap, mythicItem) -> TIER_1.useBook.invoke(chance, enchMap, mythicItem),
                (item) -> {
                    if (!item.isDark()) {
                        if (RandomUtil.hasSuccessfullyByChance(0.01)) { //Artifact Prefix -> 100 Lives
                            item.setMaxLive(100);
                        } else {
                            item.setMaxLive(RandomUtil.rand(35, 21)); //16-23
                        }
                    }
                });
        final int level;
        @Setter
        Func3<Double, Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>>, IMythicItem, Boolean> normal;
        @Setter
        Func3<Double, Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>>, IMythicItem, Boolean> useBook;
        @Setter
        Consumer<IMythicItem> post;

        public static Tier ofTier(IMythicItem item) {
            int tier = item.getTier();
            Tier[] values = values();
            return values[tier - 1];
        }
        public static Tier ofTierUnsigned(IMythicItem item) {
            int tier = item.getTier();
            Tier[] values = values();
            return values[tier];
        }


        public void doEnchant(EnchantmentTable enchTable, EnchantRequest enq) {
            PlayerProfile profile = enq.getProfile();
            IMythicItem item = enq.getMythic();
            String enchantingBook = profile.getEnchantingBook();
            var level = getEnchantMap(enchTable, item);
            if (level == null) {
                enq.fail();
                return;
            }
            boolean completed = false;
            boolean announce = false;

            double chance = NewConfiguration.INSTANCE.getChance(enq.getPlayer(), item.getColor(), item.getTier());
            try {
                item.tier++;
                if (!item.canUseBook() || enchantingBook == null) {
                    announce = normal.invoke(chance, level, item);
                } else {
                    profile.setEnchantingBook(null);
                    announce = useBook.invoke(chance, level, item);
                    item.boostedByBook = true;
                }

                completed = true;
            } catch (IllegalEnchantInputException e) {
                enq.fail();
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                int lastMaxLive = item.maxLive;

                post.accept(item);
                if (item.tier > 1) {
                    item.live = Einstein.clampi(item.live + item.maxLive - lastMaxLive, 0, item.maxLive);
                } else {
                    item.setLive(item.getMaxLive());
                }
                if (completed) {
                    enq.complete(item, true, announce);
                }
            }
            enq.getProfile().allow();
        }

        @Nullable
        private Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> getEnchantMap(EnchantmentTable enchTable, IMythicItem item) {
            Object2ObjectOpenHashMap<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> level = null;
            if (item instanceof MythicBowItem) {
                level = enchTable.bowEnchant;
            }
            if (item instanceof MythicSwordItem) {
                level = enchTable.swordEnchant;
            }
            if (item instanceof MythicLeggingsItem) {
                level = enchTable.armorEnchant;
            }
            if (item instanceof MagicFishingRod) {
                level = enchTable.rodEnchant;
            }
            return level;
        }
    }
}
