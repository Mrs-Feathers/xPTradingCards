package it.forgottenworld.tradingcards.config

import it.forgottenworld.tradingcards.TradingCards

class ConfigManager(val tradingCards: TradingCards) {

    lateinit var pluginConfig: Config
        private set
    lateinit var decksConfig: Config
        private set
    lateinit var messagesConfig: Config
        private set
    lateinit var cardsConfig: Config
        private set

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
        pluginConfig = Config("config.yml", tradingCards)
    }

    fun reloadDecksConfig() {
        decksConfig = Config("decks.yml", tradingCards);
    }

    fun reloadMessagesConfig(){
        messagesConfig = Config("messages.yml", tradingCards)
    }

    fun reloadCardsConfig(){
        cardsConfig = Config("cards.yml", tradingCards)
    }
}