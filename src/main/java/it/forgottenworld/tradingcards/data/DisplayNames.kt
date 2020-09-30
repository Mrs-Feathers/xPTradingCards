package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config

object DisplayNames {

    //lateinit var Title: String
    lateinit var ShinyTitle: String
    lateinit var Series: String
    lateinit var Type: String
    lateinit var Info: String
    lateinit var About: String
    
    fun load() {
        with(Config.PLUGIN) {
            //Title = getString("DisplayNames.Cards.Title") ?: "%PREFIX%%COLOUR%%NAME%"
            ShinyTitle = getString("DisplayNames.Cards.ShinyTitle") ?: "%PREFIX%%COLOUR%%SHINYPREFIX% %NAME%"
            Series = getString("DisplayNames.Cards.Series") ?: "Series"
            Type = getString("DisplayNames.Cards.Type") ?: "Type"
            Info = getString("DisplayNames.Cards.Info") ?: "Info"
            About = getString("DisplayNames.Cards.About") ?: "About"
        }
    }
}