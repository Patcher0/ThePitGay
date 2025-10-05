package net.mizukilab.pit.enchantment.info;

import cn.charlotte.pit.data.PlayerProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.mizukilab.pit.data.operator.Promise;
import net.mizukilab.pit.data.operator.SuPromise;
import net.mizukilab.pit.item.IMythicItem;
import org.bukkit.entity.Player;

@Getter
public class EnchantRequest {
    Player player;
    PlayerProfile profile;
    IMythicItem mythic;
    SuPromise<EnchantRequest> promise;
    boolean fail = false;
    boolean announcement = false;
    boolean useBook;
    public EnchantRequest(Player player,PlayerProfile profile,IMythicItem mythic, boolean useBook, SuPromise<EnchantRequest> promise) {
        this.player = player;
        this.mythic = mythic;
        this.useBook = useBook;
        this.promise = promise;
        this.profile = profile;
    }
    public void complete(IMythicItem mythic, final boolean finallyUseBook,final boolean announcement){
        this.useBook = finallyUseBook;
        this.mythic = mythic;
        this.announcement = announcement;
        promise.ret(this);
    }
    public void fail(){
        fail = true;
        promise.ret(this);
    }
}
