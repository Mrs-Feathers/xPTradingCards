package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.*
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.capitalizeFully
import it.forgottenworld.tradingcards.util.tC
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class BoosterPack(
        val name: String,
        val numNormalCards: Int,
        val normalCardRarity: Rarity,
        val numSpecialCards: Int,
        val specialCardRarity: Rarity,
        val numExtraCards: Int,
        val extraCardRarity: Rarity?,
        val price: Double) {

    companion object {

        private val blankBoosterPack
            get() = ItemStack(General.BoosterPackMaterial).apply {
                val meta = itemMeta
                meta?.persistentDataContainer?.set(NamespacedKey(TradingCards.instance, "uncraftable"), PersistentDataType.BYTE, 1)
                itemMeta = meta
            }

        fun getItemStack(name: String): ItemStack {

            val boosterPack = blankBoosterPack

            val data = BoosterPacks[name] ?: return boosterPack

            var extraRarity = ""
            var numExtraCards = 0
            var hasExtraRarity = false

            if (data.numExtraCards > 0) {
                hasExtraRarity = true
                extraRarity = data.extraCardRarity?.name ?: Rarities.values.first().name
                numExtraCards = data.numExtraCards
            }

            val specialCardColour = Colors.BoosterPackSpecialCards

            boosterPack.itemMeta = boosterPack.itemMeta?.apply {
                setDisplayName(tC("${General.BoosterPackPrefix}${Colors.BoosterPackName}${name.replace("_", " ")}"))

                lore = listOfNotNull(
                        tC(Colors.BoosterPackNormalCards +
                                data.numNormalCards +
                                "${Colors.BoosterPackLore} " +
                                data.normalCardRarity.name.toUpperCase()),
                        if (hasExtraRarity)
                            tC(Colors.BoosterPackExtraCards +
                                    numExtraCards +
                                    "${Colors.BoosterPackLore} " +
                                    extraRarity.toUpperCase())
                        else null,
                        tC(specialCardColour +
                                data.numSpecialCards +
                                "${Colors.BoosterPackLore} " +
                                data.specialCardRarity.name.toUpperCase())
                )

                if (General.HideEnchants) addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }

            return boosterPack.apply { addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10) }
        }

        fun Player.tryOpenBoosterPack() {
            val heldItem = inventory.getItem(inventory.heldItemSlot) ?: return
            if (heldItem.type != General.BoosterPackMaterial || !hasPermission("fwtradingcards.openboosterpack"))
                return

            if (!heldItem.containsEnchantment(Enchantment.ARROW_INFINITE)) return

            if (gameMode == GameMode.CREATIVE) {
                sendMessage(tC("${Messages.Prefix} ${Messages.NoCreative}"))
                return
            }

            if (heldItem.amount > 1)
                heldItem.amount -= 1
            else
                inventory.removeItem(heldItem)

            val packMeta = heldItem.itemMeta ?: return
            val lore = packMeta.lore ?: return
            var hasExtra = false

            if (lore.size > 2) hasExtra = true
            val line1 = lore[0].split(" ", limit = 2)
            val line2 = lore[1].split(" ", limit = 2)
            val line3 = if (hasExtra) lore[2].split(" ", limit = 2) else listOf("")

            val normalCardAmount = ChatColor.stripColor(line1[0])!!.toInt()
            val specialCardAmount = ChatColor.stripColor(line2[0])!!.toInt()
            var extraCardAmount = 0
            if (hasExtra) extraCardAmount = ChatColor.stripColor(line3[0])!!.toInt()

            sendMessage(tC("${Messages.Prefix} ${Messages.OpenBoosterPack}"))

            val normalRarity = Rarities[line1[1].capitalizeFully()] ?: return
            val specialRarity = Rarities[line2[1].capitalizeFully()] ?: return

            for (i in 0 until normalCardAmount) {
                if (inventory.firstEmpty() != -1)
                    inventory.addItem(CardManager.createRandomCardItemStack(normalRarity))
                else if (gameMode == GameMode.SURVIVAL)
                    world.dropItem(location, CardManager.createRandomCardItemStack(normalRarity))
            }

            for (i in 0 until specialCardAmount) {
                if (inventory.firstEmpty() != -1)
                    inventory.addItem(CardManager.createRandomCardItemStack(specialRarity))
                else if (gameMode == GameMode.SURVIVAL)
                    world.dropItem(location, CardManager.createRandomCardItemStack(specialRarity))
            }

            if (hasExtra) for (i in 0 until extraCardAmount) {

                val extraRarity = Rarities[line3[1].capitalizeFully()]
                if (extraRarity != null) {
                    if (inventory.firstEmpty() != -1)
                        inventory.addItem(CardManager.createRandomCardItemStack(extraRarity))
                    else if (gameMode == GameMode.SURVIVAL)
                        world.dropItem(location, CardManager.createRandomCardItemStack(extraRarity))
                }
            }
        }
    }
}