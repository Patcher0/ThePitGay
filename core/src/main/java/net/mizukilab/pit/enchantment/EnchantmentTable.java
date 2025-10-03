package net.mizukilab.pit.enchantment;

import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.util.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
public class EnchantmentTable{
    EnchantmentFactor factor;
    Map<EnchantmentRarity, ObjectArrayList<AbstractEnchantment>> enchantments;
    public EnchantmentTable(EnchantmentFactor factor) {
        this.factor = factor;
        reload();
    }
    public void reload(){
        factor.getEnchantments().forEach(i -> {
            enchantments.computeIfAbsent(i.getRarity(),b -> new ObjectArrayList<>()).add(i);
        });
    }
    public void asyncTick(){

    }

    public void enchantItem(IMythicItem item,boolean hasBook){
    }
    @Getter
    @AllArgsConstructor
    enum Tier{
        TIER_1(1,
                (mythicItem) -> {

        },
                (mythicItem) -> {

        }),
        TIER_2(2,
                (mythicItem) -> {

        },
                (mythicItem) -> {

        }),
        TIER_3(3,
                (mythicItem) -> {

        },
                (mythicItem) -> {
        }),;
        final int level;
        @Setter
        Consumer<IMythicItem> normal;
        @Setter
        Consumer<IMythicItem> useBook;
        public static Tier ofTier(IMythicItem item){
            int tier = item.getTier();
            Tier[] values = values();
            return values[tier - 1];
        }


        public void doEnchant(IMythicItem item, PlayerProfile profile){
            String enchantingBook = profile.getEnchantingBook();
            if(item.boostedByBook || enchantingBook == null){
                normal.accept(item);
                return;
            }
            profile.setEnchantingBook(null);
            useBook.accept(item);
        }
    }
}
