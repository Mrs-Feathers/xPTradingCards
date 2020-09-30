package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.data.Chances
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.MobType
import it.forgottenworld.tradingcards.util.getMobType
import org.bukkit.entity.EntityType
import kotlin.random.Random

class Rarity(
        val name: String,
        val color: String,
        val chance: Chance,
        val cards: MutableMap<String, Card>) {

    companion object {

        fun calculate(e: EntityType, alwaysDrop: Boolean): Rarity? {

            val drop = Random.nextInt(100) + 1

            val rarChances = Rarities.values.map { v ->
                Pair(
                        v,
                        when(getMobType(e) ?: return null) {
                            MobType.HOSTILE -> {
                                if (!alwaysDrop && drop > Chances.HostileChance) return null
                                v.chance.hostile
                            }
                            MobType.NEUTRAL -> {
                                if (!alwaysDrop && drop > Chances.NeutralChance) return null
                                v.chance.neutral
                            }
                            MobType.PASSIVE -> {
                                if (!alwaysDrop && drop > Chances.PassiveChance) return null
                                v.chance.passive
                            }
                            MobType.BOSS -> {
                                if (!alwaysDrop && drop > Chances.BossChance) return null
                                v.chance.boss
                            }
                        }
                )
            }.toMap()

            val rng = Random.nextInt(rarChances.values.sum() + 1)

            var acc = 0
            for ((k,v) in rarChances) {
                acc += v
                if (rng < acc)
                    return k
            }

            return null
        }
    }
}