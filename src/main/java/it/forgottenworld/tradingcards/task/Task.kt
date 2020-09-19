package it.forgottenworld.tradingcards.task

import it.forgottenworld.tradingcards.TradingCards
import org.bukkit.Bukkit
import org.bukkit.GameMode
import java.util.*

class Task(val tradingCards: TradingCards) {

    var taskid : Int = -1

    fun startTimer() {
        val config = tradingCards.configManager.pluginConfig.config!!
        val cardsConfig = tradingCards.configManager.cardsConfig.config!!
        val messagesConfig = tradingCards.configManager.messagesConfig.config!!
        val scheduler = Bukkit.getServer().scheduler
        if (scheduler.isQueued(taskid) || scheduler.isCurrentlyRunning(taskid)) {
            scheduler.cancelTask(taskid)
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Successfully cancelled task $taskid")
        }
        val hours = if (config.getInt("General.Schedule-Card-Time-In-Hours") < 1) 1 else config.getInt("General.Schedule-Card-Time-In-Hours")
        val tmessage: String = messagesConfig.getString("Messages.TimerMessage")!!.replace("%hour%".toRegex(), hours.toString())
        Bukkit.broadcastMessage(tradingCards.utils.cMsg(messagesConfig.getString("Messages.Prefix") + " " + tmessage))
        taskid = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(tradingCards, {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Task running..")
            if (config.getBoolean("General.Schedule-Cards")) {
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Schedule cards is true.")
                val rarities = cardsConfig.getConfigurationSection("Cards")!!
                val rarityKeys = rarities.getKeys(false)
                var keyToUse = ""
                for (key in rarityKeys) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Rarity key: $key")
                    if (key.equals(config.getString("General.Schedule-Card-Rarity"), ignoreCase = true)) {
                        keyToUse = key
                    }
                }
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] keyToUse: $keyToUse")
                if (keyToUse != "") {
                    Bukkit.broadcastMessage(tradingCards.utils.cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ScheduledGiveaway")))
                    for (p in Bukkit.getOnlinePlayers()) {
                        val cards = cardsConfig.getConfigurationSection("Cards.$keyToUse")!!
                        val cardKeys = cards.getKeys(false)
                        val rIndex = Random().nextInt(cardKeys.size)
                        var i = 0
                        var cardName = ""
                        for (theCardName in cardKeys) {
                            if (i == rIndex) {
                                cardName = theCardName
                                break
                            }
                            i++
                        }
                        if (p.inventory.firstEmpty() != -1) {
                            p.inventory.addItem(tradingCards.cardManager.createPlayerCard(cardName, keyToUse, 1, false))
                        } else {
                            val curWorld = p.world
                            if (p.gameMode == GameMode.SURVIVAL) {
                                curWorld.dropItem(p.location, tradingCards.cardManager.createPlayerCard(cardName, keyToUse, 1, false))
                            }
                        }
                    }
                }
            }
        }, hours * 20 * 60 * 60.toLong(), hours * 20 * 60 * 60.toLong())
    }
}