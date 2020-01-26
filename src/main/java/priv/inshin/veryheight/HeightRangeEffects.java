/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package priv.inshin.veryheight;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 效果高度范围
 */
public class HeightRangeEffects {
    /**
     * 为每个玩家单独计算 tick 周期
     */
    public final Map<UUID, Long> ticks = new ConcurrentHashMap<>();
    /**
     * BUFF 范围最低高度
     */
    public final int minHeight;
    /**
     * BUFF 范围最高高度
     */
    public final int maxHeight;
    /**
     * BUFF
     */
    public final List<Effect> effects;
    /**
     * BUFF 周期
     */
    public final int tick;

    public HeightRangeEffects(int minHeight, int maxHeight, @NotNull List<String> effects, @Range(from = 0, to = Integer.MAX_VALUE) int tick) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.effects = new ArrayList<>(effects.size());
        this.tick = tick;

        for (Object object : effects) { // <effect_name>:<amplifier>:<duration>
            object = ((String) object).split(":", 3);
            PotionEffectType effect = PotionEffectType.getByName(((String[]) object)[0]);
            if (effect == null) { // 无效的 BUFF 名
                continue;
            }
            this.effects.add(new Effect(effect, ((String[]) object)[2], ((String[]) object)[1]));
        }
    }

    /**
     * 根据玩家 Y 坐标创建相应 BUFF
     *
     * @param entity 玩家
     * @return 所有 BUFF
     */
    public Collection<PotionEffect> effects(@NotNull LivingEntity entity) {
        if (this.effects != null && !this.effects.isEmpty()) { // 确认有合法的 BUFF
            int height = (int) entity.getLocation().getY(); // 玩家当前的高度
            if (height >= this.minHeight && height <= this.maxHeight) { // 高度范围内
                long tk = this.ticks.computeIfAbsent(entity.getUniqueId(), k -> 0L);
                if (tk % this.tick == 0) { // 玩家 tick 周期
                    // 即将给玩家的 BUFF 列表
                    Collection<PotionEffect> effects = new ArrayList<>();
                    for (Effect effect : this.effects) {
                        effects.add(effect.effect(entity));
                    }
                    return effects;
                }
            } else { // 不在高度范围内
                // 移除玩家 tick 周期
                if (!this.ticks.isEmpty()) {
                    this.ticks.remove(entity.getUniqueId());
                }
            }
        }
        return Collections.emptyList();
    }

    public static class Effect {
        /**
         * BUFF 类型
         */
        public final PotionEffectType type;
        /**
         * BUFF 持续时间
         */
        public final String duration;
        /**
         * BUFF 等级
         */
        public final String amplifier;

        public Effect(@NotNull PotionEffectType type, @NotNull String duration, @NotNull String amplifier) {
            this.type = type;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        /**
         * 根据 + 和 - 判断是否进行相对计算
         *
         * @param number 数字字符串
         * @return 是否进行相对计算
         */
        private boolean isRelative(@NotNull String number) {
            return number.charAt(0) == '+' || number.charAt(0) == '-';
        }

        /**
         * 根据相对时间创建 BUFF
         *
         * @param entity 玩家
         * @return BUFF
         */
        public PotionEffect effect(@NotNull LivingEntity entity) {
            PotionEffect effect = entity.getPotionEffect(this.type);
            int duration = Integer.parseInt(this.duration);
            int amplifier = Integer.parseInt(this.amplifier);

            if (effect == null) { // 没有当前 BUFF
                // 持续时间大于或等于 0
                duration = isRelative(this.duration) ? Math.max(duration, 0) : duration;
                // 等级大于或等于 0
                amplifier = isRelative(this.amplifier) ? Math.max(amplifier, 0) : amplifier;
                return new PotionEffect(this.type, duration + 1, amplifier);
            } else { // 已有当前BUFF
                if (isRelative(this.amplifier)) { // BUFF相对等级
                    return new PotionEffect(this.type, effect.getDuration() + duration, effect.getAmplifier() + amplifier);
                } else { // BUFF绝对等级
                    return new PotionEffect(this.type, duration, amplifier);
                }
            }
        }
    }
}