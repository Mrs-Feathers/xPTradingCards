package it.forgottenworld.tradingcards.config

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
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
    constructor(configName: String, plugin: JavaPlugin) : this(File(plugin.dataFolder, configName), plugin) {}

    /**
     * Saves current config values
     */
    fun save() {
        try {
            config!!.save(configFile)
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Error while saving the config file " + ChatColor.RED + configFile.name)
            e.printStackTrace()
        }
    }

    private fun exists(): Boolean {
        return if (configFile.exists()) {
            logger.info(configFile.name + " has been found")
            true
        } else {
            logger.info(configFile.name + " " +
                    "was not been found")
            false
        }
    }

    @Throws(IOException::class)
    private fun createConfig() {
        logger.info("Creating " + configFile.name + "...")
        if (!configFile.parentFile.exists()) {
            val result = configFile.parentFile.mkdirs()
            if (!result) throw IOException("Error creating " + configFile.parentFile.name)
        }
        if (plugin.getResource(configFile.name) != null) {
            plugin.saveResource(configFile.name, false)
        } else {
            if (!configFile.exists()) {
                val result = configFile.createNewFile()
                if (result) {
                    logger.log(Level.INFO, configFile.name + "has been created")
                } else {
                    logger.log(Level.SEVERE, ChatColor.DARK_RED.toString() + "" + ChatColor.UNDERLINE + "Error creating " + configFile.name)
                    throw IOException("Error creating " + configFile.name)
                }
            }
        }
    }

    private fun loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)
        logger.info(configFile.name + " has been loaded")
    }

    /**
     * Use this constructor in case you want to load an existing file
     *
     * @param configFile The file you want to load
     */
    init {
        if (exists()) {
            loadConfig()
        } else {
            createConfig()
        }
    }
}