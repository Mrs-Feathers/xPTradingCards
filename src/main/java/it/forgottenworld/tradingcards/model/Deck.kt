package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.data.Rarities
import java.util.*

class Deck(val owner: UUID, val cards: List<DeckCardGroup>) {

    data class DeckCardGroup(val card: Card, val amount: Int, val isShiny: Boolean)

    companion object {
        fun deserialize(owner: UUID, serializedCards: List<String>) =
                Deck(owner, serializedCards.map {
                    val s = it.split(",")
                    DeckCardGroup(Rarities[s[0]]!!.cards[s[1]]!!,s[2].toInt(),s[3] == "yes")
                })
    }
}