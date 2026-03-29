package net.mizukilab.pit.event;

import cn.charlotte.pit.event.PitEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class TrueDamageByEntityEvent extends PitEvent {

    private final Player attacker;
    private final Player victim;
    private final boolean canImmune;
    private final double damage;

}
