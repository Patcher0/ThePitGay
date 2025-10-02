package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.ThePit;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@BowOnly
@AutoRegister
public class DemonHenEnchant extends AbstractEnchantment implements IActionDisplayEnchant, IPlayerShootEntity, Listener, IPlayerKilledEntity {
    private final Map<UUID, Cooldown> cooldown = new Reference2ObjectArrayMap<>();
    Set<Entity> masters = new CopyOnWriteArraySet<>();
    Set<Map.Entry<Entity, LivingEntity>> entitySet = new CopyOnWriteArraySet<>();
    BukkitTask scheduledTask = Bukkit.getScheduler().runTaskTimerAsynchronously(ThePit.getInstance(), () -> {
        Set<Map.Entry<Entity, LivingEntity>> entities = new HashSet<>();
        entitySet.forEach(s -> {
            LivingEntity i = s.getValue();
            if (i.isOnGround()) {
                entities.add(s);
                Location location = i.getLocation();
                if (i.isDead()) {
                    return;
                }
                final float healthScaled = (float) (i.getHealth() / i.getMaxHealth());
                i.remove();
                World world = location.getWorld();
                Collection<Entity> nearbyEntities = world.getNearbyEntities(location, 3, 3, 3);
                Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                    masters.add(s.getKey());
                    ((CraftWorld) world).getHandle().createExplosion(((CraftEntity) s.getKey()).getHandle(), location.getX(), location.getY(), location.getZ(),
                            (1.25F * healthScaled), false, false);
                    float xyz = (float) ((Math.random() - 0.5) * 2);
                    nearbyEntities.forEach(a -> {
                        if (a instanceof Player player) {
                            player.setVelocity(new Vector(xyz, Math.abs(xyz), xyz));
                            return;
                        }
                        a.setVelocity(new Vector(xyz, Math.abs(xyz), xyz));
                    });
                    masters.remove(s.getKey());
                });
            }
        });
        entitySet.removeAll(entities);
    }, 0, 20);

    @Override
    public String getEnchantName() {
        return "恶魔鸡";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "evil_hen";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return new Cooldown(3, TimeUnit.SECONDS);
    }

    @Override
    public String getUsefulnessLore(int i) {
        return "箭击中玩家时会生成 &f" + i + "&7 只爆炸性的鸡。/s" +
                "小鸡在爆炸中对玩家造成大量击退和 &c1.0 ❤&7 扩散伤害 (2秒冷却)";
    }

    @PlayerOnly
    @BowOnly
    @Override
    public void handleShootEntity(int i, Player player, Entity entity, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        Cooldown cooldown1 = cooldown.computeIfAbsent(player.getUniqueId(), z -> new Cooldown(0, TimeUnit.SECONDS));
        if (cooldown1.hasExpired()) {
            cooldown.put(player.getUniqueId(), getCooldown());
            Location location = entity.getLocation();
            for (int level = 0; level < i; level++) {
                Location henLocation = getHenLocation(location);
                LivingEntity entity1 = (LivingEntity) henLocation.getWorld().spawnEntity(henLocation, EntityType.CHICKEN);
                entity1.setCustomNameVisible(true);
                entity1.setCustomName("§c恶魔鸡");
                entitySet.add(Maps.immutableEntry(player, entity1));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldown.remove(e.getPlayer().getUniqueId());
    }

    public Location getHenLocation(Location location) {
        Location clone = location.clone();
        ThreadLocalRandom current = ThreadLocalRandom.current();
        float rad = (float) Math.toRadians(current.nextInt(360) % 360 - 180);
        float rad2 = (float) Math.toRadians(current.nextInt(360) % 360 - 180);
        float range = 2;
        float x = MathHelper.sin(rad) * range;
        float z = MathHelper.cos(rad2) * range;
        return clone.subtract(x, -1, z);
    }

    @EventHandler
    public void onExped(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (masters.contains(e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        if (target.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            coins.set(coins.get() * 0.2);
            experience.set(experience.get() * 0.2);
        }
    }

    @Override
    public String getText(int level, Player player) {
        return (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).getRemaining()).replace(" ", ""));
    }
}
