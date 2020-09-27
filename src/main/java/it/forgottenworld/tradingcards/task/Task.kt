package it.forgottenworld.tradingcards.task

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.printDebug
import org.bukkit.Bukkit
import org.bukkit.GameMode

class Task {

    private var taskid : Int = -1

    fun startTimer() {

        val config = Config.PLUGIN
        val cardsConfig = Config.CARDS

        val scheduler = Bukkit.getServer().scheduler
        if (scheduler.isQueued(taskid) || scheduler.isCurrentlyRunning(taskid)) {
            scheduler.cancelTask(taskid)
            printDebug("[Cards] Successfully cancelled task $taskid")
        }

        val hours = config.getInt("General.Schedule-Card-Time-In-Hours").coerceAtLeast(1)

        Bukkit.broadcastMessage(cMsg("${Messages.Prefix} ${Messages.TimerMessage.replaceFirst("%hour%", hours.toString())}"))

        taskid = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(TradingCards.instance, {

            printDebug("[Cards] Task running..")

            if (config.getBoolean("General.Schedule-Cards")) {

                printDebug("[Cards] Schedule cards is true.")

                val rarities = cardsConfig.getConfigurationSection("Cards")!!
                val rarityKeys = rarities.getKeys(false)
                val keyToUse = rarityKeys.find {
                    it.equals(config.getString("General.Schedule-Card-Rarity"), ignoreCase = true)
                } ?: ""

                printDebug("[Cards] keyToUse: $keyToUse")

                if (keyToUse.isEmpty()) {

                    Bukkit.broadcastMessage(cMsg("${Messages.Prefix} ${Messages.ScheduledGiveaway}"))

                    for (p in Bukkit.getOnlinePlayers()) {

                        val cardKeys = cardsConfig.getConfigurationSection("Cards.$keyToUse")?.getKeys(false)?.toList()
                        val r = cardKeys?.random() ?: continue

                        if (p.inventory.firstEmpty() != -1)
                            p.inventory.addItem(
                                    CardManager.createPlayerCard(r, keyToUse, 1, false)
                            )
                        else if (p.gameMode == GameMode.SURVIVAL)
                            p.world.dropItem(
                                    p.location,
                                    CardManager.createPlayerCard(r, keyToUse, 1, false)
                            )
                    }
                }
            }
        }, hours * 20 * 60 * 60.toLong(), hours * 20 * 60 * 60.toLong())
    }
}