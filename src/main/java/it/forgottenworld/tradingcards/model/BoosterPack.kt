package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.BoosterPacks
import it.forgottenworld.tradingcards.data.Colors
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.cMsg
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class BoosterPack (
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
                meta?.persistentDataContainer?.set(TradingCards.nameSpacedKey, PersistentDataType.BYTE, 1)
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
                setDisplayName(cMsg("${General.BoosterPackPrefix}${Colors.BoosterPackName}${name.replace("_", " ")}"))

                lore = listOfNotNull(
                        cMsg(Colors.BoosterPackNormalCards +
                                data.numNormalCards +
                                Colors.BoosterPackLore + " " +
                                data.normalCardRarity.name.toUpperCase()),
                        if (hasExtraRarity)
                            cMsg(
                                    Colors.BoosterPackExtraCards +
                                            numExtraCards +
                                            Colors.BoosterPackLore + " " +
                                            extraRarity.toUpperCase())
                        else
                            null,
                        cMsg(specialCardColour +
                                data.numSpecialCards +
                                Colors.BoosterPackLore + " " +
                                data.specialCardRarity.name.toUpperCase()))

                if (General.HideEnchants) addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }

            return boosterPack.apply { addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10) }
        }
    }
}