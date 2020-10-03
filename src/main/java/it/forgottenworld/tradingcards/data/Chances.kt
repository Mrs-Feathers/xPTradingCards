package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.model.Chance

object Chances {

    var HostileChance = 20
    var NeutralChance = 5
    var PassiveChance = 1
    var BossChance = 100
    var BossDrop = false
    lateinit var BossDropRarity: String
    var ShinyVersionChance = 1

    fun load() {
        with(Config.PLUGIN) {
            HostileChance = getInt("Chances.Hostile-Chance", 20)
            NeutralChance = getInt("Chances.Neutral-Chance", 5)
            PassiveChance = getInt("Chances.Passive-Chance", 1)
            BossChance = getInt("Chances.Boss-Chance", 100)
            BossDrop = getBoolean("Chances.Boss-Drop", false)
            BossDropRarity = getString("Chances.Boss-Drop-Rarity") ?: "None"
            ShinyVersionChance = getInt("Chances.Shiny-Version-Chance", 1)
        }
    }

    fun getRarityChances(rarity: String) =
            Config.PLUGIN.run {
                Chance(
                        getInt("Chances.$rarity.Hostile", 0),
                        getInt("Chances.$rarity.Neutral", 0),
                        getInt("Chances.$rarity.Passive", 0),
                        getInt("Chances.$rarity.Boss", 0),
                )
            }
}