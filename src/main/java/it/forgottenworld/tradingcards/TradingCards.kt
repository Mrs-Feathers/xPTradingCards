package it.forgottenworld.tradingcards

import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.apache.commons.lang.WordUtils
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.util.*
import kotlin.math.max

class TradingCards : JavaPlugin(), Listener, CommandExecutor {
    private var hostileMobs: List<EntityType> = listOf(EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.BLAZE, EntityType.SILVERFISH, EntityType.GHAST, EntityType.SLIME, EntityType.GUARDIAN, EntityType.MAGMA_CUBE, EntityType.WITCH, EntityType.ENDERMITE)
    private var neutralMobs: List<EntityType> = listOf(EntityType.ENDERMAN, EntityType.PIG_ZOMBIE, EntityType.WOLF, EntityType.SNOWMAN, EntityType.IRON_GOLEM)
    private var passiveMobs: List<EntityType> = listOf(EntityType.CHICKEN, EntityType.COW, EntityType.SQUID, EntityType.SHEEP, EntityType.PIG, EntityType.RABBIT, EntityType.VILLAGER, EntityType.BAT, EntityType.HORSE)
    private var bossMobs: List<EntityType> = listOf(EntityType.ENDER_DRAGON, EntityType.WITHER)
    private var hasVault = false
    private var deckData: FileConfiguration? = null
    private var deckDataFile: File? = null
    private var messagesData: FileConfiguration? = null
    private var messagesDataFile: File? = null
    private var cardsData: FileConfiguration? = null
    private var cardsDataFile: File? = null
    private var r = Random()
    private var taskid = 0
    
    override fun onEnable() {
        try {
            config.options().copyDefaults(true)
            server.pluginManager.addPermission(permRarities)
            saveDefaultConfig()
            server.pluginManager.registerEvents(this, this)
            val cmd = getCommand("fwtc")
            cmd?.setExecutor(this)
            reloadCustomConfig()
            saveDefaultDeckFile()
            reloadDeckData()
            saveDefaultMessagesFile()
            reloadMessagesData()
            saveDefaultCardsFile()
            reloadCardsData()
            /*if (getConfig().getBoolean("PluginSupport.Towny.Towny-Enabled"))
      if (getServer().getPluginManager().getPlugin("Towny") != null) {
        getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        System.out.println("[xPTradingCards] Towny successfully hooked!");
      } else {
        System.out.println("[xPTradingCards] Towny not found, hook unsuccessful!");
      }*/if (config.getBoolean("PluginSupport.Vault.Vault-Enabled")) if (server.pluginManager.getPlugin("Vault") != null) {
                setupEconomy()
                println("[xPTradingCards] Vault hook successful!")
                hasVault = true
            } else {
                println("[xPTradingCards] Vault not found, hook unsuccessful!")
            }
            /*if (getConfig().getBoolean("PluginSupport.MobArena.MobArena-Enabled"))
      if (getServer().getPluginManager().getPlugin("MobArena") != null) {
        PluginManager pm = getServer().getPluginManager();
        //MobArena maPlugin = (MobArena)pm.getPlugin("MobArena");
        //this.am = maPlugin.getArenaMaster();
        //pm.registerEvents(new MobArenaListener(this), this);
        //System.out.println("[xPTradingCards] Mob Arena hook successful!");
        this.hasMobArena = false;
      } else {
        System.out.println("[xPTradingCards] Mob Arena not found, hook unsuccessful!");
      }*/if (config.getBoolean("General.Schedule-Cards")) startTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        deckData = null
        deckDataFile = null
        messagesData = null
        messagesDataFile = null
        cardsData = null
        cardsDataFile = null
        econ = null
        perms = null
        chat = null
        server.pluginManager.removePermission(permRarities)
    }

    private fun setupEconomy() {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return
        econ = rsp.provider
    }

    private fun isMobHostile(e: EntityType): Boolean {
        return hostileMobs.contains(e)
    }

    private fun isMobNeutral(e: EntityType): Boolean {
        return neutralMobs.contains(e)
    }

    private fun isMobPassive(e: EntityType): Boolean {
        return passiveMobs.contains(e)
    }

    private fun isMobBoss(e: EntityType): Boolean {
        return bossMobs.contains(e)
    }

    private fun getBlankCard(quantity: Int): ItemStack {
        return ItemStack(Material.getMaterial(config.getString("General.Card-Material")!!)!!, quantity)
    }

    private val blankBoosterPack: ItemStack
        get() = ItemStack(Material.getMaterial(config.getString("General.BoosterPack-Material")!!)!!)

    private val blankDeck: ItemStack
        get() = ItemStack(Material.getMaterial(config.getString("General.Deck-Material")!!)!!)

    private fun createDeck(p: Player, num: Int): ItemStack {
        val deck = blankDeck
        val deckMeta = deck.itemMeta
        deckMeta!!.setDisplayName(cMsg(config.getString("General.Deck-Prefix") + p.name + "'s Deck #" + num))
        if (config.getBoolean("General.Hide-Enchants", true)) deckMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        deck.itemMeta = deckMeta
        deck.addUnsafeEnchantment(Enchantment.DURABILITY, 10)
        return deck
    }

    private fun reloadDeckData() {
        if (deckDataFile == null) {
            deckDataFile = File(dataFolder, "decks.yml")
        }
        deckData = YamlConfiguration.loadConfiguration(deckDataFile!!)
        var defConfigStream: Reader? = null
        try {
            defConfigStream = InputStreamReader(getResource("decks.yml")!!, "UTF8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        if (defConfigStream != null) {
            val defConfig = YamlConfiguration.loadConfiguration(defConfigStream)
            (deckData as YamlConfiguration).setDefaults(defConfig)
        }
    }

    private fun getDeckData(): FileConfiguration? {
        if (deckData == null) {
            reloadDeckData()
        }
        return deckData
    }

    private fun saveDeckData() {
        if (deckData == null || deckDataFile == null) {
            return
        }
        try {
            getDeckData()!!.save(deckDataFile!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveDefaultDeckFile() {
        if (deckDataFile == null) {
            deckDataFile = File(dataFolder, "decks.yml")
        }
        if (!deckDataFile!!.exists()) {
            saveResource("decks.yml", false)
        }
    }

    private fun reloadMessagesData() {
        if (messagesDataFile == null) {
            messagesDataFile = File(dataFolder, "messages.yml")
        }
        messagesData = YamlConfiguration.loadConfiguration(messagesDataFile!!)
        var defConfigStream: Reader? = null
        try {
            defConfigStream = InputStreamReader(getResource("messages.yml")!!, "UTF8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        if (defConfigStream != null) {
            val defConfig = YamlConfiguration.loadConfiguration(defConfigStream)
            (messagesData as YamlConfiguration).setDefaults(defConfig)
        }
    }

    private fun getMessagesData(): FileConfiguration? {
        if (messagesData == null) {
            reloadMessagesData()
        }
        return messagesData
    }

    private fun saveDefaultMessagesFile() {
        if (messagesDataFile == null) {
            messagesDataFile = File(dataFolder, "messages.yml")
        }
        if (!messagesDataFile!!.exists()) {
            saveResource("messages.yml", false)
        }
    }

    private fun reloadCardsData() {
        if (cardsDataFile == null) {
            cardsDataFile = File(dataFolder, "cards.yml")
        }
        cardsData = YamlConfiguration.loadConfiguration(cardsDataFile!!)
        var defConfigStream: Reader? = null
        try {
            defConfigStream = InputStreamReader(getResource("cards.yml")!!, "UTF8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        if (defConfigStream != null) {
            val defConfig = YamlConfiguration.loadConfiguration(defConfigStream)
            (cardsData as YamlConfiguration).setDefaults(defConfig)
        }
    }

    private fun getCardsData(): FileConfiguration? {
        if (cardsData == null) {
            reloadCardsData()
        }
        return cardsData
    }

    private fun saveCardsData() {
        if (cardsData == null || cardsDataFile == null) {
            return
        }
        try {
            getCardsData()!!.save(cardsDataFile!!)
        } catch (ignored: IOException) {
        }
    }

    private fun saveDefaultCardsFile() {
        if (cardsDataFile == null) {
            cardsDataFile = File(dataFolder, "cards.yml")
        }
        if (!cardsDataFile!!.exists()) {
            saveResource("cards.yml", false)
        }
    }

    private fun hasDeck(p: Player, num: Int): Boolean {
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

    private fun openDeck(p: Player, deckNum: Int) {
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck opened.")
        val uuidString = p.uniqueId.toString()
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck UUID: $uuidString")
        val contents = getDeckData()!!.getStringList("Decks.Inventories.$uuidString.$deckNum")
        val cards: MutableList<ItemStack?> = mutableListOf()
        val quantity: MutableList<Int?> = mutableListOf()
        var card: ItemStack?
        for (s in contents) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck file content: $s")
            val splitContents: Array<String> = s.split(",").toTypedArray()
            card = if (splitContents[3].equals("yes", ignoreCase = true)) {
                createPlayerCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]), true)
            } else getNormalCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]))
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

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Title: " + e.view.title)
        if (e.view.title.contains("s Deck #")) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck closed.")
            val contents = e.inventory.contents
            val title: Array<String> = e.view.title.split("'").toTypedArray()
            val titleNum: Array<String> = e.view.title.split("#").toTypedArray()
            val deckNum: Int = titleNum[1].toInt()
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck num: $deckNum")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Title: " + title[0])
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Title: " + title[1])
            val id = Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])!!).uniqueId
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] New ID: $id")
            val serialized: MutableList<String?> = mutableListOf()
            var arrayOfItemStack1: Array<ItemStack?>
            val j: Int = contents.also { arrayOfItemStack1 = it }.size
            for (i in 0 until j) {
                val it = arrayOfItemStack1[i]
                if (it != null && it.type != Material.AIR &&
                        it.type == Material.valueOf(config.getString("General.Card-Material")!!)) {
                    if (it.itemMeta!!.hasDisplayName()) {
                        val lore = it.itemMeta!!.lore
                        val shinyPrefix = config.getString("General.Shiny-Name")!!
                        val rarity: String = ChatColor.stripColor(lore!![lore.size - 1])!!.replace(shinyPrefix + " ".toRegex(), "")
                        val card = getCardName(rarity, it.itemMeta!!.displayName)
                        val amount = it.amount.toString()
                        var shiny = "no"
                        if (it.containsEnchantment(Enchantment.ARROW_INFINITE)) {
                            shiny = "yes"
                        }
                        val serializedString = "$rarity,$card,$amount,$shiny"
                        serialized.add(serializedString)
                        if (config.getBoolean("General.Debug-Mode")) {
                            println("[Cards] Added $serializedString to deck file.")
                        }
                    } else if (Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])!!).isOnline) {
                        val p = Bukkit.getPlayer(ChatColor.stripColor(title[0])!!)
                        val w = p!!.world
                        w.dropItem(p.location, it)
                    }
                }
            }
            getDeckData()!!["Decks.Inventories.$id.$deckNum"] = serialized
            saveDeckData()
        }
    }

    private fun getCardName(rarity: String, display: String): String {
        var hasPrefix = false
        var prefix: String? = ""
        if (config.contains("General.Card-Prefix") && config.getString("General.Card-Prefix") !== "") {
            hasPrefix = true
            prefix = ChatColor.stripColor(config.getString("General.Card-Prefix"))
        }
        val shinyPrefix = config.getString("General.Shiny-Name")!!
        var cleaned = ChatColor.stripColor(display)
        if (hasPrefix) cleaned = cleaned!!.replace(prefix!!.toRegex(), "")
        cleaned = cleaned!!.replace(shinyPrefix + " ".toRegex(), "")
        val cleanedArray: Array<String> = cleaned.split(" ").toTypedArray()
        val cs = getCardsData()!!.getConfigurationSection("Cards.$rarity")!!
        val keys = cs.getKeys(false)
        for (s in keys) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] getCardName s: $s")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] getCardName display: $display")
            val regex = Regex(".*\\b$s\\b.*")
            if (cleanedArray.size > 1) {
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] cleanedArray > 1")
                if ((cleanedArray[0] + "_" + cleanedArray[1]).matches(regex)) return s
                if (cleanedArray.size > 2 && (cleanedArray[1] + "_" + cleanedArray[2]).matches(regex)) return s
                if (cleanedArray.size > 3 && (cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3]).matches(regex)) return s
                if (cleanedArray.size > 4 && (cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4]).matches(Regex(".*\\b$s\\b.*"))) return s
                if (cleanedArray.size > 5 && (cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4] + "_" + cleanedArray[5]).matches(Regex(".*\\b$s\\b.*"))) return s
                if (cleanedArray[0].matches(regex)) return s
                if (cleanedArray[1].matches(regex)) {
                    return s
                }
            } else if (cleanedArray[0].matches(regex)) {
                return s
            }
        }
        return "None"
    }

    private fun createBoosterPack(name: String): ItemStack {
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

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            val p = event.player
            if (p.inventory.getItem(p.inventory.heldItemSlot)?.type == Material.valueOf(config.getString("General.BoosterPack-Material")!!) &&
                    event.player.hasPermission("fwtc.openboosterpack")) {
                if (p.gameMode != GameMode.CREATIVE) {
                    if (p.inventory.getItem(p.inventory.heldItemSlot)!!.containsEnchantment(Enchantment.ARROW_INFINITE)) {
                        if (p.inventory.getItem(p.inventory.heldItemSlot)!!.amount > 1)
                            p.inventory.getItem(p.inventory.heldItemSlot)!!.amount =
                                    p.inventory.getItem(p.inventory.heldItemSlot)!!.amount - 1
                        else p.inventory.removeItem(p.inventory.getItem(p.inventory.heldItemSlot))
                        val boosterPack = event.item
                        val packMeta = boosterPack!!.itemMeta
                        val lore = packMeta!!.lore
                        var hasExtra = false
                        if (lore!!.size > 2) hasExtra = true
                        val line1: Array<String> = lore[0].split(" ", limit = 2).toTypedArray()
                        val line2: Array<String> = lore[1].split(" ", limit = 2).toTypedArray()
                        var line3 = arrayOf("")
                        if (hasExtra) line3 = lore[2].split(" ", limit = 2).toTypedArray()
                        val normalCardAmount: Int = ChatColor.stripColor(line1[0])!!.toInt()
                        val specialCardAmount: Int = ChatColor.stripColor(line2[0])!!.toInt()
                        var extraCardAmount = 0
                        if (hasExtra) extraCardAmount = ChatColor.stripColor(line3[0])!!.toInt()
                        p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.OpenBoosterPack")))
                        for (i in 0 until normalCardAmount) {
                            if (p.inventory.firstEmpty() != -1) {
                                p.inventory.addItem(generateCard(WordUtils.capitalizeFully(line1[1])))
                            } else {
                                val curWorld = p.world
                                if (p.gameMode == GameMode.SURVIVAL) {
                                    curWorld.dropItem(p.location, generateCard(WordUtils.capitalizeFully(line1[1]))!!)
                                }
                            }
                        }
                        for (i in 0 until specialCardAmount) {
                            if (p.inventory.firstEmpty() != -1) {
                                p.inventory.addItem(generateCard(WordUtils.capitalizeFully(line2[1])))
                            } else {
                                val curWorld = p.world
                                if (p.gameMode == GameMode.SURVIVAL) {
                                    curWorld.dropItem(p.location, generateCard(WordUtils.capitalizeFully(line2[1]))!!)
                                }
                            }
                        }
                        if (hasExtra) for (i in 0 until extraCardAmount) {
                            if (p.inventory.firstEmpty() != -1) {
                                p.inventory.addItem(generateCard(WordUtils.capitalizeFully(line3[1])))
                            } else {
                                val curWorld = p.world
                                if (p.gameMode == GameMode.SURVIVAL) {
                                    curWorld.dropItem(p.location, generateCard(WordUtils.capitalizeFully(line3[1]))!!)
                                }
                            }
                        }
                    }
                } else event.player.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoCreative")))
            }
            if (p.inventory.getItem(p.inventory.heldItemSlot)?.type == Material.valueOf(config.getString("General.Deck-Material")!!)) {
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Deck material...")
                if (p.gameMode != GameMode.CREATIVE) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Not creative...")
                    if (p.inventory.getItem(p.inventory.heldItemSlot)?.containsEnchantment(Enchantment.DURABILITY) == true) {
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Has enchant...")
                        if (p.inventory.getItem(p.inventory.heldItemSlot)?.getEnchantmentLevel(Enchantment.DURABILITY) == 10) {
                            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Enchant is level 10...")
                            val name = p.inventory.getItem(p.inventory.heldItemSlot)?.itemMeta!!.displayName
                            val nameSplit: Array<String> = name.split("#").toTypedArray()
                            val num: Int = nameSplit[1].toInt()
                            openDeck(p, num)
                        }
                    }
                }
            }
        }
    }

    private fun calculateRarity(e: EntityType, alwaysDrop: Boolean): String? {
        val shouldItDrop = r.nextInt(100) + 1
        var bossRarity = 0
        var type = ""
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] shouldItDrop Num: $shouldItDrop")
        when {
            isMobHostile(e) -> {
                type = if (!alwaysDrop) {
                    if (shouldItDrop > config.getInt("Chances.Hostile-Chance")) return "None"
                    "Hostile"
                } else {
                    "Hostile"
                }
            }
            isMobNeutral(e) -> {
                type = if (!alwaysDrop) {
                    if (shouldItDrop > config.getInt("Chances.Neutral-Chance")) return "None"
                    "Neutral"
                } else {
                    "Neutral"
                }
            }
            isMobPassive(e) -> {
                type = if (!alwaysDrop) {
                    if (shouldItDrop > config.getInt("Chances.Passive-Chance")) return "None"
                    "Passive"
                } else {
                    "Passive"
                }
            }
            isMobBoss(e) -> {
                if (!alwaysDrop) {
                    if (shouldItDrop > config.getInt("Chances.Boss-Chance")) return "None"
                    if (config.getBoolean("Chances.Boss-Drop")) bossRarity = config.getInt("Chances.Boss-Drop-Rarity")
                }
                type = "Boss"
            }
            else -> return "None"
        }
        val rarities = config.getConfigurationSection("Rarities")!!
        val rarityKeys = rarities.getKeys(false)
        val rarityChances: MutableMap<String?, Int?> = mutableMapOf()
        val rarityIndexes: MutableMap<Int?, String?> = mutableMapOf()
        var i = 0
        var mini = 0
        val random = r.nextInt(100000) + 1
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Random Card Num: $random")
        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Type: $type")
        for (key in rarityKeys) {
            rarityIndexes[i] = key
            i++
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] $i, $key")
            if (config.contains("Chances." + key + "." + StringUtils.capitalize(e.getName())) && mini == 0) {
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Mini: $i")
                mini = i
            }
            val chance = config.getInt("Chances.$key.$type", -1)
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Keys: $key, $chance, i=$i")
            rarityChances[key] = chance
        }
        if (mini != 0) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Mini: $mini")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] i: $i")
            while (i >= mini) {
                i--
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] i: $i")
                val chance = config.getInt("Chances." + rarityIndexes[i] + "." + StringUtils.capitalize(e.getName()), -1)
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Chance: $chance")
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Rarity: " + rarityIndexes[i])
                if (chance > 0) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Chance > 0")
                    if (random <= chance) {
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Random <= Chance")
                        return rarityIndexes[i]
                    }
                }
            }
        } else {
            while (i > 0) {
                i--
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Final loop iteration $i")
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Iteration " + i + " in HashMap is: " + rarityIndexes[i] + ", " + config.getString(StringBuilder("Rarities.").append(rarityIndexes[i]).append(".Name").toString()))
                val chance: Int = config.getInt("Chances." + rarityIndexes[i] + "." + type, -1)
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] " + config.getString(StringBuilder("Rarities.").append(rarityIndexes[i]).append(".Name").toString()) + "'s chance of dropping: " + chance + " out of 100,000")
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] The random number we're comparing that against is: $random")
                if (chance > 0 &&
                        random <= chance) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Yup, looks like $random is definitely lower than $chance!")
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Giving a " + config.getString(StringBuilder("Rarities.").append(rarityIndexes[i]).append(".Name").toString()) + " card.")
                    return rarityIndexes[i]
                }
            }
        }
        return "None"
    }

    private fun isOnList(p: Player?): Boolean {
        val playersOnList = config.getStringList("Blacklist.Players")
        return playersOnList.contains(p!!.name)
    }

    private fun addToList(p: Player) {
        val playersOnList = config.getStringList("Blacklist.Players")
        playersOnList.add(p.name)
        config["Blacklist.Players"] = null
        config["Blacklist.Players"] = playersOnList
        saveConfig()
    }

    private fun removeFromList(p: Player) {
        val playersOnList = config.getStringList("Blacklist.Players")
        playersOnList.remove(p.name)
        config["Blacklist.Players"] = null
        config["Blacklist.Players"] = playersOnList
        saveConfig()
    }

    private fun blacklistMode(): Char {
        return if (config.getBoolean("Blacklist.Whitelist-Mode")) 'w' else 'b'
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        var drop = false
        var worldName = ""
        var worlds: List<String?> = ArrayList()
        if (e.entity.killer != null) {
            val p = e.entity.killer
            drop = if (isOnList(p) && blacklistMode() == 'b') {
                false
            } else if (!isOnList(p) && blacklistMode() == 'b') {
                true
            } else isOnList(p) && blacklistMode() == 'w'
            worldName = p!!.world.name
            worlds = config.getStringList("World-Blacklist")
            /*if (this.hasMobArena) {
        int i = 0;
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mob Arena checks starting.");
        if ((this.am.getArenas() != null) && (!this.am.getArenas().isEmpty())) {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] There is at least 1 arena!");
          for (Arena arena : this.am.getArenas()) {
            i++;
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] For arena #" + i + "...");
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] In arena?: " + arena.inArena(p));
            if ((arena.inArena(p)) || (arena.inLobby(p))) {
              if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Killer is in an arena/lobby, so let's mess with the drops.");
              if (getConfig().getBoolean("PluginSupport.MobArena.Disable-In-Arena")) drop = false;
              if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Drops are now: " + drop);
            }
            else if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Killer is not in this arena!");
            }
          }
        }
      }*/
        }
        if (drop &&
                !worlds.contains(worldName)) {
            var rare = calculateRarity(e.entityType, false)
            if (config.getBoolean("Chances.Boss-Drop") && isMobBoss(e.entityType)) rare = config.getString("Chances.Boss-Drop-Rarity")
            var cancelled = false
            if (rare !== "None") {
                if (config.getBoolean("General.Spawner-Block") &&
                        e.entity.customName != null &&
                        e.entity.customName == config.getString("General.Spawner-Mob-Name")) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Mob came from spawner, not dropping card.")
                    cancelled = true
                }
                if (!cancelled) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Successfully generated card.")
                    if (generateCard(rare) != null) e.drops.add(generateCard(rare))
                }
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (config.getBoolean("General.Player-Drops-Card") && config.getBoolean("General.Auto-Add-Players")) {
            val killer: Entity? = e.entity.killer
            if (killer != null) {
                val rarities = config.getConfigurationSection("Rarities")!!
                val rarityKeys = rarities.getKeys(false)
                var k: String? = null
                for (key in rarityKeys) {
                    if (getCardsData()!!.contains("Cards." + key + "." + e.entity.name)) {
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] $key")
                        k = key
                    }
                }
                if (k != null) {
                    val rndm = r.nextInt(100) + 1
                    if (rndm <= config.getInt("General.Player-Drops-Card-Rarity")) {
                        e.drops.add(createPlayerCard(e.entity.name, k, 1, false))
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] " + e.drops.toString())
                    }
                } else {
                    println("k is null")
                }
            }
        }
    }

    private fun generateCard(rare: String?): ItemStack? {
        if (rare != "None") {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] generateCard.rare: $rare")
            val card = getBlankCard(1)
            reloadCustomConfig()
            val cardSection = getCardsData()!!.getConfigurationSection("Cards.$rare")!!
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] generateCard.cardSection: " + getCardsData()!!.contains(StringBuilder("Cards.").append(rare).toString()))
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] generateCard.rarity: $rare")
            val cards = cardSection.getKeys(false)
            val cardNames: MutableList<String?> = mutableListOf()
            cardNames.addAll(cards)
            val cIndex = r.nextInt(cardNames.size)
            val cardName = cardNames[cIndex]
            val hasShinyVersion = getCardsData()!!.getBoolean("Cards.$rare.$cardName.Has-Shiny-Version")
            var isShiny = false
            if (hasShinyVersion) {
                val shinyRandom = r.nextInt(100) + 1
                if (shinyRandom <= config.getInt("Chances.Shiny-Version-Chance")) isShiny = true
            }
            val rarityColour = config.getString("Rarities.$rare.Colour")!!
            val prefix = config.getString("General.Card-Prefix")!!
            val series = getCardsData()!!.getString("Cards.$rare.$cardName.Series")!!
            val seriesColour = config.getString("Colours.Series")!!
            val seriesDisplay = config.getString("DisplayNames.Cards.Series", "Series")!!
            val about = getCardsData()!!.getString("Cards.$rare.$cardName.About", "None")!!
            val aboutColour = config.getString("Colours.About")!!
            val aboutDisplay = config.getString("DisplayNames.Cards.About", "About")!!
            val type = getCardsData()!!.getString("Cards.$rare.$cardName.Type")!!
            val typeColour = config.getString("Colours.Type")!!
            val typeDisplay = config.getString("DisplayNames.Cards.Type", "Type")!!
            val info = getCardsData()!!.getString("Cards.$rare.$cardName.Info")!!
            val infoColour = config.getString("Colours.Info")!!
            val infoDisplay = config.getString("DisplayNames.Cards.Info", "Info")!!
            val shinyPrefix = config.getString("General.Shiny-Name")!!
            val cost: String
            cost = if (getCardsData()!!.contains("Cards.$rare.$cardName.Buy-Price")) getCardsData()!!.getDouble("Cards.$rare.$cardName.Buy-Price").toString() else "None"
            val cmeta = card.itemMeta
            if (isShiny) {
                cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.ShinyTitle")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName!!).replace("%COST%".toRegex(), cost).replace("%SHINYPREFIX%".toRegex(), shinyPrefix).replace("_".toRegex(), " ")))
            } else cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.Title")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName!!).replace("%COST%".toRegex(), cost).replace("_".toRegex(), " ")))
            val lore: MutableList<String?> = mutableListOf()
            lore.add(cMsg("$typeColour$typeDisplay: &f$type"))
            if (info == "None" || info == "") {
                lore.add(cMsg("$infoColour$infoDisplay: &f$info"))
            } else {
                lore.add(cMsg("$infoColour$infoDisplay:"))
                lore.addAll(wrapString(info))
            }
            lore.add(cMsg("$seriesColour$seriesDisplay: &f$series"))
            if (getCardsData()!!.contains("Cards.$rare.$cardName.About")) lore.add(cMsg("$aboutColour$aboutDisplay: &f$about"))
            if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + config.getString("General.Shiny-Name") + " " + rare)) else lore.add(cMsg(rarityColour + ChatColor.BOLD + rare))
            cmeta.lore = lore
            if (config.getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            card.itemMeta = cmeta
            if (isShiny) {
                card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
            }
            return card
        }
        return null
    }

    private fun wrapString(s: String?): List<String?> {
        val parsedString = ChatColor.stripColor(s)
        val addedString = WordUtils.wrap(parsedString, config.getInt("General.Info-Line-Length", 25), "\n", true)
        val splitString: Array<String> = addedString.split("\n").toTypedArray()
        val finalArray: MutableList<String?> = mutableListOf()
        var arrayOfString1: Array<String>
        val j: Int = splitString.also { arrayOfString1 = it }.size
        for (i in 0 until j) {
            val ss = arrayOfString1[i]
            println(ChatColor.getLastColors(ss))
            finalArray.add(cMsg("&f &7- &f$ss"))
        }
        return finalArray
    }

    private fun createPlayerCard(cardName: String, rarity: String, num: Int, forcedShiny: Boolean): ItemStack {
        val card = getBlankCard(num)
        val hasShinyVersion = getCardsData()!!.getBoolean("Cards.$rarity.$cardName.Has-Shiny-Version")
        var isShiny = false
        if (hasShinyVersion) {
            val shinyRandom = r.nextInt(100) + 1
            if (shinyRandom <= config.getInt("Chances.Shiny-Version-Chance")) isShiny = true
        }
        if (forcedShiny) isShiny = true
        val rarityColour = config.getString("Rarities.$rarity.Colour")!!
        val prefix = config.getString("General.Card-Prefix")!!
        val series = getCardsData()!!.getString("Cards.$rarity.$cardName.Series")!!
        val seriesColour = config.getString("Colours.Series")!!
        val seriesDisplay = config.getString("DisplayNames.Cards.Series", "Series")!!
        val about = getCardsData()!!.getString("Cards.$rarity.$cardName.About", "None")!!
        val aboutColour = config.getString("Colours.About")!!
        val aboutDisplay = config.getString("DisplayNames.Cards.About", "About")!!
        val type = getCardsData()!!.getString("Cards.$rarity.$cardName.Type")!!
        val typeColour = config.getString("Colours.Type")!!
        val typeDisplay = config.getString("DisplayNames.Cards.Type", "Type")!!
        val info = getCardsData()!!.getString("Cards.$rarity.$cardName.Info")!!
        val infoColour = config.getString("Colours.Info")!!
        val infoDisplay = config.getString("DisplayNames.Cards.Info", "Info")!!
        val shinyPrefix = config.getString("General.Shiny-Name")!!
        val cost: String = if (getCardsData()!!.contains("Cards.$rarity.$cardName.Buy-Price")) getCardsData()!!.getDouble("Cards.$rarity.$cardName.Buy-Price").toString() else "None"
        val cmeta = card.itemMeta
        if (isShiny) {
            cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.ShinyTitle")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName).replace("%COST%".toRegex(), cost).replace("%SHINYPREFIX%".toRegex(), shinyPrefix).replace("_".toRegex(), " ")))
        } else cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.Title")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName).replace("%COST%".toRegex(), cost).replace("_".toRegex(), " ")))
        val lore: MutableList<String?> = mutableListOf()
        lore.add(cMsg("$typeColour$typeDisplay: &f$type"))
        if (info == "None" || info == "") {
            lore.add(cMsg("$infoColour$infoDisplay: &f$info"))
        } else {
            lore.add(cMsg("$infoColour$infoDisplay:"))
            lore.addAll(wrapString(info))
        }
        lore.add(cMsg("$seriesColour$seriesDisplay: &f$series"))
        if (getCardsData()!!.contains("Cards.$rarity.$cardName.About")) lore.add(cMsg("$aboutColour$aboutDisplay: &f$about"))
        if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + config.getString("General.Shiny-Name") + " " + rarity)) else lore.add(cMsg(rarityColour + ChatColor.BOLD + rarity))
        cmeta.lore = lore
        if (config.getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        card.itemMeta = cmeta
        if (isShiny) {
            card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
        }
        return card
    }

    private fun getNormalCard(cardName: String, rarity: String, num: Int): ItemStack {
        val card = getBlankCard(num)
        val rarityColour = config.getString("Rarities.$rarity.Colour")!!
        val prefix = config.getString("General.Card-Prefix")!!
        val series = getCardsData()!!.getString("Cards.$rarity.$cardName.Series")!!
        val seriesColour = config.getString("Colours.Series")!!
        val seriesDisplay = config.getString("DisplayNames.Cards.Series", "Series")!!
        val about = getCardsData()!!.getString("Cards.$rarity.$cardName.About", "None")!!
        val aboutColour = config.getString("Colours.About")!!
        val aboutDisplay = config.getString("DisplayNames.Cards.About", "About")!!
        val type = getCardsData()!!.getString("Cards.$rarity.$cardName.Type")!!
        val typeColour = config.getString("Colours.Type")!!
        val typeDisplay = config.getString("DisplayNames.Cards.Type", "Type")!!
        val info = getCardsData()!!.getString("Cards.$rarity.$cardName.Info")!!
        val infoColour = config.getString("Colours.Info")!!
        val infoDisplay = config.getString("DisplayNames.Cards.Info", "Info")!!
        val cost: String = if (getCardsData()!!.contains("Cards.$rarity.$cardName.Buy-Price")) getCardsData()!!.getDouble("Cards.$rarity.$cardName.Buy-Price").toString() else "None"
        val cmeta = card.itemMeta
        cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.Title")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName).replace("%COST%".toRegex(), cost).replace("_".toRegex(), " ")))
        val lore: MutableList<String?> = mutableListOf()
        lore.add(cMsg("$typeColour$typeDisplay: &f$type"))
        if (info == "None" || info == "") {
            lore.add(cMsg("$infoColour$infoDisplay: &f$info"))
        } else {
            lore.add(cMsg("$infoColour$infoDisplay:"))
            lore.addAll(wrapString(info))
        }
        lore.add(cMsg("$seriesColour$seriesDisplay: &f$series"))
        if (getCardsData()!!.contains("Cards.$rarity.$cardName.About")) lore.add(cMsg("$aboutColour$aboutDisplay: &f$about"))
        lore.add(cMsg(rarityColour + ChatColor.BOLD + rarity))
        cmeta.lore = lore
        if (config.getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        card.itemMeta = cmeta
        return card
    }

    @EventHandler
    fun onMobSpawn(e: CreatureSpawnEvent) {
        if (e.entity !is Player &&
                e.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER && config.getBoolean("General.Spawner-Block")) {
            e.entity.customName = config.getString("General.Spawner-Mob-Name")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Spawner mob renamed.")
            e.entity.removeWhenFarAway = true
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (config.getBoolean("General.Auto-Add-Players")) {
            val p = e.player
            val gc = GregorianCalendar()
            val date: Int
            val month: Int
            val year: Int
            if (p.hasPlayedBefore()) {
                gc.timeInMillis = p.firstPlayed
            } else {
                gc.timeInMillis = System.currentTimeMillis()
            }
            date = gc[Calendar.DATE]
            month = gc[Calendar.MONTH] + 1
            year = gc[Calendar.YEAR]
            val rarities = config.getConfigurationSection("Rarities")!!
            var i = 1
            val rarityKeys = rarities.getKeys(false)
            val children = permRarities.children
            var rarity = config.getString("General.Auto-Add-Player-Rarity")!!
            for (key in rarityKeys) {
                i++
                children["fwtc.rarity.$key"] = java.lang.Boolean.FALSE
                permRarities.recalculatePermissibles()
                if (p.hasPermission("fwtc.rarity.$key")) {
                    rarity = key
                    break
                }
            }
            if (p.isOp) rarity = config.getString("General.Player-Op-Rarity")!!
            if (!getCardsData()!!.contains("Cards." + rarity + "." + p.name)) {
                val series = config.getString("General.Player-Series")!!
                val type = config.getString("General.Player-Type")!!
                val hasShiny = config.getBoolean("General.Player-Has-Shiny-Version")
                getCardsData()!!["Cards." + rarity + "." + p.name + ".Series"] = series
                getCardsData()!!["Cards." + rarity + "." + p.name + ".Type"] = type
                getCardsData()!!["Cards." + rarity + "." + p.name + ".Has-Shiny-Version"] = hasShiny
                if (config.getBoolean("General.American-Mode")) getCardsData()!!["Cards." + rarity + "." + p.name + ".Info"] = "Joined $month/$date/$year" else getCardsData()!!["Cards." + rarity + "." + p.name + ".Info"] = "Joined $date/$month/$year"
                saveCardsData()
                reloadCardsData()
            }
        }
    }

    private fun createCard(creator: Player, rarity: String, name: String, series: String, type: String, hasShiny: Boolean, info: String) {
        if (!getCardsData()!!.contains("Cards.$rarity.$name")) {
            if (name.matches(Regex("^[a-zA-Z0-9-_]+$"))) {
                val rarities = getCardsData()!!.getConfigurationSection("Cards")!!
                val rarityKeys = rarities.getKeys(false)
                var keyToUse = ""
                for (key in rarityKeys) {
                    if (key.equals(rarity, ignoreCase = true)) {
                        keyToUse = key
                    }
                }
                if (keyToUse != "") {
                    val regex = Regex("^[a-zA-Z0-9-_]+$")
                    val series1 = if (series.matches(regex)) series else "None"
                    val type1 = if (type.matches(regex)) type else "None"
                    val info1 = if (info.matches(regex)) info else "None"
                    val hasShiny1: Boolean = hasShiny
                    getCardsData()!!["Cards.$rarity.$name.Series"] = series1
                    getCardsData()!!["Cards.$rarity.$name.Type"] = type1
                    getCardsData()!!["Cards.$rarity.$name.Has-Shiny-Version"] = hasShiny1
                    getCardsData()!!["Cards.$rarity.$name.Info"] = info1
                    saveCardsData()
                    reloadCardsData()
                    creator.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CreateSuccess")!!.replace("%name%".toRegex(), name).replace("%rarity%".toRegex(), rarity)))
                } else {
                    creator.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoRarity")))
                }
            } else {
                creator.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CreateNoName")))
            }
        } else creator.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CreateExists")))
    }

    private fun reloadCustomConfig() {
        val file = File(dataFolder.toString() + File.separator + "config.yml")
        if (!file.exists()) {
            config.options().copyDefaults(true)
            saveDefaultConfig()
        }
        reloadConfig()
        reloadDeckData()
        reloadMessagesData()
        reloadCardsData()
        reloadDeckData()
        reloadMessagesData()
        reloadCardsData()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (cmd.name.equals("fwtc", ignoreCase = true)) {
            if (args.isNotEmpty()) {
                if (args[0].equals("reload", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.reload")) {
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.Reload")))
                        reloadCustomConfig()
                        if (config.getBoolean("General.Schedule-Cards")) startTimer()
                        return true
                    }
                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                } else if (args[0].equals("toggle", ignoreCase = true)) {
                    val p = sender as Player
                    if (isOnList(p) && blacklistMode() == 'b') {
                        removeFromList(p)
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ToggleEnabled")))
                    } else if (isOnList(p) && blacklistMode() == 'w') {
                        removeFromList(p)
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ToggleDisabled")))
                    } else if (!isOnList(p) && blacklistMode() == 'b') {
                        addToList(p)
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ToggleDisabled")))
                    } else if (!isOnList(p) && blacklistMode() == 'w') {
                        addToList(p)
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ToggleEnabled")))
                    }
                } else if (args[0].equals("create", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.create")) {
                        val p = sender as Player
                        if (args.size < 8) {
                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CreateUsage")))
                        } else {
                            val isShiny = args[5].equals("true", ignoreCase = true) || args[5].equals("yes", ignoreCase = true) || args[5].equals("y", ignoreCase = true)
                            createCard(p, args[1].replace("_".toRegex(), " "), args[2], args[3].replace("_".toRegex(), " "), args[4].replace("_".toRegex(), " "), isShiny, args[6].replace("_".toRegex(), " "))
                        }
                    }
                } else if (args[0].equals("givecard", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.givecard")) {
                        if (args.size > 2) {
                            val p = sender as Player
                            if (getCardsData()!!.contains("Cards." + args[1].replace("_".toRegex(), " ") + "." + args[2])) p.inventory.addItem(getNormalCard(args[2], args[1].replace("_".toRegex(), " "), 1)) else {
                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoCard")))
                            }
                        } else {
                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveCardUsage")))
                        }
                    } else {
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("giveshinycard", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.giveshinycard")) {
                        if (args.size > 2) {
                            val p = sender as Player
                            if (getCardsData()!!.contains("Cards." + args[1].replace("_".toRegex(), " ") + "." + args[2])) p.inventory.addItem(createPlayerCard(args[2], args[1].replace("_".toRegex(), " "), 1, true)) else {
                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoCard")))
                            }
                        } else {
                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveCardUsage")))
                        }
                    } else {
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("giveboosterpack", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.giveboosterpack")) {
                        if (args.size > 2) {
                            if (config.contains("BoosterPacks." + args[2].replace(" ".toRegex(), "_"))) {
                                if (Bukkit.getPlayer(args[1]) != null) {
                                    val p = Bukkit.getPlayer(args[1])
                                    if (p!!.inventory.firstEmpty() != -1) {
                                        p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.BoosterPackMsg")))
                                        p.inventory.addItem(createBoosterPack(args[2]))
                                    } else {
                                        val curWorld = p.world
                                        if (p.gameMode == GameMode.SURVIVAL) {
                                            p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.BoosterPackMsg")))
                                            curWorld.dropItem(p.location, createBoosterPack(args[2]))
                                        }
                                    }
                                } else {
                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPlayer")))
                                }
                            } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoBoosterPack")))
                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveBoosterPackUsage")))
                    } else {
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("getdeck", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.getdeck")) {
                        if (args.size > 1) {
                            if (StringUtils.isNumeric(args[1])) {
                                if (sender.hasPermission("fwtc.decks." + args[1])) {
                                    val p = sender as Player
                                    if (!hasDeck(p, args[1].toInt())) {
                                        if (p.inventory.firstEmpty() != -1) {
                                            p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveDeck")))
                                            p.inventory.addItem(createDeck(p, args[1].toInt()))
                                        } else {
                                            val curWorld = p.world
                                            if (p.gameMode == GameMode.SURVIVAL) {
                                                p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveDeck")))
                                                curWorld.dropItem(p.location, createDeck(p, args[1].toInt()))
                                            }
                                        }
                                    } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.AlreadyHaveDeck")))
                                } else {
                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.MaxDecks")))
                                }
                            } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GetDeckUsage")))
                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GetDeckUsage")))
                    } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                } else if (args[0].equals("giverandomcard", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.randomcard")) {
                        if (args.size > 2) {
                            if (Bukkit.getPlayer(args[2]) != null) {
                                val p = Bukkit.getPlayer(args[2])
                                try {
                                    EntityType.valueOf(args[1].toUpperCase())
                                    val rare = calculateRarity(EntityType.valueOf(args[1].toUpperCase()), true)
                                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] onCommand.rare: $rare")
                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveRandomCardMsg")!!.replace("%player%".toRegex(), p!!.name)))
                                    if (p.inventory.firstEmpty() != -1) {
                                        p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveRandomCard")))
                                        if (generateCard(rare) != null) p.inventory.addItem(generateCard(rare))
                                    } else {
                                        val curWorld = p.world
                                        if (p.gameMode == GameMode.SURVIVAL) {
                                            p.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveRandomCard")))
                                            if (generateCard(rare) != null) curWorld.dropItem(p.location, generateCard(rare)!!)
                                        }
                                    }
                                } catch (e: IllegalArgumentException) {
                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoEntity")))
                                }
                            } else {
                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPlayer")))
                            }
                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveRandomCardUsage")))
                    } else {
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                    }
                } else if (args[0].equals("list", ignoreCase = true)) {
                    if (sender.hasPermission("fwtc.list")) {
                        val cards = getCardsData()!!.getConfigurationSection("Cards")!!
                        val cardKeys = cards.getKeys(false)
                        var msg = ""
                        var i = 0
                        var finalMsg = ""
                        for (key in cardKeys) {
                            val cardsWithKey = getCardsData()!!.getConfigurationSection("Cards.$key")!!
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
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
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
                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                        }
                    } else if (args[0].equals("giveaway", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.giveaway")) {
                            if (args.size > 1) {
                                val rarities = getCardsData()!!.getConfigurationSection("Cards")!!
                                val rarityKeys = rarities.getKeys(false)
                                var keyToUse = ""
                                for (key in rarityKeys) {
                                    if (key.equals(args[1].replace("_".toRegex(), " "), ignoreCase = true)) {
                                        keyToUse = key
                                    }
                                }
                                if (keyToUse != "") {
                                    Bukkit.broadcastMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.Giveaway")!!.replace("%player%".toRegex(), sender.name).replace("%rarity%".toRegex(), keyToUse)))
                                    for (p in Bukkit.getOnlinePlayers()) {
                                        val cards = getCardsData()!!.getConfigurationSection("Cards.$keyToUse")!!
                                        val cardKeys = cards.getKeys(false)
                                        val rIndex = r.nextInt(cardKeys.size)
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
                                            p.inventory.addItem(createPlayerCard(cardName, keyToUse, 1, false))
                                        } else {
                                            val curWorld = p.world
                                            if (p.gameMode == GameMode.SURVIVAL) {
                                                curWorld.dropItem(p.location, createPlayerCard(cardName, keyToUse, 1, false))
                                            }
                                        }
                                    }
                                } else {
                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoRarity")))
                                }
                            } else {
                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.GiveawayUsage")))
                            }
                        } else {
                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                        }
                    } else if (args[0].equals("worth", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.worth")) {
                            if (hasVault) {
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
                                    if (getCardsData()!!.contains("Cards.$rarity.$cardName.Buy-Price")) {
                                        buyPrice = getCardsData()!!.getDouble("Cards.$rarity.$cardName.Buy-Price")
                                        if (buyPrice > 0.0) canBuy = true
                                    }
                                    if (canBuy) {
                                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CanBuy")!!.replace("%buyAmount%".toRegex(), buyPrice.toString())))
                                    } else {
                                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CanNotBuy")))
                                    }
                                } else {
                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NotACard")))
                                }
                            } else {
                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoVault")))
                            }
                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                    } else if (args[0].equals("credits", ignoreCase = true)) {
                        sender.sendMessage(cMsg(formatTitle("Credits and Special Thanks")))
                        sender.sendMessage(cMsg("&7[&aDeveloper&7] &aLukas Xenoyia Gentle"))
                        sender.sendMessage(cMsg("   &7- &6&oxPXenoyia&f, &6&oXenoyia&f, &6&oxPLukas&f, &6&oSnoopDogg&f"))
                        sender.sendMessage(cMsg("&7[&eSpecial Thanks&7] XpanD, IrChaos, xtechgamer735, PTsandro, FlyingSquidwolf, iXRaZoRXi, iToxy, TowelieDOH, Miku_Snow, NOBUTSS, doitliketyler, Celebrimbor90, Magz, GypsySix, bumbble, iFosadrink_2, Sunique, TheRealGSD, Zenko, Berkth, TubeCraftXXL, Cra2ytig3r, marcosds13, ericbarbwire, Bonzo"))
                    } else if (args[0].equals("buy", ignoreCase = true)) {
                        if (sender.hasPermission("fwtc.buy")) {
                            if (hasVault) {
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
                                                    if (econ!!.getBalance(p) >= buyPrice) {
                                                        if (config.getBoolean("PluginSupport.Vault.Closed-Economy")) {
                                                            econ!!.withdrawPlayer(p, buyPrice)
                                                            econ!!.depositPlayer(config.getString("PluginSupport.Vault.Server-Account"), buyPrice)
                                                        } else {
                                                            econ!!.withdrawPlayer(p, buyPrice)
                                                        }
                                                        if (p.inventory.firstEmpty() != -1) {
                                                            p.inventory.addItem(createBoosterPack(args[2]))
                                                        } else {
                                                            val curWorld = p.world
                                                            if (p.gameMode == GameMode.SURVIVAL) {
                                                                curWorld.dropItem(p.location, createBoosterPack(args[2]))
                                                            }
                                                        }
                                                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.BoughtCard")!!.replace("%amount%".toRegex(), buyPrice.toString())))
                                                    } else {
                                                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NotEnoughMoney")))
                                                    }
                                                } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CannotBeBought")))
                                            } else {
                                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.PackDoesntExist")))
                                            }
                                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ChoosePack")))
                                    } else if (args[1].equals("card", ignoreCase = true)) {
                                        if (args.size > 2) {
                                            if (args.size > 3) {
                                                if (getCardsData()!!.contains("Cards." + args[2] + "." + args[3])) {
                                                    var buyPrice = 0.0
                                                    var canBuy = false
                                                    if (getCardsData()!!.contains("Cards." + args[2] + "." + args[3] + ".Buy-Price")) {
                                                        buyPrice = getCardsData()!!.getDouble("Cards." + args[2] + "." + args[3] + ".Buy-Price")
                                                        if (buyPrice > 0.0) canBuy = true
                                                    }
                                                    if (canBuy) {
                                                        if (econ!!.getBalance(p) >= buyPrice) {
                                                            if (config.getBoolean("PluginSupport.Vault.Closed-Economy")) {
                                                                econ!!.withdrawPlayer(p, buyPrice)
                                                                econ!!.depositPlayer(config.getString("PluginSupport.Vault.Server-Account"), buyPrice)
                                                            } else {
                                                                econ!!.withdrawPlayer(p, buyPrice)
                                                            }
                                                            if (p.inventory.firstEmpty() != -1) {
                                                                p.inventory.addItem(createPlayerCard(args[3], args[2], 1, false))
                                                            } else {
                                                                val curWorld = p.world
                                                                if (p.gameMode == GameMode.SURVIVAL) {
                                                                    curWorld.dropItem(p.location, createPlayerCard(args[3], args[2], 1, false))
                                                                }
                                                            }
                                                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.BoughtCard")!!.replace("%amount%".toRegex(), buyPrice.toString())))
                                                        } else {
                                                            sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NotEnoughMoney")))
                                                        }
                                                    } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CannotBeBought")))
                                                } else {
                                                    sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.CardDoesntExist")))
                                                }
                                            } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ChooseCard")))
                                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ChooseRarity")))
                                    } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.BuyUsage")))
                                } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.BuyUsage")))
                            } else {
                                sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoVault")))
                            }
                        } else sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoPerms")))
                    } else {
                        sender.sendMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.NoCmd")))
                    }
                }
            } else {
                val showUsage = config.getBoolean("General.Show-Command-Usage", true)
                sender.sendMessage(cMsg(formatTitle(config.getString("General.Server-Name") + " Trading Cards")))
                if (sender.hasPermission("fwtc.reload")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.ReloadUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.ReloadHelp")))
                }
                if (sender.hasPermission("fwtc.givecard")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.GiveCardUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.GiveCardHelp")))
                }
                if (sender.hasPermission("fwtc.giveshinycard")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.GiveShinyCardUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.GiveShinyCardHelp")))
                }
                if (sender.hasPermission("fwtc.giverandomcard")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.GiveRandomCardUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.GiveRandomCardHelp")))
                }
                if (sender.hasPermission("fwtc.giveboosterpack")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.GiveBoosterPackUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.GiveBoosterPackHelp")))
                }
                if (sender.hasPermission("fwtc.giveaway")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.GiveawayUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.GiveawayHelp")))
                }
                if (sender.hasPermission("fwtc.getdeck")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.GetDeckUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.GetDeckHelp")))
                }
                if (sender.hasPermission("fwtc.list")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.ListUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.ListHelp")))
                }
                if (sender.hasPermission("fwtc.listpacks")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.ListPacksUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.ListPacksHelp")))
                }
                if (sender.hasPermission("fwtc.toggle")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.ToggleUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.ToggleHelp")))
                }
                if (sender.hasPermission("fwtc.create")) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.CreateUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.CreateHelp")))
                }
                if (sender.hasPermission("fwtc.buy") && hasVault) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.BuyUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.BuyHelp")))
                }
                if (sender.hasPermission("fwtc.worth") && hasVault) {
                    sender.sendMessage(cMsg("&7> &3" + getMessagesData()!!.getString("Messages.WorthUsage")))
                    if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData()!!.getString("Messages.WorthHelp")))
                }
                return true
            }
        }
        return true
    }

    private fun cMsg(message: String?): String {
        return ChatColor.translateAlternateColorCodes('&', message!!)
    }

    private fun startTimer() {
        val scheduler = Bukkit.getServer().scheduler
        if (scheduler.isQueued(taskid) || scheduler.isCurrentlyRunning(taskid)) {
            scheduler.cancelTask(taskid)
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Successfully cancelled task $taskid")
        }
        val hours = if (config.getInt("General.Schedule-Card-Time-In-Hours") < 1) 1 else config.getInt("General.Schedule-Card-Time-In-Hours")
        val tmessage: String = getMessagesData()!!.getString("Messages.TimerMessage")!!.replace("%hour%".toRegex(), hours.toString())
        Bukkit.broadcastMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + tmessage))
        taskid = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(this, {
            if (this@TradingCards.config.getBoolean("General.Debug-Mode")) println("[Cards] Task running..")
            if (this@TradingCards.config.getBoolean("General.Schedule-Cards")) {
                if (this@TradingCards.config.getBoolean("General.Debug-Mode")) println("[Cards] Schedule cards is true.")
                val rarities = getCardsData()!!.getConfigurationSection("Cards")!!
                val rarityKeys = rarities.getKeys(false)
                var keyToUse = ""
                for (key in rarityKeys) {
                    if (this@TradingCards.config.getBoolean("General.Debug-Mode")) println("[Cards] Rarity key: $key")
                    if (key.equals(this@TradingCards.config.getString("General.Schedule-Card-Rarity"), ignoreCase = true)) {
                        keyToUse = key
                    }
                }
                if (this@TradingCards.config.getBoolean("General.Debug-Mode")) println("[Cards] keyToUse: $keyToUse")
                if (keyToUse != "") {
                    Bukkit.broadcastMessage(cMsg(getMessagesData()!!.getString("Messages.Prefix") + " " + getMessagesData()!!.getString("Messages.ScheduledGiveaway")))
                    for (p in Bukkit.getOnlinePlayers()) {
                        val cards = getCardsData()!!.getConfigurationSection("Cards.$keyToUse")!!
                        val cardKeys = cards.getKeys(false)
                        val rIndex = r.nextInt(cardKeys.size)
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
                            p.inventory.addItem(createPlayerCard(cardName, keyToUse, 1, false))
                        } else {
                            val curWorld = p.world
                            if (p.gameMode == GameMode.SURVIVAL) {
                                curWorld.dropItem(p.location, createPlayerCard(cardName, keyToUse, 1, false))
                            }
                        }
                    }
                }
            }
        }, hours * 20 * 60 * 60.toLong(), hours * 20 * 60 * 60.toLong())
    }

    private fun formatTitle(title: String): String {
        val line = "&7[&foOo&7]&f____________________________________________________&7[&foOo&7]&f"
        val pivot = line.length / 2
        val center = "&7.< &3$title&7 >.&f"
        var out: String = line.substring(0, max(0, pivot - center.length / 2))
        out = out + center + line.substring(pivot + center.length / 2)
        return out
    }

    companion object {
        var permRarities = Permission("fwtc.rarity")
        var econ: Economy? = null
        var perms: Permission? = null
        var chat: Chat? = null
    }
}