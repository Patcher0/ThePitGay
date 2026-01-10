package net.mizukilab.pit.enchantment;

import lombok.val;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.jetbrains.annotations.Nullable;

public class UnknownEnchantment extends AbstractEnchantment {

    private final String nbtName;

    public UnknownEnchantment(String nbtName) {
        this.nbtName = nbtName;
    }

    @Override
    public String getEnchantName() {
        val string = "&c" + this.nbtName.substring(0, 1).toUpperCase() + this.nbtName.substring(1);
        return string.replace("_", " ");
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.SPECIAL;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return this.nbtName;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7未知附魔. 通常因为管理员更改了某个已有附魔的nbt, 导致该附魔没有被正常加载. /s" +
                "&7这个附魔原有的nbt是: &f" + this.nbtName + "/s" +
                "当管理员配置了带有原有nbt名称的附魔时, 这个附魔会重新被加载.";
    }
}