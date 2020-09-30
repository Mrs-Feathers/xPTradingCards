package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.data.Rarities

class Deck(val cards: List<DeckCardGroup>) {

    data class DeckCardGroup(val card: Card, val amount: Int, val isShiny: Boolean)

    companion object {
        fun deserialize(serializedCards: List<String>) =
                Deck(serializedCards.map {
                    val s = it.split(",")
                    DeckCardGroup(Rarities[s[0]]!!.cards[s[1]]!!,s[2].toInt(),s[3] == "yes")
                })
    }
}