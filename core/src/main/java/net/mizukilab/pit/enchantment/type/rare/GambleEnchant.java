package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.Einstein;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.random.RandomUtil;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/2 18:35
 */
@WeaponOnly
@Skip
public class GambleEnchant extends AbstractEnchantment implements IAttackEntity, Listener {
    Object2ObjectOpenHashMap<UUID,Cooldown> cooldowns = new Object2ObjectOpenHashMap<>();
    @Override
    public String getEnchantName() {
        return "赌徒";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "gamble_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击时有 &e50% &7的几率对自身或敌人"
                + "/s&7额外造成 &c" + enchantLevel + "❤ &7的&c必中&7伤害 &7&c(必中伤害无法被免疫与抵抗)";
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Cooldown cooldown = cooldowns.get(attacker.getUniqueId());
        if(cooldown == null){
            cooldown = new Cooldown(0);
        }
        if(cooldown.hasExpired()) {
            if (RandomUtil.hasSuccessfullyByChance(0.5)) {
                cooldowns.put(attacker.getUniqueId(), new Cooldown(50));
                Player targetPlayer = (Player) target;
                Player gamblePlayer = attacker;

                if (RandomUtil.hasSuccessfullyByChance(0.5)) {
                    gamblePlayer = targetPlayer;
                }
                if (gamblePlayer.getHealth() > enchantLevel * 2) {
                    PlayerUtil.damage(
                            attacker, gamblePlayer,
                            PlayerUtil.DamageType.TRUE,
                            enchantLevel * 2,
                            false
                    );
                } else {
                    if (target == gamblePlayer) {
                        finalDamage.set(1000);
                        return;
                    }
                    gamblePlayer.damage(1000);
                }
            }
        }
    }
}
