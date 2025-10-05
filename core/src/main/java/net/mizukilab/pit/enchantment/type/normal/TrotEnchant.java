package net.mizukilab.pit.enchantment.type.normal;

import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

/**
 * @Author: Starry_Killer
 * @Created_In: 2023/11/22 18:15
 */

@ArmorOnly
@Skip
@AutoRegister
public class TrotEnchant extends AbstractEnchantment implements MovementHandler, Listener {

    @SneakyThrows
    public TrotEnchant() {
        try {
            iSpigot.INSTANCE.addMovementHandler(this);
        } catch (NoClassDefFoundError ignore) {
        }
    }

    @Override
    public String getEnchantName() {
        return "疾走";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Trot";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7穿戴时行走速度提升 &b" + (enchantLevel == 3 ? "20%" : (enchantLevel == 2 ? "10%" : "5%"));
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
    }

    @Override
    public void handleUpdateSpeed(Player player, AtomicDouble speed, Location in, Location out) {
        IMythicItem leggings = (IMythicItem) PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).leggings;
        boolean shouldIgnoreEnchant = PlayerUtil.shouldIgnoreEnchant(player);
        float targetSpeed = 0.2F;
        if (leggings != null && !shouldIgnoreEnchant) {
            int level = this.getItemEnchantLevel(leggings);
            switch (level) {
                case 1:
                    targetSpeed = 0.21F; // 增加5%速度
                    break;
                case 2:
                    targetSpeed = 0.22F; // 增加10%速度
                    break;
                case 3:
                    targetSpeed = 0.24F; // 增加20%速度
                    break;
                default:
                    break;
            }

            Location location = player.getLocation().add(0.0, 0.5, 0.0);
            Utils.sendRedstoneParticle(player, location, 255f, 255f, 255f);
        }
        speed.set(targetSpeed);
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
    }

}