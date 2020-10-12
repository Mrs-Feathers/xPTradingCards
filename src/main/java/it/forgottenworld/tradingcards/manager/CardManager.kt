package it.forgottenworld.tradingcards.manager

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.Colors
import it.forgottenworld.tradingcards.data.DisplayNames
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.model.Rarity
import it.forgottenworld.tradingcards.util.MapRenderer
import it.forgottenworld.tradingcards.util.tC
import it.forgottenworld.tradingcards.util.wrapString
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.persistence.PersistentDataType
import java.io.File
import javax.imageio.ImageIO

object CardManager {

    private fun setCardItemStackDisplayName(itemStack: ItemStack, card: Card, isShiny: Boolean) {
        val meta = itemStack.itemMeta ?: return
        val shinyPrefix = if (isShiny) General.ShinyName else ""
        meta.setDisplayName(tC(DisplayNames.Title
                .replaceFirst("%PREFIX%", General.CardPrefix)
                .replaceFirst("%COLOUR%", card.rarity.color)
                .replaceFirst("%NAME%", card.name)
                .replaceFirst("%COST%", card.price.toString())
                .replace("_", " ")
                .replaceFirst("%SHINYPREFIX%", shinyPrefix)))
        itemStack.itemMeta = meta
    }

    private fun setCardItemStackLore(itemStack: ItemStack, card: Card, isShiny: Boolean) {
        val itemMeta = itemStack.itemMeta ?: return

        itemMeta.lore = mutableListOf(tC("${Colors.Type}${DisplayNames.Type}: &f${card.type}")).apply {

            if (card.info == "None" || card.info == "")
                add(tC("${Colors.Info}${DisplayNames.Info}: &f${card.info}"))
            else {
                add(tC("${Colors.Info}${DisplayNames.Info}:"))
                addAll(wrapString(card.info))
            }

            add(tC("${Colors.Series}${DisplayNames.Series}: &f${card.series}"))

            if (card.about != "None")
                add(tC("${Colors.About}${DisplayNames.About}: &f${card.about}"))

            add(tC("${Colors.Rarity}${ChatColor.BOLD}${if (isShiny) "${General.ShinyName} " else ""}${card.rarity.name}"))
        }

        if (General.HideEnchants)
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        itemStack.itemMeta = itemMeta

        if (isShiny)
            itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
    }

    private fun createBlankCardItemStack(quantity: Int) =
            ItemStack(General.CardMaterial, quantity).apply {
                val meta = itemMeta
                meta?.persistentDataContainer?.set(NamespacedKey(TradingCards.instance, "uncraftable"), PersistentDataType.BYTE, 1)
                meta?.let { itemMeta = it }
            }

    fun createCardItemStack(card: Card, quantity: Int, forcedShiny: Boolean = false) =
            createBlankCardItemStack(quantity).apply {
                val isShiny = forcedShiny || card.isShiny
                setCardItemStackDisplayName(this, card, isShiny)
                setCardItemStackLore(this, card, isShiny)
                setCardRenderer(this, card)
            }

    fun createRandomCardItemStack(rarity: Rarity) =
            createCardItemStack(rarity.values.random(), 1)

    private fun setCardRenderer(itemStack: ItemStack, card: Card) {
        val mapView = Bukkit.getWorld(General.MainWorldName)?.let {
            @Suppress("DEPRECATION")
            //Reason: https://www.spigotmc.org/threads/getmap-in-1-13.333754/#post-3115997
            Bukkit.getMap(card.mapViewId)
        }
        mapView?.addRenderer(MapRenderer(card.image, false))
        val itemMeta = itemStack.itemMeta as MapMeta
        itemMeta.persistentDataContainer.set(NamespacedKey(TradingCards.instance,"name"), PersistentDataType.STRING,card.name)
        itemMeta.persistentDataContainer.set(NamespacedKey(TradingCards.instance,"rarity"), PersistentDataType.STRING,card.rarity.name)
        if (mapView != null) {
            itemMeta.mapView = mapView
        }
        itemStack.setItemMeta(itemMeta as ItemMeta)
    }
}