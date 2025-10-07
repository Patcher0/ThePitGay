package net.mizukilab.pit.event;

import cn.charlotte.pit.event.PitEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
@RequiredArgsConstructor
@Data
public class TrueDamageByEntityEvent extends PitEvent {

    private final Player attacker;
    private final Player victim;
    private final boolean canImmune;
    private final double damage;

}
