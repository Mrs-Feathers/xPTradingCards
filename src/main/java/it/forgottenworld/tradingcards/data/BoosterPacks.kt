package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.model.BoosterPack

object BoosterPacks : Map<String, BoosterPack> {

    private var map = mutableMapOf<String, BoosterPack>()

    fun load() {
        with(Config.PLUGIN) {
            map = getConfigurationSection("BoosterPacks")?.getKeys(false)?.map {
                Pair(it, BoosterPack(
                        it,
                        getInt("BoosterPacks.$it.NumNormalCards"),
                        Rarities[getString("BoosterPacks.$it.NormalCardRarity")]!!,
                        getInt("BoosterPacks.$it.NumSpecialCards"),
                        Rarities[getString("BoosterPacks.$it.SpecialCardRarity")]!!,
                        getInt("BoosterPacks.$it.NumExtraCards"),
                        Rarities[getString("BoosterPacks.$it.ExtraCardRarity")],
                        getDouble("BoosterPacks.$it.Price", 0.0),
                ))
            }?.toMap()?.toMutableMap() ?: mutableMapOf()
        }
    }

    override val entries: Set<Map.Entry<String, BoosterPack>>
        get() = map.entries
    override val keys: Set<String>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: Collection<BoosterPack>
        get() = map.values

    override fun containsKey(key: String) = map.containsKey(key)

    override fun containsValue(value: BoosterPack) = map.containsValue(value)

    override fun get(key: String) = map[key]

    override fun isEmpty() = map.isEmpty()

}