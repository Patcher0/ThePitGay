package cn.charlotte.pit.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/30 1:09
 */
@Getter
public class PitDamageEvent extends PitEvent {

    private final Player attacker;
    private final double finalDamage;
    private final double damage;
    EntityDamageByEntityEvent event;
    public PitDamageEvent(EntityDamageByEntityEvent ent,Player attacker, double finalDamage, double damage) {
        this.attacker = attacker;
        this.event = ent;
        this.finalDamage = finalDamage;
        this.damage = damage;
    }
}
