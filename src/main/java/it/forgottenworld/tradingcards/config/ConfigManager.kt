package it.forgottenworld.tradingcards.config

import it.forgottenworld.tradingcards.TradingCards

object ConfigManager {

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

    fun reloadPluginConfig() {
        pluginConfig = Config("config.yml", TradingCards.instance)
    }

    private fun reloadDecksConfig() {
        decksConfig = Config("decks.yml", TradingCards.instance)
    }

    private fun reloadMessagesConfig() {
        messagesConfig = Config("messages.yml", TradingCards.instance)
    }

    fun reloadCardsConfig() {
        cardsConfig = Config("cards.yml", TradingCards.instance)
    }
}