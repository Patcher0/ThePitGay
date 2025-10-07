package net.mizukilab.pit.event;

import cn.charlotte.pit.event.PitEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class CanImmuneEvent extends PitEvent {
    final Player victim;
    boolean canImmune = false;
}
