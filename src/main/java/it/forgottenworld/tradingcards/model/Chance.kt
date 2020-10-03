package it.forgottenworld.tradingcards.model

data class Chance(
        val hostile: Int,
        val neutral: Int,
        val passive: Int,
        val boss: Int
)