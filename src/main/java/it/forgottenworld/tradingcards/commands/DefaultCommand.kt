package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.TradingCards.Companion.configManager
import it.forgottenworld.tradingcards.util.Utils.Companion.addToList
import it.forgottenworld.tradingcards.util.Utils.Companion.blacklistMode
import it.forgottenworld.tradingcards.util.Utils.Companion.cMsg
import it.forgottenworld.tradingcards.util.Utils.Companion.calculateRarity
import it.forgottenworld.tradingcards.util.Utils.Companion.formatTitle
import it.forgottenworld.tradingcards.util.Utils.Companion.isOnList
import it.forgottenworld.tradingcards.util.Utils.Companion.removeFromList
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

class DefaultCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        val configManager = configManager
        val cardManager = TradingCards.cardManager
        val deckManager = TradingCards.deckManager
        val messagesConfig = configManager.messagesConfig.config!!
        val cardsConfig = configManager.cardsConfig.config!!
        val config = configManager.pluginConfig.config!!

        if (cmd.name.equals("fwtc", ignoreCase = true)) {
            if (args.isNotEmpty()) {
                if (args[0].equals("reload", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.reload")) {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.Reload")))
                        configManager.reloadAllConfigs()
                        if (config.getBoolean("General.Schedule-Cards")) TradingCards.instance.task.startTimer()
                        return true
                    }
                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                } else if (args[0].equals("toggle", ignoreCase = true)) {
                    val p = sender as Player
                    if (isOnList(p) && blacklistMode() == 'b') {
                        removeFromList(p)
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ToggleEnabled")))
                    } else if (isOnList(p) && blacklistMode() == 'w') {
                        removeFromList(p)
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ToggleDisabled")))
                    } else if (!isOnList(p) && blacklistMode() == 'b') {
                        addToList(p)
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ToggleDisabled")))
                    } else if (!isOnList(p) && blacklistMode() == 'w') {
                        addToList(p)
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ToggleEnabled")))
                    }
                } else if (args[0].equals("create", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.create")) {
                        val p = sender as Player
                        if (args.size < 8) {
                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.CreateUsage")))
                        } else {
                            val isShiny = args[5].equals("true", ignoreCase = true) || args[5].equals("yes", ignoreCase = true) || args[5].equals("y", ignoreCase = true)
                            cardManager.createCard(p, args[1].replace("_".toRegex(), " "), args[2], args[3].replace("_".toRegex(), " "), args[4].replace("_".toRegex(), " "), isShiny, args[6].replace("_".toRegex(), " "))
                        }
                    }
                } else if (args[0].equals("givecard", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.givecard")) {
                        if (args.size > 2) {
                            val p = sender as Player
                            if (cardsConfig.contains("Cards." + args[1].replace("_".toRegex(), " ") + "." + args[2])) p.inventory.addItem(cardManager.getNormalCard(args[2], args[1].replace("_".toRegex(), " "), 1)) else {
                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoCard")))
                            }
                        } else {
                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveCardUsage")))
                        }
                    } else {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("giveshinycard", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.giveshinycard")) {
                        if (args.size > 2) {
                            val p = sender as Player
                            if (cardsConfig.contains("Cards." + args[1].replace("_".toRegex(), " ") + "." + args[2])) p.inventory.addItem(cardManager.createPlayerCard(args[2], args[1].replace("_".toRegex(), " "), 1, true)) else {
                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoCard")))
                            }
                        } else {
                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveCardUsage")))
                        }
                    } else {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("giveboosterpack", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.giveboosterpack")) {
                        if (args.size > 2) {
                            if (config.contains("BoosterPacks." + args[2].replace(" ".toRegex(), "_"))) {
                                if (Bukkit.getPlayer(args[1]) != null) {
                                    val p = Bukkit.getPlayer(args[1])
                                    if (p!!.inventory.firstEmpty() != -1) {
                                        p.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.BoosterPackMsg")))
                                        p.inventory.addItem(deckManager.createBoosterPack(args[2]))
                                    } else {
                                        val curWorld = p.world
                                        if (p.gameMode == GameMode.SURVIVAL) {
                                            p.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.BoosterPackMsg")))
                                            curWorld.dropItem(p.location, deckManager.createBoosterPack(args[2]))
                                        }
                                    }
                                } else {
                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPlayer")))
                                }
                            } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoBoosterPack")))
                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveBoosterPackUsage")))
                    } else {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("getdeck", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.getdeck")) {
                        if (args.size > 1) {
                            if (StringUtils.isNumeric(args[1])) {
                                if (sender.hasPermission("fwtc.decks." + args[1])) {
                                    val p = sender as Player
                                    if (!deckManager.hasDeck(p, args[1].toInt())) {
                                        if (p.inventory.firstEmpty() != -1) {
                                            p.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveDeck")))
                                            p.inventory.addItem(deckManager.createDeck(p, args[1].toInt()))
                                        } else {
                                            val curWorld = p.world
                                            if (p.gameMode == GameMode.SURVIVAL) {
                                                p.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveDeck")))
                                                curWorld.dropItem(p.location, deckManager.createDeck(p, args[1].toInt()))
                                            }
                                        }
                                    } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.AlreadyHaveDeck")))
                                } else {
                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.MaxDecks")))
                                }
                            } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GetDeckUsage")))
                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GetDeckUsage")))
                    } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                } else if (args[0].equals("giverandomcard", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.randomcard")) {
                        if (args.size > 2) {
                            if (Bukkit.getPlayer(args[2]) != null) {
                                val p = Bukkit.getPlayer(args[2])
                                try {
                                    EntityType.valueOf(args[1].toUpperCase())
                                    val rare = calculateRarity(EntityType.valueOf(args[1].toUpperCase()), true)
                                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] onCommand.rare: $rare")
                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveRandomCardMsg")!!.replace("%player%".toRegex(), p!!.name)))
                                    if (p.inventory.firstEmpty() != -1) {
                                        p.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveRandomCard")))
                                        if (cardManager.generateCard(rare) != null) p.inventory.addItem(cardManager.generateCard(rare))
                                    } else {
                                        val curWorld = p.world
                                        if (p.gameMode == GameMode.SURVIVAL) {
                                            p.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveRandomCard")))
                                            if (cardManager.generateCard(rare) != null) curWorld.dropItem(p.location, cardManager.generateCard(rare)!!)
                                        }
                                    }
                                } catch (e: IllegalArgumentException) {
                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoEntity")))
                                }
                            } else {
                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPlayer")))
                            }
                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveRandomCardUsage")))
                    } else {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("list", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.list")) {
                        val cards = cardsConfig.getConfigurationSection("Cards")!!
                        val cardKeys = cards.getKeys(false)
                        var msg = ""
                        var i = 0
                        var finalMsg = ""
                        for (key in cardKeys) {
                            val cardsWithKey = cardsConfig.getConfigurationSection("Cards.$key")!!
                            val keyKeys = cardsWithKey.getKeys(false)
                            for (key2 in keyKeys) {
                                if (i > 41) {
                                    finalMsg = "$msg&7and more!"
                                } else msg = msg + "&7" + key2.replace("_".toRegex(), " ") + "&f, "
                                i++
                            }
                            sender.sendMessage(cMsg("&6--- $key &7(&f$i&7)&6 ---"))
                            msg = StringUtils.removeEnd(msg, ", ")
                            if (finalMsg == "") sender.sendMessage(cMsg(msg)) else sender.sendMessage(cMsg(finalMsg))
                            msg = ""
                            finalMsg = ""
                            i = 0
                        }
                    } else {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    }
                } else {
                    var hasExtra: Boolean
                    if (args[0].equals("listpacks", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.listpacks")) {
                            val cards = config.getConfigurationSection("BoosterPacks")!!
                            val cardKeys = cards.getKeys(false)
                            var i = 0
                            sender.sendMessage(cMsg("&6--- Booster Packs ---"))
                            var hasPrice = false
                            hasExtra = false
                            for (key in cardKeys) {
                                if (config.contains("BoosterPacks.$key.Price")) hasPrice = true
                                if (config.contains("BoosterPacks.$key.ExtraCardRarity") && config.contains("BoosterPacks.$key.NumExtraCards")) hasExtra = true
                                i++
                                if (hasPrice) sender.sendMessage(cMsg("&6" + i + ") &e" + key + " &7(&aPrice: " + config.getDouble(StringBuilder("BoosterPacks.").append(key).append(".Price&7").toString()) + ")")) else sender.sendMessage(cMsg("&6$i) &e$key"))
                                if (hasExtra) sender.sendMessage(cMsg("  &7- &f&o" + config.getInt(StringBuilder("BoosterPacks.").append(key).append(".NumNormalCards").toString()) + " " + config.getString(StringBuilder("BoosterPacks.").append(key).append(".NormalCardRarity").toString()) + ", " + config.getInt(StringBuilder("BoosterPacks.").append(key).append(".NumExtraCards").toString()) + " " + config.getString(StringBuilder("BoosterPacks.").append(key).append(".ExtraCardRarity").toString()) + ", " + config.getInt(StringBuilder("BoosterPacks.").append(key).append(".NumSpecialCards").toString()) + " " + config.getString(StringBuilder("BoosterPacks.").append(key).append(".SpecialCardRarity").toString()))) else sender.sendMessage(cMsg("  &7- &f&o" + config.getInt(StringBuilder("BoosterPacks.").append(key).append(".NumNormalCards").toString()) + " " + config.getString(StringBuilder("BoosterPacks.").append(key).append(".NormalCardRarity").toString()) + ", " + config.getInt(StringBuilder("BoosterPacks.").append(key).append(".NumSpecialCards").toString()) + " " + config.getString(StringBuilder("BoosterPacks.").append(key).append(".SpecialCardRarity").toString())))
                                hasPrice = false
                                hasExtra = false
                            }
                        } else {
                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                        }
                    } else if (args[0].equals("giveaway", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.giveaway")) {
                            if (args.size > 1) {
                                val rarities = cardsConfig.getConfigurationSection("Cards")!!
                                val rarityKeys = rarities.getKeys(false)
                                var keyToUse = ""
                                for (key in rarityKeys) {
                                    if (key.equals(args[1].replace("_".toRegex(), " "), ignoreCase = true)) {
                                        keyToUse = key
                                    }
                                }
                                if (keyToUse != "") {
                                    Bukkit.broadcastMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.Giveaway")!!.replace("%player%".toRegex(), sender.name).replace("%rarity%".toRegex(), keyToUse)))
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
                                            p.inventory.addItem(cardManager.createPlayerCard(cardName, keyToUse, 1, false))
                                        } else {
                                            val curWorld = p.world
                                            if (p.gameMode == GameMode.SURVIVAL) {
                                                curWorld.dropItem(p.location, cardManager.createPlayerCard(cardName, keyToUse, 1, false))
                                            }
                                        }
                                    }
                                } else {
                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoRarity")))
                                }
                            } else {
                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.GiveawayUsage")))
                            }
                        } else {
                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                        }
                    } else if (args[0].equals("worth", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.worth")) {
                            if (TradingCards.instance.hasVault) {
                                val p = sender as Player
                                if (p.inventory.getItem(p.inventory.heldItemSlot)?.type == Material.valueOf(config.getString("General.Card-Material")!!)) {
                                    val itemInHand = p.inventory.getItem(p.inventory.heldItemSlot)
                                    val itemName = itemInHand!!.itemMeta!!.displayName
                                    if (config.getBoolean("General.Debug-Mode")) println(itemName)
                                    if (config.getBoolean("General.Debug-Mode")) println(ChatColor.stripColor(itemName))
                                    val splitName: Array<String> = ChatColor.stripColor(itemName)!!.split(" ").toTypedArray()
                                    val cardName = if (splitName.size > 1) {
                                        splitName[1]
                                    } else splitName[0]
                                    if (config.getBoolean("General.Debug-Mode")) println(cardName)
                                    val lore = itemInHand.itemMeta!!.lore
                                    val rarity = ChatColor.stripColor(lore!![3])
                                    if (config.getBoolean("General.Debug-Mode")) println(rarity)
                                    var canBuy = false
                                    var buyPrice = 0.0
                                    if (cardsConfig.contains("Cards.$rarity.$cardName.Buy-Price")) {
                                        buyPrice = cardsConfig.getDouble("Cards.$rarity.$cardName.Buy-Price")
                                        if (buyPrice > 0.0) canBuy = true
                                    }
                                    if (canBuy) {
                                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.CanBuy")!!.replace("%buyAmount%".toRegex(), buyPrice.toString())))
                                    } else {
                                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.CanNotBuy")))
                                    }
                                } else {
                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NotACard")))
                                }
                            } else {
                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoVault")))
                            }
                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    } else if (args[0].equals("credits", ignoreCase = true)) {
                        sender.sendMessage(cMsg(formatTitle("Credits and Special Thanks")))
                        sender.sendMessage(cMsg("&7[&aDeveloper&7] &aLukas Xenoyia Gentle"))
                        sender.sendMessage(cMsg("   &7- &6&oxPXenoyia&f, &6&oXenoyia&f, &6&oxPLukas&f, &6&oSnoopDogg&f"))
                        sender.sendMessage(cMsg("&7[&eSpecial Thanks&7] XpanD, IrChaos, xtechgamer735, PTsandro, FlyingSquidwolf, iXRaZoRXi, iToxy, TowelieDOH, Miku_Snow, NOBUTSS, doitliketyler, Celebrimbor90, Magz, GypsySix, bumbble, iFosadrink_2, Sunique, TheRealGSD, Zenko, Berkth, TubeCraftXXL, Cra2ytig3r, marcosds13, ericbarbwire, Bonzo"))
                    } else if (args[0].equals("buy", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.buy")) {
                            if (TradingCards.instance.hasVault) {
                                val p = sender as Player
                                if (args.size > 1) {
                                    if (args[1].equals("pack", ignoreCase = true)) {
                                        if (args.size > 2) {
                                            if (config.contains("BoosterPacks." + args[2])) {
                                                var buyPrice = 0.0
                                                var canBuy = false
                                                if (config.contains("BoosterPacks." + args[2] + ".Price")) {
                                                    buyPrice = config.getDouble("BoosterPacks." + args[2] + ".Price")
                                                    if (buyPrice > 0.0) canBuy = true
                                                }
                                                if (canBuy) {
                                                    if (TradingCards.econ!!.getBalance(p) >= buyPrice) {
                                                        if (config.getBoolean("PluginSupport.Vault.Closed-Economy")) {
                                                            TradingCards.econ!!.withdrawPlayer(p, buyPrice)
                                                            TradingCards.econ!!.depositPlayer(config.getString("PluginSupport.Vault.Server-Account"), buyPrice)
                                                        } else {
                                                            TradingCards.econ!!.withdrawPlayer(p, buyPrice)
                                                        }
                                                        if (p.inventory.firstEmpty() != -1) {
                                                            p.inventory.addItem(deckManager.createBoosterPack(args[2]))
                                                        } else {
                                                            val curWorld = p.world
                                                            if (p.gameMode == GameMode.SURVIVAL) {
                                                                curWorld.dropItem(p.location, deckManager.createBoosterPack(args[2]))
                                                            }
                                                        }
                                                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.BoughtCard")!!.replace("%amount%".toRegex(), buyPrice.toString())))
                                                    } else {
                                                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NotEnoughMoney")))
                                                    }
                                                } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.CannotBeBought")))
                                            } else {
                                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.PackDoesntExist")))
                                            }
                                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ChoosePack")))
                                    } else if (args[1].equals("card", ignoreCase = true)) {
                                        if (args.size > 2) {
                                            if (args.size > 3) {
                                                if (cardsConfig.contains("Cards." + args[2] + "." + args[3])) {
                                                    var buyPrice = 0.0
                                                    var canBuy = false
                                                    if (cardsConfig.contains("Cards." + args[2] + "." + args[3] + ".Buy-Price")) {
                                                        buyPrice = cardsConfig.getDouble("Cards." + args[2] + "." + args[3] + ".Buy-Price")
                                                        if (buyPrice > 0.0) canBuy = true
                                                    }
                                                    if (canBuy) {
                                                        if (TradingCards.econ!!.getBalance(p) >= buyPrice) {
                                                            if (config.getBoolean("PluginSupport.Vault.Closed-Economy")) {
                                                                TradingCards.econ!!.withdrawPlayer(p, buyPrice)
                                                                TradingCards.econ!!.depositPlayer(config.getString("PluginSupport.Vault.Server-Account"), buyPrice)
                                                            } else {
                                                                TradingCards.econ!!.withdrawPlayer(p, buyPrice)
                                                            }
                                                            if (p.inventory.firstEmpty() != -1) {
                                                                p.inventory.addItem(cardManager.createPlayerCard(args[3], args[2], 1, false))
                                                            } else {
                                                                val curWorld = p.world
                                                                if (p.gameMode == GameMode.SURVIVAL) {
                                                                    curWorld.dropItem(p.location, cardManager.createPlayerCard(args[3], args[2], 1, false))
                                                                }
                                                            }
                                                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.BoughtCard")!!.replace("%amount%".toRegex(), buyPrice.toString())))
                                                        } else {
                                                            sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NotEnoughMoney")))
                                                        }
                                                    } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.CannotBeBought")))
                                                } else {
                                                    sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.CardDoesntExist")))
                                                }
                                            } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ChooseCard")))
                                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.ChooseRarity")))
                                    } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.BuyUsage")))
                                } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.BuyUsage")))
                            } else {
                                sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoVault")))
                            }
                        } else sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoPerms")))
                    } else {
                        sender.sendMessage(cMsg(messagesConfig.getString("Messages.Prefix") + " " + messagesConfig.getString("Messages.NoCmd")))
                    }
                }
            } else {
                val showUsage = config.getBoolean("General.Show-Command-Usage", true)
                sender.sendMessage(cMsg(formatTitle(config.getString("General.Server-Name") + " Trading Cards")))
                if (sender.hasPermission("fwtc.reload")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.ReloadUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.ReloadHelp")))
                }
                if (sender.hasPermission("fwtc.givecard")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.GiveCardUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.GiveCardHelp")))
                }
                if (sender.hasPermission("fwtc.giveshinycard")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.GiveShinyCardUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.GiveShinyCardHelp")))
                }
                if (sender.hasPermission("fwtc.giverandomcard")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.GiveRandomCardUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.GiveRandomCardHelp")))
                }
                if (sender.hasPermission("fwtc.giveboosterpack")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.GiveBoosterPackUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.GiveBoosterPackHelp")))
                }
                if (sender.hasPermission("fwtc.giveaway")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.GiveawayUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.GiveawayHelp")))
                }
                if (sender.hasPermission("fwtc.getdeck")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.GetDeckUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.GetDeckHelp")))
                }
                if (sender.hasPermission("fwtc.list")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.ListUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.ListHelp")))
                }
                if (sender.hasPermission("fwtc.listpacks")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.ListPacksUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.ListPacksHelp")))
                }
                if (sender.hasPermission("fwtc.toggle")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.ToggleUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.ToggleHelp")))
                }
                if (sender.hasPermission("fwtc.create")) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.CreateUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.CreateHelp")))
                }
                if (sender.hasPermission("fwtc.buy") && TradingCards.instance.hasVault) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.BuyUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.BuyHelp")))
                }
                if (sender.hasPermission("fwtc.worth") && TradingCards.instance.hasVault) {
                    sender.sendMessage(cMsg("&7> &3" + messagesConfig.getString("Messages.WorthUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + messagesConfig.getString("Messages.WorthHelp")))
                }
                return true
            }
        }
        return true
    }

}