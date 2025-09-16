package cn.charlotte.pit.event

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PitDamagePlayerEvent(entEvent: EntityDamageByEntityEvent,attack: Player, damage: Double, finalDamage: Double, val victim: Player) :
    PitDamageEvent(entEvent,attack, damage, finalDamage)