package net.mizukilab.pit.util.aabb;

import net.mizukilab.pit.util.chat.CC;
import org.bukkit.entity.Player;

public class Test {
    private int x;

    public Test() {
        this.x = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void record(Player player) {
        x++;
        player.sendMessage(CC.translate("&dvalue: &f" + x));
    }

    public void record(Player player, String message) {
        x++;
        player.sendMessage(CC.translate("&dvalue: &f" + x + " &dmessage: &f" + message));
    }
}
