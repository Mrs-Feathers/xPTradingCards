package it.forgottenworld.tradingcards.config

object Messages {

    lateinit var Prefix: String
    lateinit var Reload: String
    lateinit var NoCard: String
    lateinit var NoPerms: String
    lateinit var NoPlayer: String
    lateinit var NoCmd: String
    lateinit var NoEntity: String
    lateinit var NoCreative: String
    lateinit var NoRarity: String
    lateinit var NoBoosterPack: String
    lateinit var ScheduledGiveaway: String
    lateinit var Giveaway: String
    lateinit var GiveRandomCard: String
    lateinit var GiveRandomCardMsg: String
    lateinit var GiveBoosterPackUsage: String
    lateinit var GiveBoosterPackHelp: String
    lateinit var BoosterPackMsg: String
    lateinit var OpenBoosterPack: String
    lateinit var ReloadUsage: String
    lateinit var ReloadHelp: String
    lateinit var ListUsage: String
    lateinit var ListHelp: String
    lateinit var BuyUsage: String
    lateinit var BuyHelp: String
    lateinit var WorthUsage: String
    lateinit var WorthHelp: String
    lateinit var CanBuy: String
    lateinit var CanNotBuy: String
    lateinit var ChooseCard: String
    lateinit var ChooseRarity: String
    lateinit var ChoosePack: String
    lateinit var CannotBeBought: String
    lateinit var NotEnoughMoney: String
    lateinit var BoughtCard: String
    lateinit var NotACard: String
    lateinit var CardDoesntExist: String
    lateinit var PackDoesntExist: String
    lateinit var NoVault: String
    lateinit var GiveCardUsage: String
    lateinit var GiveCardHelp: String
    lateinit var GetDeckUsage: String
    lateinit var GetDeckHelp: String
    lateinit var GiveDeck: String
    lateinit var AlreadyHaveDeck: String
    lateinit var MaxDecks: String
    lateinit var GiveShinyCardUsage: String
    lateinit var GiveShinyCardHelp: String
    lateinit var GiveawayUsage: String
    lateinit var GiveawayHelp: String
    lateinit var GiveRandomCardUsage: String
    lateinit var GiveRandomCardHelp: String
    lateinit var CreateUsage: String
    lateinit var CreateHelp: String
    lateinit var CreateNoName: String
    lateinit var CreateExists: String
    lateinit var CreateSuccess: String
    lateinit var TimerMessage: String
    lateinit var ToggleEnabled: String
    lateinit var ToggleDisabled: String
    lateinit var ToggleUsage: String
    lateinit var ToggleHelp: String
    lateinit var ListPacksUsage: String
    lateinit var ListPacksHelp: String
    
    fun load() {
        val config = Config.MESSAGES
        
        Prefix = config.getString("Messages.Prefix") ?: "ERROR"
        Reload = config.getString("Messages.Reload") ?: "ERROR"
        NoCard = config.getString("Messages.NoCard") ?: "ERROR"
        NoPerms = config.getString("Messages.NoPerms") ?: "ERROR"
        NoPlayer = config.getString("Messages.NoPlayer") ?: "ERROR"
        NoCmd = config.getString("Messages.NoCmd") ?: "ERROR"
        NoEntity = config.getString("Messages.NoEntity") ?: "ERROR"
        NoCreative = config.getString("Messages.NoCreative") ?: "ERROR"
        NoRarity = config.getString("Messages.NoRarity") ?: "ERROR"
        NoBoosterPack = config.getString("Messages.NoBoosterPack") ?: "ERROR"
        ScheduledGiveaway = config.getString("Messages.ScheduledGiveaway") ?: "ERROR"
        Giveaway = config.getString("Messages.Giveaway") ?: "ERROR"
        GiveRandomCard = config.getString("Messages.GiveRandomCard") ?: "ERROR"
        GiveRandomCardMsg = config.getString("Messages.GiveRandomCardMsg") ?: "ERROR"
        GiveBoosterPackUsage = config.getString("Messages.GiveBoosterPackUsage") ?: "ERROR"
        GiveBoosterPackHelp = config.getString("Messages.GiveBoosterPackHelp") ?: "ERROR"
        BoosterPackMsg = config.getString("Messages.BoosterPackMsg") ?: "ERROR"
        OpenBoosterPack = config.getString("Messages.OpenBoosterPack") ?: "ERROR"
        ReloadUsage = config.getString("Messages.ReloadUsage") ?: "ERROR"
        ReloadHelp = config.getString("Messages.ReloadHelp") ?: "ERROR"
        ListUsage = config.getString("Messages.ListUsage") ?: "ERROR"
        ListHelp = config.getString("Messages.ListHelp") ?: "ERROR"
        BuyUsage = config.getString("Messages.BuyUsage") ?: "ERROR"
        BuyHelp = config.getString("Messages.BuyHelp") ?: "ERROR"
        WorthUsage = config.getString("Messages.WorthUsage") ?: "ERROR"
        WorthHelp = config.getString("Messages.WorthHelp") ?: "ERROR"
        CanBuy = config.getString("Messages.CanBuy") ?: "ERROR"
        CanNotBuy = config.getString("Messages.CanNotBuy") ?: "ERROR"
        ChooseCard = config.getString("Messages.ChooseCard") ?: "ERROR"
        ChooseRarity = config.getString("Messages.ChooseRarity") ?: "ERROR"
        ChoosePack = config.getString("Messages.ChoosePack") ?: "ERROR"
        CannotBeBought = config.getString("Messages.CannotBeBought") ?: "ERROR"
        NotEnoughMoney = config.getString("Messages.NotEnoughMoney") ?: "ERROR"
        BoughtCard = config.getString("Messages.BoughtCard") ?: "ERROR"
        NotACard = config.getString("Messages.NotACard") ?: "ERROR"
        CardDoesntExist = config.getString("Messages.CardDoesntExist") ?: "ERROR"
        PackDoesntExist = config.getString("Messages.PackDoesntExist") ?: "ERROR"
        NoVault = config.getString("Messages.NoVault") ?: "ERROR"
        GiveCardUsage = config.getString("Messages.GiveCardUsage") ?: "ERROR"
        GiveCardHelp = config.getString("Messages.GiveCardHelp") ?: "ERROR"
        GetDeckUsage = config.getString("Messages.GetDeckUsage") ?: "ERROR"
        GetDeckHelp = config.getString("Messages.GetDeckHelp") ?: "ERROR"
        GiveDeck = config.getString("Messages.GiveDeck") ?: "ERROR"
        AlreadyHaveDeck = config.getString("Messages.AlreadyHaveDeck") ?: "ERROR"
        MaxDecks = config.getString("Messages.MaxDecks") ?: "ERROR"
        GiveShinyCardUsage = config.getString("Messages.GiveShinyCardUsage") ?: "ERROR"
        GiveShinyCardHelp = config.getString("Messages.GiveShinyCardHelp") ?: "ERROR"
        GiveawayUsage = config.getString("Messages.GiveawayUsage") ?: "ERROR"
        GiveawayHelp = config.getString("Messages.GiveawayHelp") ?: "ERROR"
        GiveRandomCardUsage = config.getString("Messages.GiveRandomCardUsage") ?: "ERROR"
        GiveRandomCardHelp = config.getString("Messages.GiveRandomCardHelp") ?: "ERROR"
        CreateUsage = config.getString("Messages.CreateUsage") ?: "ERROR"
        CreateHelp = config.getString("Messages.CreateHelp") ?: "ERROR"
        CreateNoName = config.getString("Messages.CreateNoName") ?: "ERROR"
        CreateExists = config.getString("Messages.CreateExists") ?: "ERROR"
        CreateSuccess = config.getString("Messages.CreateSuccess") ?: "ERROR"
        TimerMessage = config.getString("Messages.TimerMessage") ?: "ERROR"
        ToggleEnabled = config.getString("Messages.ToggleEnabled") ?: "ERROR"
        ToggleDisabled = config.getString("Messages.ToggleDisabled") ?: "ERROR"
        ToggleUsage = config.getString("Messages.ToggleUsage") ?: "ERROR"
        ToggleHelp = config.getString("Messages.ToggleHelp") ?: "ERROR"
        ListPacksUsage = config.getString("Messages.ListPacksUsage") ?: "ERROR"
        ListPacksHelp = config.getString("Messages.ListPacksHelp") ?: "ERROR"
    }
}