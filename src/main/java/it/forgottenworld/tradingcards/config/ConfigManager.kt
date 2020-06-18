package it.forgottenworld.tradingcards.config

import it.forgottenworld.tradingcards.TradingCards

class ConfigManager() {

    lateinit var pluginConfig: Config
        private set
    lateinit var decksConfig: Config
        private set
    lateinit var messagesConfig: Config
        private set
    lateinit var cardsConfig: Config
        private set
    private val plugin = TradingCards.instance

    init {
        reloadAllConfigs()
    }

    fun reloadAllConfigs(){
        reloadPluginConfig()
        reloadDecksConfig()
        reloadMessagesConfig()
        reloadCardsConfig()
    }

    fun reloadPluginConfig(){
        pluginConfig = Config("config.yml", plugin)
    }

    fun reloadDecksConfig() {
        decksConfig = Config("decks.yml", plugin);
    }

    fun reloadMessagesConfig(){
        messagesConfig = Config("messages.yml", plugin)
    }

    fun reloadCardsConfig(){
        cardsConfig = Config("cards.yml", plugin)
    }
}