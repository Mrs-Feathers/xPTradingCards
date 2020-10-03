package it.forgottenworld.tradingcards.task

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.tC
import org.bukkit.Bukkit
import org.bukkit.GameMode

class Task {

    private var taskid: Int = -1

    fun startTimer() {

        val scheduler = Bukkit.getServer().scheduler
        if (scheduler.isQueued(taskid) || scheduler.isCurrentlyRunning(taskid))
            scheduler.cancelTask(taskid)

        val hours = General.ScheduleCardTimeInHours.coerceAtLeast(1)
        Bukkit.broadcastMessage(tC("${Messages.Prefix} ${Messages.TimerMessage.replaceFirst("%hour%", hours.toString())}"))

        val interval = hours * 20 * 60 * 60L

        taskid = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(TradingCards.instance, {

            if (General.ScheduleCards) {

                val rarity = Rarities[General.ScheduleCardRarity]

                if (rarity != null) {

                    Bukkit.broadcastMessage(tC("${Messages.Prefix} ${Messages.ScheduledGiveaway}"))

                    for (p in Bukkit.getOnlinePlayers()) {

                        if (p.inventory.firstEmpty() != -1)
                            p.inventory.addItem(CardManager.createCardItemStack(rarity.values.random(), 1))
                        else if (p.gameMode == GameMode.SURVIVAL)
                            p.world.dropItem(p.location, CardManager.createCardItemStack(rarity.values.random(), 1))
                    }
                }
            }
        }, interval, interval)
    }
}