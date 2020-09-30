package it.forgottenworld.tradingcards.task

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.printDebug
import org.bukkit.Bukkit
import org.bukkit.GameMode

class Task {

    private var taskid : Int = -1

    fun startTimer() {

        val scheduler = Bukkit.getServer().scheduler
        if (scheduler.isQueued(taskid) || scheduler.isCurrentlyRunning(taskid))
            scheduler.cancelTask(taskid)

        val hours = General.ScheduleCardTimeInHours.coerceAtLeast(1)
        Bukkit.broadcastMessage(cMsg("${Messages.Prefix} ${Messages.TimerMessage.replaceFirst("%hour%", hours.toString())}"))

        val interval = hours * 20 * 60 * 60L

        taskid = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(TradingCards.instance, {

            printDebug("[Cards] Task running..")

            if (General.ScheduleCards) {

                val rarity = Rarities[General.ScheduleCardRarity]

                if (rarity != null) {

                    Bukkit.broadcastMessage(cMsg("${Messages.Prefix} ${Messages.ScheduledGiveaway}"))

                    for (p in Bukkit.getOnlinePlayers()) {

                        if (p.inventory.firstEmpty() != -1)
                            p.inventory.addItem(CardManager.getCardItemStack(rarity.cards.values.random(), 1))
                        else if (p.gameMode == GameMode.SURVIVAL)
                            p.world.dropItem(p.location, CardManager.getCardItemStack(rarity.cards.values.random(), 1))
                    }
                }
            }
        }, interval, interval)
    }
}