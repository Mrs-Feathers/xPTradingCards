package it.forgottenworld.tradingcards.config

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.IOException
import java.util.logging.Level

class Config(private val configFile: File, private val plugin: JavaPlugin) {

    private val logger = Bukkit.getLogger()

    /**
     * Returns the FileConfiguration instance
     *
     * @return FileCongifuration
     */
    var config: FileConfiguration? = null
        private set

    /**
     * Use this constructor in case you want to copy an internal file (or template)
     *
     * @param configName Name of the config file
     * @param plugin     Instance of the plugin
     */
    constructor(configName: String, plugin: JavaPlugin) : this(File(plugin.dataFolder, configName), plugin)

    /**
     * Saves current config values
     */
    fun save() {
        try {
            config!!.save(configFile)
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Error while saving the config file ${ChatColor.RED}${configFile.name}")
            e.printStackTrace()
        }
    }

    private fun exists() = configFile.exists().also {
        logger.info("${configFile.name} has ${if (!it) "not " else ""}been found")
    }

    @Throws(IOException::class)
    private fun createConfig() {

        logger.info("Creating ${configFile.name}...")

        if (!configFile.parentFile.exists() && !configFile.parentFile.mkdirs())
            throw IOException("Error creating ${configFile.parentFile.name}")

        if (plugin.getResource(configFile.name) != null)
            plugin.saveResource(configFile.name, false)
        else if (!configFile.exists()) {
            if (configFile.createNewFile())
                logger.log(Level.INFO, "${configFile.name}has been created")
            else {
                logger.log(Level.SEVERE, "${ChatColor.DARK_RED}${ChatColor.UNDERLINE}Error creating ${configFile.name}")
                throw IOException("Error creating ${configFile.name}")
            }
        }
    }

    private fun loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)
        logger.info("${configFile.name} has been loaded")
    }

    /**
     * Use this constructor in case you want to load an existing file
     *
     */
    init {
        if (!exists())
            createConfig()
        loadConfig()
    }

    companion object {

        private lateinit var pluginConfig: Config
        private lateinit var decksConfig: Config
        private lateinit var messagesConfig: Config
        private lateinit var cardsConfig: Config

        val PLUGIN
            get() = pluginConfig.config!!
        val DECKS
            get() = decksConfig.config!!
        val CARDS
            get() = cardsConfig.config!!
        val MESSAGES
            get() = messagesConfig.config!!
        val DEBUG
            get() = General.DebugMode

        fun saveDecksConfig() {
            object: BukkitRunnable() {
                override fun run() {
                    decksConfig.save()
                }
            }.runTaskAsynchronously(TradingCards.instance)
        }

        fun savePluginConfig() {
            object: BukkitRunnable() {
                override fun run() {
                    pluginConfig.save()
                }
            }.runTaskAsynchronously(TradingCards.instance)
        }

        fun saveCardsConfig() {
            object: BukkitRunnable() {
                override fun run() {
                    cardsConfig.save()
                }
            }.runTaskAsynchronously(TradingCards.instance)
        }

        private fun reloadPluginConfig() {
            pluginConfig = Config("config.yml", TradingCards.instance)
        }

        private fun reloadDecksConfig() {
            decksConfig = Config("decks.yml", TradingCards.instance)
        }

        private fun reloadMessagesConfig() {
            messagesConfig = Config("messages.yml", TradingCards.instance)
        }

        private fun reloadCardsConfig() {
            cardsConfig = Config("cards.yml", TradingCards.instance)
        }

        init {
            reloadAllConfigs()
        }

        fun reloadAllConfigs() {
            reloadPluginConfig()
            reloadDecksConfig()
            reloadMessagesConfig()
            reloadCardsConfig()
        }
    }

}