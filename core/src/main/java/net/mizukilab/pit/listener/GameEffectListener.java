package net.mizukilab.pit.listener;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.KillRecap;
import cn.charlotte.pit.data.sub.PerkData;
import cn.charlotte.pit.data.sub.QuestData;
import cn.charlotte.pit.event.OriginalTimeChangeEvent;
import cn.charlotte.pit.event.PitDamageEvent;
import cn.charlotte.pit.event.PitDamagePlayerEvent;
import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkFactory;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MobEffectList;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.EnchantmentFactor;
import net.mizukilab.pit.enchantment.param.event.NotPlayerOnly;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.parm.listener.*;
import net.mizukilab.pit.parm.type.BowOnly;
import net.mizukilab.pit.parm.type.ThrowOnly;
import net.mizukilab.pit.quest.AbstractQuest;
import net.mizukilab.pit.util.Einstein;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.RangedStreamLineList;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemUtil;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/2 12:40
 */
@Skip
public class GameEffectListener implements Listener {

    private final DecimalFormat numFormatTwo = new DecimalFormat("0.00");
    @SneakyThrows
    public static void processKilled(IPlayerKilledEntity ins, int level, Player killer, Entity target, AtomicDouble coin, AtomicDouble exp) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handlePlayerKilled", int.class, Player.class, Entity.class, AtomicDouble.class, AtomicDouble.class);
            if (method.isAnnotationPresent(PlayerOnly.class)) {
                if (target instanceof Player player) {
                    ins.handlePlayerKilled(level, killer, player, coin, exp);
                }
            } else if (method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(target instanceof Player)) {
                    ins.handlePlayerKilled(level, killer, target, coin, exp);
                }
            } else {
                ins.handlePlayerKilled(level, killer, target, coin, exp);
            }
        }
    }

    @SneakyThrows
    public static void processBeKilledByEntity(IPlayerBeKilledByEntity ins, int level, Player myself, Entity target, AtomicDouble coin, AtomicDouble exp) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handlePlayerBeKilledByEntity", int.class, Player.class, Entity.class, AtomicDouble.class, AtomicDouble.class);
            if (method.isAnnotationPresent(PlayerOnly.class)) {
                if (target instanceof Player player) {
                    ins.handlePlayerBeKilledByEntity(level, myself, player, coin, exp);
                }
            } else if (method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(target instanceof Player)) {
                    ins.handlePlayerBeKilledByEntity(level, myself, target, coin, exp);
                }
            } else {
                ins.handlePlayerBeKilledByEntity(level, myself, target, coin, exp);
            }
        }
    }

    private static void fixDamage(EntityDamageByEntityEvent event, Entity damager1) {
        if (damager1 instanceof Player attacker) {
            //修正伤害
            ItemStack itemInHand = attacker.getItemInHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR
                    || itemInHand.getType() == Material.FISHING_ROD) {
                event.setDamage(1);
            } else {
                event.setDamage(Einstein.clamp(event.getDamage(), 0, 9));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFired(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
//            event.setCancelled(true);
////            if (player.getHealth() <= event.getFinalDamage()) {
////                player.damage(player.getMaxHealth() * 100);
////            } else {
////                double newHealth = player.getHealth() - event.getFinalDamage();
////                // 确保生命值在有效范围内
////                player.setHealth(Math.max(0, Math.min(newHealth, player.getMaxHealth())));
////            }
//
            PlayerProfile.getPlayerProfileByUuid(player.getUniqueId())
                    .setNoDamageAnimations(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void modifyLeatherArmor(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player op) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(op.getUniqueId());
            if (ItemUtil.isTheItemType(op.getInventory().getHelmet(),"kings_helmet")) {
                //0.12是每点伤害 铁裤和皮革裤子的减免差距，所以减去0.12*伤害值，以增加皮革裤子为铁裤防御
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) - 0.06 * event.getDamage());
            }
            AbstractPitItem leggings = profile.leggings;
            if(leggings instanceof MythicLeggingsItem i){
                if(i.getColor() == MythicColor.DARK){
                    return;
                }
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) - 0.12 * event.getDamage());
            }

        }
    }

    @EventHandler
    public void addEnchantToArrow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            if (profile.heldItem instanceof MythicBowItem bowItem) {
                event.getProjectile().setMetadata("enchant",
                        new FixedMetadataValue(ThePit.getInstance(), bowItem.getEnchantments()));
            }
            //for gc
            Utils.pointMetadataAndRemove(event.getProjectile(), 500, "enchant");
        }
    }

    @EventHandler
    public void onTimeChange(OriginalTimeChangeEvent event) {
        CC.boardCast("Time change to: " + event.getTime());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        fixDamage(event, damagerEntity);

        ThePit instance = ThePit.getInstance();
        PerkFactory perkFactory = instance.getPerkFactory();
        EnchantmentFactor enchantmentFactor = instance.getEnchantmentFactor();
        AtomicDouble finalDamage = new AtomicDouble(event.getDamage());
        AtomicDouble boostDamage = new AtomicDouble(1);
        AtomicBoolean cancel = new AtomicBoolean(false);
        Player damager = null;

        PlayerProfile playerProfileByUuid = PlayerProfile.getPlayerProfile(damagerEntity);
        Set<AbstractPerk> disabledPerks = instance.getDisabledPerks();
        if (damagerEntity instanceof Player) {
            damager = (Player) damagerEntity;

            if (PlayerUtil.critical(damager)){
                boostDamage.set(1.5);
            }
            if (NewConfiguration.INSTANCE.getNoobProtect() && playerProfileByUuid.getPrestige() <= 0 && playerProfileByUuid.getLevel() < NewConfiguration.INSTANCE.getNoobProtectLevel()) {
                boostDamage.getAndAdd(NewConfiguration.INSTANCE.getNoobDamageBoost() - 1);
            }
            //TODO perk handlers
            //perk handler
            //faster
            processPerkATK(event, playerProfileByUuid, perkFactory, disabledPerks, damager, finalDamage, boostDamage, cancel);
            //TODO heldItem -- leggings handlers
            boolean shouldIgnoreEnchant;
            List<IAttackEntity> attackEntities = enchantmentFactor.getAttackEntities();
            if (!attackEntities.isEmpty()) {
                shouldIgnoreEnchant = PlayerUtil.shouldIgnoreEnchant(damager
                        , event.getEntity());
            } else {
                shouldIgnoreEnchant = false;
            }
            IMythicItem heldItem = (IMythicItem) playerProfileByUuid.heldItem;
            IMythicItem leggings = (IMythicItem) playerProfileByUuid.leggings;
            //十分甚至九分的重构

            if ((heldItem != null || leggings != null)) {
                processAttackWithLeggingAndHeldItems(shouldIgnoreEnchant, event, damager, finalDamage, boostDamage, cancel, heldItem, leggings);
            }
            //TODO quest handler

            QuestData currentQuest1 = playerProfileByUuid.getCurrentQuest();
            if (currentQuest1 != null) {
                AbstractQuest handle = currentQuest1.getHandle();
                if (handle instanceof IAttackEntity iac) {
                    processAttackEntity(iac, currentQuest1.getLevel(), damager, event.getEntity(), event.getFinalDamage(), finalDamage, boostDamage, cancel);

                }
            }
            //When you were hurt by a projectile
        } else if (damagerEntity instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
            damager = (Player) (projectile.getShooter());

            if (NewConfiguration.INSTANCE.getNoobProtect() && playerProfileByUuid.getPrestige() <= 0 && playerProfileByUuid.getLevel() < NewConfiguration.INSTANCE.getNoobProtectLevel()) {
                boostDamage.getAndAdd(NewConfiguration.INSTANCE.getNoobDamageBoost() - 1);
            }

            for (IPlayerShootEntity ins : perkFactory.getPlayerShootEntities()) {
                AbstractPerk perk = (AbstractPerk) ins;
                int level = perk.getPlayerLevel(damager);
                processShootEntity(ins, level, damager, event.getEntity(), damagerEntity, event.getFinalDamage(), finalDamage, boostDamage, cancel);
            }
            //projectile handler
            List<MetadataValue> enchant = projectile.getMetadata("enchant");
            if (!enchant.isEmpty()) {
                final MetadataValue value = enchant.get(0);
                final Object enchants = value.value();
                if (enchants instanceof Object2IntOpenHashMap a) {
                    Object2IntOpenHashMap<AbstractEnchantment> ench = (Object2IntOpenHashMap<AbstractEnchantment>) a;
                    boolean shouldIgnoreEnchant = PlayerUtil.shouldIgnoreEnchant(damager, event.getEntity());
                    processShot(shouldIgnoreEnchant, event, ench, damager, finalDamage, boostDamage, cancel);
                }
            }
            QuestData currentQuest1 = playerProfileByUuid.getCurrentQuest();
            if (currentQuest1 != null) {
                AbstractQuest handle = currentQuest1.getHandle();
                if (handle instanceof IPlayerShootEntity isc) {
                    processShootEntity(isc, currentQuest1.getLevel(), damager, event.getEntity(), damagerEntity, event.getFinalDamage(), finalDamage, boostDamage, cancel);

                }
            }
            projectile.removeMetadata("enchant", instance); //garbage collector
        }

        if (event.getEntity() instanceof Player player) {

            boolean npc = PlayerUtil.isNPC(player);

            if (!npc) {//针对npc不生效。
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(event.getEntity().getUniqueId());

                if (NewConfiguration.INSTANCE.getNoobProtect() && profile.getPrestige() <= 0
                        && profile.getLevel() < NewConfiguration.INSTANCE.getNoobProtectLevel()) {
                    boostDamage.getAndAdd(NewConfiguration.INSTANCE.getNoobDamageReduce() - 1);
                }
                if (PlayerUtil.isEquippingAngelChestplate(player)) {
                    boostDamage.getAndAdd(-0.1);
                }
                if (damagerEntity instanceof Player) {
                    processPerksDMGed(event, player, profile, perkFactory, disabledPerks, finalDamage, boostDamage, cancel);

                    if (!enchantmentFactor.getPlayerDamageds().isEmpty()) {
                        boolean shouldIgnoreEnchant = PlayerUtil.shouldIgnoreEnchant(damager, event.getEntity());
                        IMythicItem leggings = (IMythicItem) profile.leggings;
                        IMythicItem sword = (IMythicItem) profile.heldItem;
                        if (!(sword instanceof MythicLeggingsItem)) {
                            processEnchDMGed(event, player, leggings, shouldIgnoreEnchant, finalDamage, boostDamage, cancel);
                        }
                        processEnchDMGed(event, player, sword, shouldIgnoreEnchant, finalDamage, boostDamage, cancel);

                    }


                    QuestData currentQuest1 = profile.getCurrentQuest();
                    processQuestDmged(event, player, currentQuest1, finalDamage, boostDamage, cancel);
                } else if (damagerEntity instanceof Projectile
                        && ((Projectile) damagerEntity).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) damagerEntity).getShooter();
                    processPerksDMGed(event, player, profile, perkFactory, disabledPerks, finalDamage, boostDamage, cancel);

                    if (!enchantmentFactor.getPlayerDamageds().isEmpty()) {
                        boolean shouldIgnoreEnchant = PlayerUtil.shouldIgnoreEnchant(damager, event.getEntity());
                        IMythicItem leggings = (IMythicItem) profile.leggings;
                        IMythicItem sword = (IMythicItem) profile.heldItem;
                        processEnchDMGed(event, player, leggings, shouldIgnoreEnchant, finalDamage, boostDamage, cancel);
                        processEnchDMGed(event, player, sword, shouldIgnoreEnchant, finalDamage, boostDamage, cancel);
                    }


                    QuestData currentQuest1 = profile.getCurrentQuest();
                    processQuestDmged(event, player, currentQuest1, finalDamage, boostDamage, cancel);
                }
                if (damager != null) {
                    new PitDamagePlayerEvent(event, damager, event.getFinalDamage(), event.getDamage(), player).callEvent();
                    new PitDamageEvent(event, damager, event.getFinalDamage(), event.getDamage()).callEvent();
                    RangedStreamLineList<KillRecap.DamageData> damageLogs = playerProfileByUuid.getKillRecap().getDamageLogs();
                    if (!damageLogs.isEmpty()) {
                        damageLogs.peekFirst().setBoostDamage(boostDamage.get());
                    }
                }
            }
            //mirror enchant code end
            player.setLastDamageCause(event);
            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            if (damager != null) {
                handle.killer = ((CraftPlayer) damager).getHandle();
                if(NewConfiguration.INSTANCE.getNo1_8Effect()){
                    handle.lastDamage = Float.MAX_VALUE;
                }
            }
            //we need to ensure the final damage is pushed into the event
            event.setDamage(finalDamage.get() * Einstein.clamp(boostDamage.get(), 0, 100));
        }

        if (!cancel.get()) {
            if (damagerEntity instanceof FishHook hook) {
                final PlayerProfile damagerProfile = PlayerProfile.getPlayerProfileByUuid(((Player) hook.getShooter()).getUniqueId());
                damagerProfile.setFishingNumber(damagerProfile.getFishingNumber() + 1);
            }
        } else {
            event.setCancelled(true);
            return;
        }
        debug(event, damagerEntity, cancel);
    }

    static Vector ZERO = new Vector();

    private void debug(EntityDamageByEntityEvent event, Entity damagerEntity, AtomicBoolean cancel) {
        if (event.getEntity() instanceof Player player) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            if (profile.getPlayerOption().isDebugDamageMessage()) {
                player.sendMessage(CC.translate("&7受到伤害(Damage/Final Damage): &c" + numFormatTwo.format(event.getDamage()) + "&7/&c" + numFormatTwo.format(event.getFinalDamage())));
            }
        }
        if (damagerEntity instanceof Player player && !(event.getEntity() instanceof Item)) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            if (profile.getPlayerOption().isDebugDamageMessage()) {
                player.sendMessage(CC.translate("&7造成伤害(Damage/Final Damage): &c" + numFormatTwo.format(event.getDamage()) + "&7/&c" + numFormatTwo.format(event.getFinalDamage())));
                final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                final double value = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                final float enchantDamage = EnchantmentManager.a(entityPlayer.bA(), ((CraftLivingEntity) event.getEntity()).getHandle().getMonsterType());
                final boolean critical = entityPlayer.fallDistance > 0.0F && !entityPlayer.onGround && !entityPlayer.k_() && !entityPlayer.V() && !entityPlayer.hasEffect(MobEffectList.BLINDNESS) && entityPlayer.vehicle == null;
                player.sendMessage(CC.translate("&7基础: &c" + value + "&7,附魔伤害: &c" + enchantDamage + "&7,cancel:" + cancel.get() + " ,暴击: &c" + critical));
            }
        }
    }

    private void processAttackWithLeggingAndHeldItems(boolean shouldIgnore, EntityDamageByEntityEvent event, Player finalDamager1, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel, IMythicItem heldItem, IMythicItem leggings) {
        BiConsumer<AbstractEnchantment, Integer> consumer = (enchant, level) -> {
            if (enchant instanceof IAttackEntity ins) {
                if (shouldIgnore && (enchant.getRarity() != EnchantmentRarity.DARK_NORMAL
                        && enchant.getRarity() != EnchantmentRarity.DARK_RARE)) {
                    return;
                }
                processAttackEntity(ins, level, finalDamager1, event.getEntity(), event.getFinalDamage(), finalDamage, boostDamage, cancel);
            }
        };
        if (heldItem != null) {
            heldItem.getEnchantments().forEach(consumer);
        }
        if (leggings != null) {
            leggings.getEnchantments().forEach(consumer);
        }
    }

    private void processShot(boolean shouldIgnoreEnch, EntityDamageByEntityEvent event, Object2IntOpenHashMap<AbstractEnchantment> ench, Player finalDamager, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        ench.forEach((enchantment, level) -> {
            if (enchantment instanceof IPlayerShootEntity enchantmentCasted) {
                EnchantmentRarity rarity = enchantment.getRarity();
                if (shouldIgnoreEnch && (rarity != EnchantmentRarity.DARK_NORMAL && rarity != EnchantmentRarity.DARK_RARE)) {
                    return;
                }

                this.processShootEntity(enchantmentCasted, level, finalDamager, event.getEntity(), event.getDamager(), event.getFinalDamage(), finalDamage, boostDamage, cancel);
            }
        });
    }

    private void processEnchDMGed(EntityDamageByEntityEvent event, Player player, IMythicItem leggings, boolean shouldIgnoreEnchant, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (leggings != null) {
            leggings.getEnchantments().forEach((enchant, level) -> {
                if (shouldIgnoreEnchant && (enchant.getRarity() != EnchantmentRarity.DARK_NORMAL
                        && enchant.getRarity() != EnchantmentRarity.DARK_RARE)) {
                    return;
                }
                if (enchant instanceof IPlayerDamaged ins) {
                    processPlayerDamaged(ins, level, player, event.getDamager(), event.getFinalDamage(), finalDamage, boostDamage, cancel);
                }
            });
        }
    }

    private void processQuestDmged(EntityDamageByEntityEvent event, Player player, QuestData currentQuest1, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (currentQuest1 != null) {
            AbstractQuest handle = currentQuest1.getHandle();
            if (handle instanceof IPlayerDamaged ins) {
                processPlayerDamaged(ins, currentQuest1.getLevel(), player, event.getDamager(), event.getFinalDamage(), finalDamage, boostDamage, cancel);
            }
        }
    }

    private void processPerkATK(EntityDamageByEntityEvent event, PlayerProfile profile, PerkFactory perkFactory, Set<AbstractPerk> disabledPerks, Player finalDamager1, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        profile.getUnlockedPerkMap().values().forEach(i -> {
            AbstractPerk abstractPerk = i.getHandle(perkFactory.getPerkMap());

            if (abstractPerk == null || !abstractPerk.isPassive()) {
                return;
            }
            if (disabledPerks.contains(abstractPerk)) {
                return;
            }
            if (abstractPerk instanceof IAttackEntity attackEntity) {

                processAttackEntity(attackEntity, abstractPerk.getPlayerLevel(finalDamager1), finalDamager1, event.getEntity(), event.getFinalDamage(), finalDamage, boostDamage, cancel);

            }
        });
        profile.getChosePerk().values().forEach(i -> {
            AbstractPerk abstractPerk = i.getHandle(perkFactory.getPerkMap());
            if (abstractPerk == null || abstractPerk.isPassive()) {
                return;
            }
            if (disabledPerks.contains(abstractPerk)) {
                return;
            }
            if (abstractPerk instanceof IAttackEntity attackEntity) {

                processAttackEntity(attackEntity, i.getLevel(), finalDamager1, event.getEntity(), event.getFinalDamage(), finalDamage, boostDamage, cancel);

            }
        });
    }

    private void processPerksDMGed(EntityDamageByEntityEvent event, Player player, PlayerProfile profile, PerkFactory perkFactory, Set<AbstractPerk> disabledPerks, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        profile.getUnlockedPerkMap().values().forEach(i -> {
            AbstractPerk abstractPerk = i.getHandle(perkFactory.getPerkMap());
            takeEffect(event, player, disabledPerks, finalDamage, boostDamage, cancel, i, abstractPerk);
        });
        profile.getChosePerk().values().forEach(i -> {
            AbstractPerk abstractPerk = i.getHandle(perkFactory.getPerkMap());
            takeEffect(event, player, disabledPerks, finalDamage, boostDamage, cancel, i, abstractPerk);
        });
    }

    private void takeEffect(EntityDamageByEntityEvent event, Player player, Set<AbstractPerk> disabledPerks, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel, PerkData i, AbstractPerk abstractPerk) {
        if (abstractPerk == null || abstractPerk.isPassive()) {
            return;
        }
        if (disabledPerks.contains(abstractPerk)) {
            return;
        }
        if (abstractPerk instanceof IPlayerDamaged ins) {

            processPlayerDamaged(ins, i.getLevel(), player, event.getDamager(), event.getFinalDamage(), finalDamage, boostDamage, cancel);
        }
    }

    @EventHandler
    public void onItemDamaged(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        IMythicItem mm = Utils.getMythicItem(item);
        if (mm != null) {
            mm.getEnchantments().forEach((i, a) -> {
                if (i instanceof IItemDamage itemDamage) {
                    itemDamage.handleItemDamaged(a, event.getItem(), event.getPlayer(), atomicBoolean);
                }
            });
        }
        if (atomicBoolean.get()) {
            event.setCancelled(true);
        }
    }

    @SneakyThrows
    private void processShootEntity(IPlayerShootEntity ins, int level, Player damager, Entity target, Entity damageSource, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (level != -1) {
            Class<? extends IPlayerShootEntity> aClass = ins.getClass();
            Method handleShootEntity = aClass.getMethod("handleShootEntity", int.class, Player.class, Entity.class, double.class, AtomicDouble.class, AtomicDouble.class, AtomicBoolean.class);

            if (handleShootEntity.isAnnotationPresent(BowOnly.class) && !(damageSource instanceof Arrow)) {
                return;
            }
            if (handleShootEntity.isAnnotationPresent(ThrowOnly.class) && damager instanceof Arrow) {
                return;
            }

            if (aClass.isAnnotationPresent(PlayerOnly.class) || handleShootEntity.isAnnotationPresent(PlayerOnly.class)) {
                if (target instanceof Player player) {
                    ins.handleShootEntity(level, damager, player, damage, finalDamage, boostDamage, cancel);
                }
            } else if (aClass.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(target instanceof Player)) {
                    ins.handleShootEntity(level, damager, target, damage, finalDamage, boostDamage, cancel);
                }
            } else {
                ins.handleShootEntity(level, damager, target, damage, finalDamage, boostDamage, cancel);
            }
        }
    }

    @SneakyThrows
    private void processPlayerDamaged(IPlayerDamaged ins, int level, Player player, Entity damager, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handlePlayerDamaged", int.class, Player.class, Entity.class, double.class, AtomicDouble.class, AtomicDouble.class, AtomicBoolean.class);
            if (method.isAnnotationPresent(PlayerOnly.class)) {
                if (damager instanceof Player damagerPlayer) {
                    ins.handlePlayerDamaged(level, player, damagerPlayer, damage, finalDamage, boostDamage, cancel);
                }
            } else if (method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(damager instanceof Player)) {
                    ins.handlePlayerDamaged(level, player, damager, damage, finalDamage, boostDamage, cancel);
                }
            } else {
                ins.handlePlayerDamaged(level, player, damager, damage, finalDamage, boostDamage, cancel);
            }
        }
    }

    @SneakyThrows
    private void processAttackEntity(IAttackEntity ins, int level, Player damager, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handleAttackEntity", int.class, Player.class, Entity.class, double.class, AtomicDouble.class, AtomicDouble.class, AtomicBoolean.class);
            if (method.isAnnotationPresent(PlayerOnly.class)) {
                if (target instanceof Player player) {
                    ins.handleAttackEntity(level, damager, player, damage, finalDamage, boostDamage, cancel);
                }
            } else if (method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(target instanceof Player) && damager == null) {
                    ins.handleAttackEntity(level, null, target, damage, finalDamage, boostDamage, cancel);
                }
            } else {
                ins.handleAttackEntity(level, damager, target, damage, finalDamage, boostDamage, cancel);
            }
        }
    }

}
