package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.model.Rarity

object Rarities : Map<String, Rarity> {

    private var map = mapOf<String, Rarity>()

    fun load() {
        with(Config.PLUGIN) {
            map = getConfigurationSection("Rarities")?.getKeys(false)?.map {
                Pair(it, Rarity(
                        it,
                        getString("Rarities.$it.Colour") ?: "&7",
                        Chances.getRarityChances(it),
                        mutableMapOf()
                ).apply { putAll(getCardsForRarityFromConfig(this)) })
            }?.toMap() ?: mapOf()
        }
    }

    private fun getCardsForRarityFromConfig(rarity: Rarity) =
            Config.CARDS.run {
                getConfigurationSection("Cards.${rarity.name}")!!.getKeys(false).map {
                    Pair(
                            it,
                            Card(
                                    it,
                                    rarity,
                                    getBoolean("Cards.${rarity.name}.$it.Has-Shiny-Version", false),
                                    getString("Cards.${rarity.name}.$it.Series")!!,
                                    getString("Cards.${rarity.name}.$it.About", "None")!!,
                                    getString("Cards.${rarity.name}.$it.Type")!!,
                                    getString("Cards.${rarity.name}.$it.Info")!!,
                                    Config.CARDS.getDouble("Cards.${rarity.name}.$it.Buy-Price", 0.0)
                            )
                    )
                }.toMap()
            }

    override val entries: Set<Map.Entry<String, Rarity>>
        get() = map.entries
    override val keys: Set<String>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: Collection<Rarity>
        get() = map.values

    override fun containsKey(key: String) = map.containsKey(key)

    override fun containsValue(value: Rarity) = map.containsValue(value)

    override fun get(key: String) = map[key]

    override fun isEmpty() = map.isEmpty()
}