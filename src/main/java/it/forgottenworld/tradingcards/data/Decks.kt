package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.model.Deck
import java.util.*

object Decks : MutableMap<UUID, MutableMap<Int, Deck>> {

    private val map = mutableMapOf<UUID, MutableMap<Int, Deck>>()

    fun load() {
        map.clear()
        map.putAll(Config.DECKS.getConfigurationSection("Decks.Inventories")?.getKeys(false)?.map {
            val uuid = UUID.fromString(it)
            Pair(
                    uuid,
                    Config.DECKS.getConfigurationSection("Decks.Inventories.$it")
                            ?.getKeys(false)
                            ?.map { n ->
                                Pair(n.toInt(), Deck.deserialize(Config.DECKS.getStringList("Decks.Inventories.$it.$n")))
                            }
                            ?.toMap()
                            ?.toMutableMap() ?: mutableMapOf()
            )
        }?.toMap() ?: return)
    }

    override val entries: MutableSet<MutableMap.MutableEntry<UUID, MutableMap<Int, Deck>>>
        get() = map.entries
    override val keys: MutableSet<UUID>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: MutableCollection<MutableMap<Int, Deck>>
        get() = map.values

    override fun containsKey(key: UUID) = map.containsKey(key)
    override fun containsValue(value: MutableMap<Int, Deck>) = map.containsValue(value)
    override fun get(key: UUID) = map[key]
    override fun isEmpty() = map.isEmpty()
    override fun clear() = map.clear()
    override fun put(key: UUID, value: MutableMap<Int, Deck>) = map.put(key, value)
    override fun putAll(from: Map<out UUID, MutableMap<Int, Deck>>) = map.putAll(from)
    override fun remove(key: UUID) = map.remove(key)
}