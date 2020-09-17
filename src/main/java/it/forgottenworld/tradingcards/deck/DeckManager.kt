package it.forgottenworld.tradingcards.deck

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.util.Utils.Companion.cMsg
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType

class DeckManager {

    val config = TradingCards.configManager.pluginConfig.config!!

    private val blankDeck: ItemStack
        get() {
            val itemStack = ItemStack(Material.getMaterial(config.getString("General.Deck-Material")!!)!!)
            itemStack.itemMeta?.customTagContainer?.setCustomTag(TradingCards.nameSpacedKey, ItemTagType.BYTE, 1)
            return itemStack
        }

    private val blankBoosterPack: ItemStack
        get() {
            val itemStack = ItemStack(Material.getMaterial(config.getString("General.BoosterPack-Material")!!)!!)
            itemStack.itemMeta?.customTagContainer?.setCustomTag(TradingCards.nameSpacedKey, ItemTagType.BYTE,1)
            return itemStack
        }

    fun createDeck(p: Player, num: Int): ItemStack {
        val deck = blankDeck
        val deckMeta = deck.itemMeta
        deckMeta!!.setDisplayName(cMsg(config.getString("General.Deck-Prefix") + p.name + "'s Deck #" + num))
        if (config.getBoolean("General.Hide-Enchants", true)) deckMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        deck.itemMeta = deckMeta
        deck.addUnsafeEnchantment(Enchantment.DURABILITY, 10)
        return deck
    }

    fun hasDeck(p: Player, num: Int): Boolean {
        for (i in p.inventory) {
            if (i != null &&
                    i.type == Material.valueOf(config.getString("General.Deck-Material")!!) &&
                    i.containsEnchantment(Enchantment.DURABILITY) &&
                    i.getEnchantmentLevel(Enchantment.DURABILITY) == 10) {
                val name = i.itemMeta!!.displayName
                val splitName: Array<String> = name.split("#").toTypedArray()
                if (num == splitName[1].toInt()) {
                    return true
                }
            }
        }
        return false
    }

    fun openDeck(p: Player, deckNum: Int) {
        val cardsConfig = TradingCards.configManager.cardsConfig.config!!
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck opened.")
        val uuidString = p.uniqueId.toString()
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck UUID: $uuidString")
        val contents = cardsConfig.getStringList("Decks.Inventories.$uuidString.$deckNum")
        val cards: MutableList<ItemStack?> = mutableListOf()
        val quantity: MutableList<Int?> = mutableListOf()
        var card: ItemStack?
        for (s in contents) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck file content: $s")
            val splitContents: Array<String> = s.split(",").toTypedArray()
            card = if (splitContents[3].equals("yes", ignoreCase = true)) {
                TradingCards.cardManager.createPlayerCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]), true)
            } else TradingCards.cardManager.getNormalCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]))
            cards.add(card)
            quantity.add(Integer.valueOf(splitContents[2]))
            if (config.getBoolean("General.Debug-Mode")) {
                println("[Cards] Put " + card + "," + splitContents[2] + " into respective lists.")
            }
        }
        val inv = Bukkit.createInventory(null, 27, cMsg("&c" + p.name + "'s Deck #" + deckNum))
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Created inventory.")
        var iter = 0
        for (i in cards) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Item " + i!!.type.toString() + " added to inventory!")
            i!!.amount = quantity[iter]!!
            if (inv.contains(i)) {
                i.amount = i.amount + 1
            } else inv.addItem(i)
            iter++
        }
        p.openInventory(inv)
    }

    fun createBoosterPack(name: String): ItemStack {
        val boosterPack = blankBoosterPack
        val numNormalCards = config.getInt("BoosterPacks.$name.NumNormalCards")
        val numSpecialCards = config.getInt("BoosterPacks.$name.NumSpecialCards")
        val prefix = config.getString("General.BoosterPack-Prefix")!!
        val normalCardColour = config.getString("Colours.BoosterPackNormalCards")!!
        val extraCardColour = config.getString("Colours.BoosterPackExtraCards")!!
        val loreColour = config.getString("Colours.BoosterPackLore")!!
        val nameColour = config.getString("Colours.BoosterPackName")!!
        val normalRarity = config.getString("BoosterPacks.$name.NormalCardRarity")!!
        val specialRarity = config.getString("BoosterPacks.$name.SpecialCardRarity")!!
        var extraRarity = ""
        var numExtraCards = 0
        var hasExtraRarity = false
        if (config.contains("BoosterPacks.$name.ExtraCardRarity") && config.contains("BoosterPacks.$name.NumExtraCards")) {
            hasExtraRarity = true
            extraRarity = config.getString("BoosterPacks.$name.ExtraCardRarity")!!
            numExtraCards = config.getInt("BoosterPacks.$name.NumExtraCards")
        }
        val specialCardColour = config.getString("Colours.BoosterPackSpecialCards")!!
        val pMeta = boosterPack.itemMeta
        pMeta!!.setDisplayName(cMsg(prefix + nameColour + name.replace("_".toRegex(), " ")))
        val lore: MutableList<String?> = mutableListOf()
        lore.add(cMsg(normalCardColour + numNormalCards + loreColour + " " + normalRarity.toUpperCase()))
        if (hasExtraRarity) lore.add(cMsg(extraCardColour + numExtraCards + loreColour + " " + extraRarity.toUpperCase()))
        lore.add(cMsg(specialCardColour + numSpecialCards + loreColour + " " + specialRarity.toUpperCase()))
        pMeta.lore = lore
        if (config.getBoolean("General.Hide-Enchants", true)) pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        boosterPack.itemMeta = pMeta
        boosterPack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
        return boosterPack
    }
}