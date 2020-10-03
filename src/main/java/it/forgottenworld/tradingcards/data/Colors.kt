package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config

object Colors {

    lateinit var Series: String
    lateinit var Type: String
    lateinit var Info: String
    lateinit var About: String
    lateinit var Rarity: String
    lateinit var BoosterPackName: String
    lateinit var BoosterPackLore: String
    lateinit var BoosterPackNormalCards: String
    lateinit var BoosterPackSpecialCards: String
    lateinit var BoosterPackExtraCards: String

    fun load() {
        with(Config.PLUGIN) {
            Series = getString("Colours.Series") ?: "&a"
            Type = getString("Colours.Type") ?: "&b"
            Info = getString("Colours.Info") ?: "&e"
            About = getString("Colours.About") ?: "&c"
            Rarity = getString("Colours.Rarity") ?: "&6"
            BoosterPackName = getString("Colours.BoosterPackName") ?: "&a"
            BoosterPackLore = getString("Colours.BoosterPackLore") ?: "&7"
            BoosterPackNormalCards = getString("Colours.BoosterPackNormalCards") ?: "&e"
            BoosterPackSpecialCards = getString("Colours.BoosterPackSpecialCards") ?: "&6"
            BoosterPackExtraCards = getString("Colours.BoosterPackExtraCards") ?: "&9"
        }
    }
}