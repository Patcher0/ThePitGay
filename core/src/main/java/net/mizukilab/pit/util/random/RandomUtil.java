package net.mizukilab.pit.util.random;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import kotlin.jvm.functions.Function2;
import net.mizukilab.pit.util.Einstein;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * 2 * @Author: EmptyIrony
 * 3 * @Date: 2020/12/28 23:47
 * 4
 */
public class RandomUtil {

    public static final SecureRandom random;

    static {
        random = new SecureRandom();
    }

    public static String randomStr() {
        String s = "ABCDEFGHIJKLMNPQRSTUVXYZ1234567890";
        char[] c = s.toCharArray();
        StringBuilder numbers = new StringBuilder();

        for (int i = 0; i < 2; ++i) {
            numbers.append(c[random.nextInt(c.length)]);
        }
        return numbers.toString();
    }

    public static String forRandomScoreboardString() {
        Date from = Date.from(Instant.now());
        int year = from.getYear();
        int mon = from.getMonth();
        return year + "" + mon + randomStr();
    }

    /**
     * 参数范围为0-1
     *
     * @param chance 百分数概率，大于等于1永远返回true
     * @return 是否成功
     */
    public static boolean hasSuccessfullyByChance(double chance) {
        if (chance >= 1) {
            return true;
        }
        if (chance <= 0) {
            return false;
        }


        return random.nextDouble() < chance;
    }
    public static int rand(int max){
        return random.nextInt(max);
    }
    public static int rand(int max,int offset){
        if(max == offset){
            return max;
        }
        return random.nextInt(max - offset) + offset;
    }

    public static <T> T helpMeToChooseOne(T... entry) {
        switchSeed();
        return entry[random.nextInt(entry.length)];
    }
    public static int helpMeToChooseOneInt(int... entry) {
        switchSeed();
        return entry[random.nextInt(entry.length)];
    }
    public static <T> void chooseAndApply(boolean unique,List<T> ench, Object2IntMap<T> t,int level) {
        int size = ench.size();
        int i1 = random.nextInt(size);
        while (true) {
            T t1 = ench.get(i1);
            if (unique && t.containsKey(t1)) {

                i1 = (i1 + 1) % size;
                continue;
            }
            t.put(t1, level);
            break;
        }
    }
    public static <T> int chooseAndApplyMultipleType(boolean unique, Object2IntMap<T> t,int level,List<T>... ench) {
        int i = random.nextInt(ench.length);
        List<T> ench1 = ench[i];
        int i1 = random.nextInt(ench1.size());
        while (true) {
            T t1 = ench1.get(i1);

            if (unique && t.containsKey(t1)) {
                i1 = (i1 + 1) % ench1.size();
                continue;
            }
            t.put(t1, level);
            return i;
        }
    }

    public static <T> T chooseAndApplyMultipleTypeRN(double chance,boolean unique, Object2IntMap<T> t,int level,List<T> normal,List<T> rare) {
        List<T> ench1 = normal;
        if(hasSuccessfullyByChance(chance)) {
            ench1 = rare;
        }
        int i1 = random.nextInt(ench1.size());
        while (true) {
            T t1 = ench1.get(i1);

            if (unique && t.containsKey(t1)) {
                i1 = (i1 + 1) % ench1.size();
                continue;
            }
            t.put(t1, level);
            return t1;
        }
    }
    public static <T> T randEnch(double rageChance,List<T> rare,List<T> normal){
        List<T> random = normal;
        if(hasSuccessfullyByChance(rageChance)){
            random = rare;
        }
        return random.get(RandomUtil.random.nextInt(random.size()));
    }
    public static <T> List<T> randEnchMultipleApply(int maxL2, List<T> normal, Object2IntMap<T> tM, Function2<Integer,T,Boolean> predicate){
        maxL2 = rand(maxL2,1);
        List<T> a = new LinkedList<>();
        for (int z = 0; z < maxL2; z++) {
            int index = RandomUtil.random.nextInt(normal.size());
            T t;
            while(true) {
                t = normal.get(index);
                int anInt = tM.getOrDefault(t,0);
                if (!predicate.invoke(anInt,t)) {
                    index = (index + 1) % normal.size();
                    continue;
                }
                break;
            }
            a.add(t);
            tM.compute(t,(i,val) -> val == null ? 1 : val + 1);
        }
        return a;
    }
    public static <T> List<T> randEnchMultipleApplyRN(int maxEnch,double chance,int min,int maxL2,Object2IntMap<T> tM,List<T> normalInput,List<T> rare,Function2<Integer,T,Boolean> predicate) {
        maxL2 = rand(maxL2, min);
        List<T> a = new LinkedList<>();
        List<T> normal;
        routine1:
        for (int z = 0; z < maxL2; z++) {
            if (hasSuccessfullyByChance(chance)) {
                normal = rare;
            } else {
                normal = normalInput;
            }

            if (tM.size() >= maxEnch) {
                var entries = tM.object2IntEntrySet();
                var iterator = entries.iterator();

                while (iterator.hasNext()) {
                    Map.Entry<T, Integer> next = iterator.next();
                    if (!predicate.invoke(next.getValue(), next.getKey())) {
                        continue;
                    }
                    next.setValue(next.getValue() + 1);
                    a.add(next.getKey());
                    continue routine1;
                }
                System.out.println("Ignoring, 333 enchant");
            } else {
                int index = RandomUtil.random.nextInt(normal.size());
                T t;
                while (true) {
                    t = normal.get(index);
                    int anInt = tM.getOrDefault(t, 0);
                    if (!predicate.invoke(anInt + 1, t)) {
                        index = (index + 1) % normal.size();
                        continue;
                    }
                    break;
                }
                tM.compute(t, (i, val) -> val == null ? 1 : val + 1);
                a.add(t);
            }
        }
        return a;
    }
    public static <T> T randEnchs(List<T>... enchantments){
        List<T> enchantment = enchantments[random.nextInt(enchantments.length)];
        return enchantment.get(random.nextInt(enchantment.size()));
    }
    public static Object helpMeToChooseOne(Set entry) {
        return helpMeToChooseOne(entry.toArray());
    }
    public static Object helpMeToChooseOne(List entry) {
        switchSeed();
        return entry.get(random.nextInt(entry.size()));
    }

    public static void switchSeed() {
        //no-op
    }

    public static Location generateRandomLocation() {
        int x = RandomUtil.random.nextInt(180) - 90;
        int z = RandomUtil.random.nextInt(180) - 90;
        World world = Bukkit.getWorlds().get(0);
        return world.getHighestBlockAt(x, z).getLocation().clone().add(0, 1, 0);
    }
}
