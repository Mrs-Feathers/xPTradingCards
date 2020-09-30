package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.model.Deck
import java.util.*

object Decks : MutableMap<UUID, MutableList<Deck>> {

    private val map = mutableMapOf<UUID, MutableList<Deck>>()

    fun load() {
        Config.DECKS.getConfigurationSection("Decks.Inventories")?.getKeys(false)?.map {
            val uuid = UUID.fromString(it)
            Pair(
                    uuid,
                    Config.DECKS.getConfigurationSection("Decks.Inventories.$it")!!
                            .getKeys(false)
                            .map { n -> Deck.deserialize(
                                    uuid,
                                    Config.DECKS.getStringList("Decks.Inventories.$it.$n")) }
            )
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<UUID, MutableList<Deck>>>
        get() = map.entries
    override val keys: MutableSet<UUID>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: MutableCollection<MutableList<Deck>>
        get() = map.values

    override fun containsKey(key: UUID) = map.containsKey(key)
    override fun containsValue(value: MutableList<Deck>) = map.containsValue(value)
    override fun get(key: UUID) = map[key]
    override fun isEmpty() = map.isEmpty()
    override fun clear() = map.clear()
    override fun put(key: UUID, value: MutableList<Deck>) = map.put(key, value)
    override fun putAll(from: Map<out UUID, MutableList<Deck>>) = map.putAll(from)
    override fun remove(key: UUID) = map.remove(key)
}