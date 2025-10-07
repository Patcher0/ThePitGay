package net.mizukilab.pit.enchantment.type.normal;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.event.CanImmuneEvent;
import net.mizukilab.pit.event.TrueDamageByEntityEvent;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.VectorUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/17 21:47
 */
@Skip
@ArmorOnly
@AutoRegister
public class MirrorEnchant extends AbstractEnchantment implements Listener {

    @Override
    public String getEnchantName() {
        return "平面镜";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Mirror";
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
        return "&7你无视任何类型为&f真实&7且来源为其他玩家的伤害"
                + (enchantLevel > 1 ? "/s&7且反弹真实伤害的 &f" + (enchantLevel * 25 - 25) + "% &7至伤害来源(伤害类型为&c必中&7) (0.5秒冷却)"
                + "/s&c(必中伤害无法被抵抗或免疫)" : "");
    }
    @EventHandler
    public void canImmune(CanImmuneEvent event){
        Player victim = event.getVictim();
        PlayerProfile playerProfileByUuid = PlayerProfile.getPlayerProfileByUuid(victim.getUniqueId());;
        AbstractPitItem leggings = playerProfileByUuid.leggings;
        if(leggings != null){
            if(isItemHasEnchant(leggings)){
                event.setCanImmune(true);
            }
        }
    }
    @EventHandler
    public void mirrorEnch(TrueDamageByEntityEvent e){
        Player victim = e.getVictim();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(victim.getUniqueId());
        AbstractPitItem leggings = profile.leggings;
        if(leggings != null){
            int itemEnchantLevel = this.getItemEnchantLevel(leggings);
            if(itemEnchantLevel >= 2){
                PlayerUtil.damage(e.getAttacker(), PlayerUtil.DamageType.TRUE, e.getDamage() * (0.25 * itemEnchantLevel - 0.25), false);
                VectorUtil.knock(e.getAttacker(), 0.41f,0.63F);
            }
        }
    }
//    @EventHandler
//    public void onHandleDamageEvent(PitDamagePlayerEvent event){
//        Player victim = event.getVictim();
//        Player damager = event.getAttacker();
//        PlayerProfile playerProfile = PlayerProfile.getPlayerProfile(victim);
//        AbstractPitItem leggings = playerProfile.leggings;
//        EntityDamageByEntityEvent event1 = event.getEvent();
//        double finalDamage = event.getFinalDamage();
//        boolean modded = false;
//        if(leggings != null) {
//            int enchantLevel = getItemEnchantLevel(leggings);
//            if (enchantLevel > 1 && finalDamage > 0 && finalDamage < 1000) {
//                MetadataValue mirrorLatestActive = null;
//                List<MetadataValue> values = victim.getMetadata("mirror_latest_active");
//                if (values != null && !values.isEmpty()) {
//                    mirrorLatestActive = values.get(0);
//                }
//                long l = System.currentTimeMillis();
//                if ((mirrorLatestActive == null ||
//                        l - mirrorLatestActive.asLong() > 500L)) {
//                    //damage giveback
//                    victim.setMetadata("mirror_latest_active", new FixedMetadataValue(ThePit.getInstance(), l));
//                    if (!victim.getUniqueId().equals(damager.getUniqueId())) {
//                        damager.damage(0.01, victim);
//                        float mirrorDamage = (float) (((enchantLevel * 25 - 25) * 0.01) * finalDamage);
//                        // 确保生命值在有效范围内
//                        event1.setDamage(mirrorDamage);
//                        modded = true;
//                    }
//                }
//            }
//            if (enchantLevel > 0 && !modded && finalDamage > 0 && finalDamage < 1000) {
//                event1.setCancelled(true);
//            }
//        }
//    }
}
