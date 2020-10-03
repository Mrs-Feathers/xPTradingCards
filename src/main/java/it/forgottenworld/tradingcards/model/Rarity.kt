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
        private val cards: MutableMap<String, Card>) : MutableMap<String, Card> {

    companion object {

        fun calculate(e: EntityType, alwaysDrop: Boolean): Rarity? {

            val chanceToDrop = Random.nextInt(100) + 1

            val rarChances = Rarities.values.map { v ->
                Pair(
                        v,
                        when (getMobType(e) ?: return null) {
                            MobType.HOSTILE -> {
                                if (!alwaysDrop && chanceToDrop > Chances.HostileChance) return null
                                v.chance.hostile
                            }
                            MobType.NEUTRAL -> {
                                if (!alwaysDrop && chanceToDrop > Chances.NeutralChance) return null
                                v.chance.neutral
                            }
                            MobType.PASSIVE -> {
                                if (!alwaysDrop && chanceToDrop > Chances.PassiveChance) return null
                                v.chance.passive
                            }
                            MobType.BOSS -> {
                                if (!alwaysDrop && chanceToDrop > Chances.BossChance) return null
                                v.chance.boss
                            }
                        }
                )
            }.toMap()

            val rng = Random.nextInt(rarChances.values.sum() + 1)

            rarChances
                    .entries
                    .fold(0) { acc, (k, v) -> acc.plus(v).also { if (rng < it) return k } }

            return null
        }
    }

    override val size: Int
        get() = cards.size

    override fun containsKey(key: String) = cards.containsKey(key)
    override fun containsValue(value: Card) = cards.containsValue(value)
    override fun get(key: String) = cards[key]
    override fun isEmpty() = cards.isEmpty()

    override val entries
        get() = cards.entries
    override val keys: MutableSet<String>
        get() = cards.keys
    override val values: MutableCollection<Card>
        get() = cards.values

    override fun clear() = cards.clear()
    override fun put(key: String, value: Card) = cards.put(key, value)
    override fun putAll(from: Map<out String, Card>) = cards.putAll(from)
    override fun remove(key: String) = cards.remove(key)
}