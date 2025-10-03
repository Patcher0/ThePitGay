package net.mizukilab.pit.hologram;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.SneakyThrows;
import net.mizukilab.pit.hologram.type.HelperHologram;
import net.mizukilab.pit.hologram.type.JumpAndFightHologram;
import net.mizukilab.pit.hologram.type.LeaderBoardHologram;
import net.mizukilab.pit.util.ClassUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/4 13:06
 */
@Getter
public class HologramFactory {

    protected final List<AbstractHologram> loopHologram;
    protected final List<AbstractHologram> normalHologram;

    public HologramFactory() {
        this.loopHologram = new LinkedList<>();
        this.normalHologram = new LinkedList<>();
    }

    @SneakyThrows
    public void init() {
        reg(new HelperHologram());
        reg(new JumpAndFightHologram());
        reg(new LeaderBoardHologram());
    }
    public void reg(AbstractHologram hologram) {
        if (hologram.shouldLoop()) {
            this.loopHologram.add(hologram);
        } else {
            this.normalHologram.add(hologram);
        }
    }
}
