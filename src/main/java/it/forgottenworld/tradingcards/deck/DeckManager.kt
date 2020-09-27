package it.forgottenworld.tradingcards.deck

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.printDebug
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object DeckManager {

    val config = Config.PLUGIN

    private val blankDeck
        get() = ItemStack(Material.getMaterial(config.getString("General.Deck-Material")!!)!!).apply {
            val meta = itemMeta
            meta?.persistentDataContainer?.set(TradingCards.nameSpacedKey, PersistentDataType.BYTE, 1)
            meta?.let { itemMeta = it }
        }

    private val blankBoosterPack
        get() = ItemStack(Material.getMaterial(config.getString("General.BoosterPack-Material")!!)!!).apply {
            val meta = itemMeta
            meta?.persistentDataContainer?.set(TradingCards.nameSpacedKey, PersistentDataType.BYTE, 1)
            itemMeta = meta
        }

    fun createDeck(p: Player, num: Int) =
            blankDeck.apply {
                val deckMeta = itemMeta!!
                deckMeta.setDisplayName(cMsg("${config.getString("General.Deck-Prefix")}${p.name}'s Deck #$num"))

                if (config.getBoolean("General.Hide-Enchants", true))
                    deckMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                itemMeta = deckMeta
                addUnsafeEnchantment(Enchantment.DURABILITY, 10)
            }

    fun hasDeck(p: Player, num: Int) =
            p.inventory.filterNotNull().any {
                it.type == Material.valueOf(config.getString("General.Deck-Material")!!) &&
                        it.getEnchantmentLevel(Enchantment.DURABILITY) == 10 &&
                        num == it.itemMeta!!.displayName.split("#")[1].toInt()
            }

    fun openDeck(p: Player, deckNum: Int) {
        val cardsConfig = Config.CARDS

        val uuidString = p.uniqueId.toString()

        if (Config.DEBUG) {
            println("[Cards] Deck opened.")
            println("[Cards] Deck UUID: $uuidString")
        }

        val contents = cardsConfig.getStringList("Decks.Inventories.$uuidString.$deckNum")
        val cards: MutableList<ItemStack?> = mutableListOf()
        val quantity: MutableList<Int?> = mutableListOf()
        for (s in contents) {

            printDebug("[Cards] Deck file content: $s")

            val splitContents = s.split(",")

            val card = if (splitContents[3].equals("yes", ignoreCase = true))
                CardManager.createPlayerCard(
                        splitContents[1],
                        splitContents[0],
                        Integer.valueOf(splitContents[2]),
                        true)
            else
                CardManager.getCard(
                        splitContents[1],
                        splitContents[0],
                        Integer.valueOf(splitContents[2]))

            cards.add(card)
            quantity.add(Integer.valueOf(splitContents[2]))
            printDebug("[Cards] Put $card,${splitContents[2]} into respective lists.")
        }

        val inv = Bukkit.createInventory(null, 27, cMsg("&c${p.name}'s Deck #$deckNum"))

        printDebug("[Cards] Created inventory.")

        for ((i, c) in cards.withIndex()) {

            printDebug("[Cards] Item ${c!!.type} added to inventory!")

            c.amount = quantity[i]!!
            if (inv.contains(c)) c.amount.inc() else inv.addItem(c)
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

        val itemMeta = boosterPack.itemMeta
        itemMeta?.apply {

            setDisplayName(cMsg("$prefix$nameColour${name.replace("_", " ")}"))

            lore = mutableListOf(
                    cMsg("$normalCardColour$numNormalCards$loreColour ${normalRarity.toUpperCase()}"),
                    if (hasExtraRarity) cMsg("$extraCardColour$numExtraCards$loreColour ${extraRarity.toUpperCase()}") else null,
                    cMsg("$specialCardColour$numSpecialCards$loreColour ${specialRarity.toUpperCase()}")
            ).filterNotNull()

            if (config.getBoolean("General.Hide-Enchants", true))
                addItemFlags(ItemFlag.HIDE_ENCHANTS)

        }
        boosterPack.itemMeta = itemMeta

        return boosterPack.apply { addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10) }
    }
}