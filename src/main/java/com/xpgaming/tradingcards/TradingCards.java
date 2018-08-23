/*      */ package com.xpgaming.tradingcards;
/*      */ 
/*      */ import com.garbagemule.MobArena.MobArena;
/*      */ import com.garbagemule.MobArena.framework.Arena;
/*      */ import com.garbagemule.MobArena.framework.ArenaMaster;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Reader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.text.NumberFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.GregorianCalendar;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Random;
/*      */ import java.util.Set;
/*      */ import java.util.UUID;
/*      */ import net.milkbowl.vault.economy.Economy;
/*      */ import org.apache.commons.lang.WordUtils;
/*      */ import org.apache.commons.lang3.StringUtils;
/*      */ import org.bukkit.Bukkit;
/*      */ import org.bukkit.ChatColor;
/*      */ import org.bukkit.GameMode;
/*      */ import org.bukkit.Material;
/*      */ import org.bukkit.OfflinePlayer;
/*      */ import org.bukkit.Server;
/*      */ import org.bukkit.World;
/*      */ import org.bukkit.command.Command;
/*      */ import org.bukkit.command.CommandSender;
/*      */ import org.bukkit.command.PluginCommand;
/*      */ import org.bukkit.configuration.ConfigurationSection;
/*      */ import org.bukkit.configuration.file.FileConfiguration;
/*      */ import org.bukkit.configuration.file.FileConfigurationOptions;
/*      */ import org.bukkit.configuration.file.YamlConfiguration;
/*      */ import org.bukkit.enchantments.Enchantment;
/*      */ import org.bukkit.entity.EntityType;
/*      */ import org.bukkit.entity.LivingEntity;
/*      */ import org.bukkit.entity.Player;
/*      */ import org.bukkit.event.EventHandler;
/*      */ import org.bukkit.event.Listener;
/*      */ import org.bukkit.event.block.Action;
/*      */ import org.bukkit.event.entity.CreatureSpawnEvent;
/*      */ import org.bukkit.event.entity.EntityDeathEvent;
/*      */ import org.bukkit.event.entity.PlayerDeathEvent;
/*      */ import org.bukkit.event.inventory.InventoryCloseEvent;
/*      */ import org.bukkit.event.player.PlayerInteractEvent;
/*      */ import org.bukkit.event.player.PlayerJoinEvent;
/*      */ import org.bukkit.inventory.Inventory;
/*      */ import org.bukkit.inventory.ItemFlag;
/*      */ import org.bukkit.inventory.ItemStack;
/*      */ import org.bukkit.inventory.PlayerInventory;
/*      */ import org.bukkit.inventory.meta.ItemMeta;
/*      */ import org.bukkit.permissions.Permission;
/*      */ import org.bukkit.plugin.PluginManager;
/*      */ import org.bukkit.plugin.RegisteredServiceProvider;
/*      */ import org.bukkit.scheduler.BukkitScheduler;
/*      */ 
/*      */ public class TradingCards extends org.bukkit.plugin.java.JavaPlugin implements Listener, org.bukkit.command.CommandExecutor
/*      */ {
/*   65 */   List<EntityType> hostileMobs = Arrays.asList(new EntityType[] { EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.BLAZE, EntityType.SILVERFISH, EntityType.GHAST, EntityType.SLIME, EntityType.GUARDIAN, EntityType.MAGMA_CUBE, EntityType.WITCH, EntityType.ENDERMITE });
/*   66 */   List<EntityType> neutralMobs = Arrays.asList(new EntityType[] { EntityType.ENDERMAN, EntityType.PIG_ZOMBIE, EntityType.WOLF, EntityType.SNOWMAN, EntityType.IRON_GOLEM });
/*   67 */   List<EntityType> passiveMobs = Arrays.asList(new EntityType[] { EntityType.CHICKEN, EntityType.COW, EntityType.SQUID, EntityType.SHEEP, EntityType.PIG, EntityType.RABBIT, EntityType.VILLAGER, EntityType.BAT, EntityType.HORSE });
/*   68 */   List<EntityType> bossMobs = Arrays.asList(new EntityType[] { EntityType.ENDER_DRAGON, EntityType.WITHER });
/*   69 */   public static Permission permRarities = new Permission("xptc.rarity");
/*      */   boolean hasVault;
/*   71 */   boolean hasMobArena; private FileConfiguration deckData = null;
/*   72 */   private File deckDataFile = null;
/*   73 */   private FileConfiguration messagesData = null;
/*      */   public ArenaMaster am;
/*   75 */   private File messagesDataFile = null;
/*   76 */   private FileConfiguration cardsData = null;
/*   77 */   private File cardsDataFile = null;
/*   78 */   public static Economy econ = null;
/*   79 */   public static Permission perms = null;
/*   80 */   public static net.milkbowl.vault.chat.Chat chat = null;
/*   81 */   Random r = new Random();
/*      */   int taskid;
/*      */   
/*      */   public void onEnable() {
/*   85 */     getConfig().options().copyDefaults(true);
/*   86 */     getServer().getPluginManager().addPermission(permRarities);
/*   87 */     saveDefaultConfig();
/*   88 */     getServer().getPluginManager().registerEvents(this, this);
/*   89 */     getCommand("xptc").setExecutor(this);
/*   90 */     reloadCustomConfig();
/*   91 */     saveDefaultDeckFile();
/*   92 */     reloadDeckData();
/*   93 */     saveDefaultMessagesFile();
/*   94 */     reloadMessagesData();
/*   95 */     saveDefaultCardsFile();
/*   96 */     reloadCardsData();
/*   97 */     if (getConfig().getBoolean("PluginSupport.Towny.Towny-Enabled"))
/*   98 */       if (getServer().getPluginManager().getPlugin("Towny") != null) {
/*   99 */         getServer().getPluginManager().registerEvents(new TownyListener(this), this);
/*  100 */         System.out.println("[xPTradingCards] Towny successfully hooked!");
/*      */       } else {
/*  102 */         System.out.println("[xPTradingCards] Towny not found, hook unsuccessful!");
/*      */       }
/*  104 */     if (getConfig().getBoolean("PluginSupport.Vault.Vault-Enabled"))
/*  105 */       if (getServer().getPluginManager().getPlugin("Vault") != null) {
/*  106 */         setupEconomy();
/*  107 */         System.out.println("[xPTradingCards] Vault hook successful!");
/*  108 */         this.hasVault = true;
/*      */       } else {
/*  110 */         System.out.println("[xPTradingCards] Vault not found, hook unsuccessful!");
/*      */       }
/*  112 */     if (getConfig().getBoolean("PluginSupport.MobArena.MobArena-Enabled"))
/*  113 */       if (getServer().getPluginManager().getPlugin("MobArena") != null) {
/*  114 */         PluginManager pm = getServer().getPluginManager();
/*  115 */         MobArena maPlugin = (MobArena)pm.getPlugin("MobArena");
/*  116 */         this.am = maPlugin.getArenaMaster();
/*  117 */         pm.registerEvents(new MobArenaListener(this), this);
/*  118 */         System.out.println("[xPTradingCards] Mob Arena hook successful!");
/*  119 */         this.hasMobArena = true;
/*      */       } else {
/*  121 */         System.out.println("[xPTradingCards] Mob Arena not found, hook unsuccessful!");
/*      */       }
/*  123 */     if (getConfig().getBoolean("General.Schedule-Cards")) startTimer();
/*      */   }
/*      */   
/*      */   public void onDisable() {
/*  127 */     this.deckData = null;
/*  128 */     this.deckDataFile = null;
/*  129 */     this.messagesData = null;
/*  130 */     this.messagesDataFile = null;
/*  131 */     this.cardsData = null;
/*  132 */     this.cardsDataFile = null;
/*  133 */     econ = null;
/*  134 */     perms = null;
/*  135 */     chat = null;
/*  136 */     getServer().getPluginManager().removePermission(permRarities);
/*      */   }
/*      */   
/*      */   private boolean setupEconomy() {
/*  140 */     if (getServer().getPluginManager().getPlugin("Vault") == null) {
/*  141 */       return false;
/*      */     }
/*  143 */     RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
/*  144 */     if (rsp == null) {
/*  145 */       return false;
/*      */     }
/*  147 */     econ = (Economy)rsp.getProvider();
/*  148 */     return econ != null;
/*      */   }

/*      */   public String formatDouble(double value)
/*      */   {
/*  164 */     NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
/*  165 */     nf.setMaximumFractionDigits(2);
/*  166 */     nf.setMinimumFractionDigits(2);
/*  167 */     return nf.format(value);
/*      */   }
/*      */   
/*      */   public boolean isMobHostile(EntityType e) {
/*  171 */     if (this.hostileMobs.contains(e)) return true;
/*  172 */     return false;
/*      */   }
/*      */   
/*      */   public boolean isMobNeutral(EntityType e) {
/*  176 */     if (this.neutralMobs.contains(e)) return true;
/*  177 */     return false;
/*      */   }
/*      */   
/*      */   public boolean isMobPassive(EntityType e) {
/*  181 */     if (this.passiveMobs.contains(e)) return true;
/*  182 */     return false;
/*      */   }
/*      */   
/*      */   public boolean isMobBoss(EntityType e) {
/*  186 */     if (this.bossMobs.contains(e)) {
/*  187 */       return true;
/*      */     }
/*  189 */     return false;
/*      */   }
/*      */   
/*      */   public ItemStack getBlankCard(int quantity) {
/*  193 */     ItemStack tradingCard = new ItemStack(Material.getMaterial(getConfig().getString("General.Card-Material")), quantity);
/*  194 */     return tradingCard;
/*      */   }
/*      */   
/*      */   public ItemStack getBlankBoosterPack() {
/*  198 */     ItemStack boosterPack = new ItemStack(Material.getMaterial(getConfig().getString("General.BoosterPack-Material")));
/*  199 */     return boosterPack;
/*      */   }
/*      */   
/*      */   public ItemStack getBlankDeck() {
/*  203 */     ItemStack deck = new ItemStack(Material.getMaterial(getConfig().getString("General.Deck-Material")));
/*  204 */     return deck;
/*      */   }
/*      */   
/*      */   public ItemStack createDeck(Player p, int num) {
/*  208 */     ItemStack deck = getBlankDeck();
/*  209 */     ItemMeta deckMeta = deck.getItemMeta();
/*  210 */     deckMeta.setDisplayName(cMsg(getConfig().getString("General.Deck-Prefix") + p.getName() + "'s Deck #" + num));
/*  211 */     if (getConfig().getBoolean("General.Hide-Enchants", true)) deckMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/*  212 */     deck.setItemMeta(deckMeta);
/*  213 */     deck.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
/*  214 */     return deck;
/*      */   }
/*      */   
/*      */   public void reloadDeckData() {
/*  218 */     if (this.deckDataFile == null) {
/*  219 */       this.deckDataFile = new File(getDataFolder(), "decks.yml");
/*      */     }
/*  221 */     this.deckData = YamlConfiguration.loadConfiguration(this.deckDataFile);
/*  222 */     Reader defConfigStream = null;
/*      */     try {
/*  224 */       defConfigStream = new InputStreamReader(getResource("decks.yml"), "UTF8");
/*      */     } catch (UnsupportedEncodingException e) {
/*  226 */       e.printStackTrace();
/*      */     }
/*  228 */     if (defConfigStream != null) {
/*  229 */       YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
/*  230 */       this.deckData.setDefaults(defConfig);
/*      */     }
/*      */   }
/*      */   
/*      */   public FileConfiguration getDeckData() {
/*  235 */     if (this.deckData == null) {
/*  236 */       reloadDeckData();
/*      */     }
/*  238 */     return this.deckData;
/*      */   }
/*      */   
/*      */   public void saveDeckData() {
/*  242 */     if ((this.deckData == null) || (this.deckDataFile == null)) {
/*  243 */       return;
/*      */     }
/*      */     try {
/*  246 */       getDeckData().save(this.deckDataFile);
/*      */     }
/*      */     catch (IOException localIOException) {}
/*      */   }
/*      */   
/*      */   public void saveDefaultDeckFile() {
/*  252 */     if (this.deckDataFile == null) {
/*  253 */       this.deckDataFile = new File(getDataFolder(), "decks.yml");
/*      */     }
/*  255 */     if (!this.deckDataFile.exists()) {
/*  256 */       saveResource("decks.yml", false);
/*      */     }
/*      */   }
/*      */   
/*      */   public void reloadMessagesData() {
/*  261 */     if (this.messagesDataFile == null) {
/*  262 */       this.messagesDataFile = new File(getDataFolder(), "messages.yml");
/*      */     }
/*  264 */     this.messagesData = YamlConfiguration.loadConfiguration(this.messagesDataFile);
/*  265 */     Reader defConfigStream = null;
/*      */     try {
/*  267 */       defConfigStream = new InputStreamReader(getResource("messages.yml"), "UTF8");
/*      */     } catch (UnsupportedEncodingException e) {
/*  269 */       e.printStackTrace();
/*      */     }
/*  271 */     if (defConfigStream != null) {
/*  272 */       YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
/*  273 */       this.messagesData.setDefaults(defConfig);
/*      */     }
/*      */   }
/*      */   
/*      */   public FileConfiguration getMessagesData() {
/*  278 */     if (this.messagesData == null) {
/*  279 */       reloadMessagesData();
/*      */     }
/*  281 */     return this.messagesData;
/*      */   }
/*      */   
/*      */   public void saveMessagesData() {
/*  285 */     if ((this.messagesData == null) || (this.messagesDataFile == null)) {
/*  286 */       return;
/*      */     }
/*      */     try {
/*  289 */       getMessagesData().save(this.messagesDataFile);
/*      */     }
/*      */     catch (IOException localIOException) {}
/*      */   }
/*      */   
/*      */   public void saveDefaultMessagesFile() {
/*  295 */     if (this.messagesDataFile == null) {
/*  296 */       this.messagesDataFile = new File(getDataFolder(), "messages.yml");
/*      */     }
/*  298 */     if (!this.messagesDataFile.exists()) {
/*  299 */       saveResource("messages.yml", false);
/*      */     }
/*      */   }
/*      */   
/*      */   public void reloadCardsData() {
/*  304 */     if (this.cardsDataFile == null) {
/*  305 */       this.cardsDataFile = new File(getDataFolder(), "cards.yml");
/*      */     }
/*  307 */     this.cardsData = YamlConfiguration.loadConfiguration(this.cardsDataFile);
/*  308 */     Reader defConfigStream = null;
/*      */     try {
/*  310 */       defConfigStream = new InputStreamReader(getResource("cards.yml"), "UTF8");
/*      */     } catch (UnsupportedEncodingException e) {
/*  312 */       e.printStackTrace();
/*      */     }
/*  314 */     if (defConfigStream != null) {
/*  315 */       YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
/*  316 */       this.cardsData.setDefaults(defConfig);
/*      */     }
/*      */   }
/*      */   
/*      */   public FileConfiguration getCardsData() {
/*  321 */     if (this.cardsData == null) {
/*  322 */       reloadCardsData();
/*      */     }
/*  324 */     return this.cardsData;
/*      */   }
/*      */   
/*      */   public void saveCardsData() {
/*  328 */     if ((this.cardsData == null) || (this.cardsDataFile == null)) {
/*  329 */       return;
/*      */     }
/*      */     try {
/*  332 */       getCardsData().save(this.cardsDataFile);
/*      */     }
/*      */     catch (IOException localIOException) {}
/*      */   }
/*      */   
/*      */   public void saveDefaultCardsFile() {
/*  338 */     if (this.cardsDataFile == null) {
/*  339 */       this.cardsDataFile = new File(getDataFolder(), "cards.yml");
/*      */     }
/*  341 */     if (!this.cardsDataFile.exists()) {
/*  342 */       saveResource("cards.yml", false);
/*      */     }
/*      */   }
/*      */   
/*      */   public boolean hasDeck(Player p, int num) {
/*  347 */     for (ItemStack i : p.getInventory()) {
/*  348 */       if ((i != null) && 
/*  349 */         (i.getType() == Material.valueOf(getConfig().getString("General.Deck-Material"))) && 
/*  350 */         (i.containsEnchantment(Enchantment.DURABILITY)) && 
/*  351 */         (i.getEnchantmentLevel(Enchantment.DURABILITY) == 10)) {
/*  352 */         String name = i.getItemMeta().getDisplayName();
/*  353 */         String[] splitName = name.split("#");
/*  354 */         if (num == Integer.valueOf(splitName[1]).intValue()) { return true;
/*      */         }
/*      */       }
/*      */     }
/*      */     
/*      */ 
/*  360 */     return false;
/*      */   }
/*      */   
/*      */   public void openDeck(Player p, int deckNum) {
/*  364 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck opened.");
/*  365 */     String uuidString = p.getUniqueId().toString();
/*  366 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck UUID: " + uuidString);
/*  367 */     List<String> contents = getDeckData().getStringList("Decks.Inventories." + uuidString + "." + deckNum);
/*  368 */     List<ItemStack> cards = new ArrayList();
/*  369 */     List<Integer> quantity = new ArrayList();
/*  370 */     ItemStack card = null;
/*  371 */     for (String s : contents) {
/*  372 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck file content: " + s);
/*  373 */       String[] splitContents = s.split(",");
/*  374 */       if (splitContents[3].equalsIgnoreCase("yes")) {
/*  375 */         card = createPlayerCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]), true);
/*      */       } else
/*  377 */         card = getNormalCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]));
/*  378 */       cards.add(card);
/*  379 */       quantity.add(Integer.valueOf(splitContents[2]));
/*  380 */       if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Put " + card + "," + splitContents[2] + " into respective lists.");
/*      */       }
/*      */     }
/*  383 */     Inventory inv = Bukkit.createInventory(null, 27, cMsg("&c" + p.getName() + "'s Deck #" + deckNum));
/*  384 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Created inventory.");
/*  385 */     int iter = 0;
/*  386 */     for (ItemStack i : cards) {
/*  387 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Item " + i.getType().toString() + " added to inventory!");
/*  388 */       i.setAmount(((Integer)quantity.get(iter)).intValue());
/*  389 */       if (inv.contains(i)) {
/*  390 */         i.setAmount(i.getAmount() + 1);
/*      */       } else
/*  392 */         inv.addItem(new ItemStack[] { i });
/*  393 */       iter++;
/*      */     }
/*  395 */     iter = 0;
/*  396 */     p.openInventory(inv);
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void onInventoryClose(InventoryCloseEvent e) {
/*  401 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Title: " + e.getInventory().getTitle());
/*  402 */     if (e.getInventory().getTitle().contains("s Deck #")) {
/*  403 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck closed.");
/*  404 */       ItemStack[] contents = e.getInventory().getContents();
/*  405 */       String[] title = e.getInventory().getTitle().split("'");
/*  406 */       String[] titleNum = e.getInventory().getTitle().split("#");
/*  407 */       int deckNum = Integer.valueOf(titleNum[1]).intValue();
/*  408 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck num: " + deckNum);
/*  409 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Title: " + title[0]);
/*  410 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Title: " + title[1]);
/*  411 */       UUID id = Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])).getUniqueId();
/*  412 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] New ID: " + id.toString());
/*  413 */       List<String> serialized = new ArrayList();
/*  414 */       ItemStack[] arrayOfItemStack1; int j = (arrayOfItemStack1 = contents).length; for (int i = 0; i < j; i++) { ItemStack i = arrayOfItemStack1[i];
/*  415 */         if ((i != null) && (i.getType() != Material.AIR) && 
/*  416 */           (i.getType() == Material.valueOf(getConfig().getString("General.Card-Material")))) {
/*  417 */           if (i.getItemMeta().hasDisplayName()) {
/*  418 */             List<String> lore = i.getItemMeta().getLore();
/*  419 */             String shinyPrefix = getConfig().getString("General.Shiny-Name");
/*  420 */             String rarity = ChatColor.stripColor((String)lore.get(lore.size() - 1)).replaceAll(shinyPrefix + " ", "");
/*  421 */             String card = getCardName(rarity, i.getItemMeta().getDisplayName());
/*  422 */             String amount = String.valueOf(i.getAmount());
/*  423 */             String shiny = "no";
/*  424 */             if (i.containsEnchantment(Enchantment.ARROW_INFINITE)) {
/*  425 */               shiny = "yes";
/*      */             }
/*  427 */             String serializedString = rarity + "," + card + "," + amount + "," + shiny;
/*  428 */             serialized.add(serializedString);
/*  429 */             if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Added " + serializedString + " to deck file.");
/*      */             }
/*      */           }
/*  432 */           else if (Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])).isOnline()) {
/*  433 */             Player p = Bukkit.getPlayer(ChatColor.stripColor(title[0]));
/*  434 */             World w = p.getWorld();
/*  435 */             w.dropItem(p.getLocation(), i);
/*      */           }
/*      */         }
/*      */       }
/*      */       
/*      */ 
/*  441 */       getDeckData().set("Decks.Inventories." + id.toString() + "." + deckNum, serialized);
/*  442 */       saveDeckData();
/*      */     }
/*      */   }
/*      */   
/*      */   public String getCardName(String rarity, String display) {
/*  447 */     boolean hasPrefix = false;
/*  448 */     String prefix = "";
/*  449 */     if ((getConfig().contains("General.Card-Prefix")) && (getConfig().getString("General.Card-Prefix") != "")) {
/*  450 */       hasPrefix = true;
/*  451 */       prefix = ChatColor.stripColor(getConfig().getString("General.Card-Prefix"));
/*      */     }
/*  453 */     String shinyPrefix = getConfig().getString("General.Shiny-Name");
/*  454 */     String cleaned = ChatColor.stripColor(display);
/*  455 */     if (hasPrefix) cleaned = cleaned.replaceAll(prefix, "");
/*  456 */     cleaned = cleaned.replaceAll(shinyPrefix + " ", "");
/*  457 */     String[] cleanedArray = cleaned.split(" ");
/*  458 */     ConfigurationSection cs = getCardsData().getConfigurationSection("Cards." + rarity);
/*  459 */     Set<String> keys = cs.getKeys(false);
/*  460 */     for (String s : keys) {
/*  461 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] getCardName s: " + s);
/*  462 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] getCardName display: " + display);
/*  463 */       if (cleanedArray.length > 1) {
/*  464 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] cleanedArray > 1");
/*  465 */         if ((cleanedArray[0] + "_" + cleanedArray[1]).matches(".*\\b" + s + "\\b.*"))
/*  466 */           return s;
/*  467 */         if ((cleanedArray.length > 2) && ((cleanedArray[1] + "_" + cleanedArray[2]).matches(".*\\b" + s + "\\b.*")))
/*  468 */           return s;
/*  469 */         if ((cleanedArray.length > 3) && ((cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3]).matches(".*\\b" + s + "\\b.*")))
/*  470 */           return s;
/*  471 */         if ((cleanedArray.length > 4) && ((cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4]).matches(".*\\b" + s + "\\b.*")))
/*  472 */           return s;
/*  473 */         if ((cleanedArray.length > 5) && ((cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4] + "_" + cleanedArray[5]).matches(".*\\b" + s + "\\b.*")))
/*  474 */           return s;
/*  475 */         if (cleanedArray[0].matches(".*\\b" + s + "\\b.*"))
/*  476 */           return s;
/*  477 */         if (cleanedArray[1].matches(".*\\b" + s + "\\b.*")) {
/*  478 */           return s;
/*      */         }
/*      */       }
/*  481 */       else if (cleanedArray[0].matches(".*\\b" + s + "\\b.*")) {
/*  482 */         return s;
/*      */       }
/*      */     }
/*  485 */     return "None";
/*      */   }
/*      */   
/*      */   public ItemStack createBoosterPack(String name) {
/*  489 */     ItemStack boosterPack = getBlankBoosterPack();
/*  490 */     String packName = name;
/*  491 */     int numNormalCards = getConfig().getInt("BoosterPacks." + name + ".NumNormalCards");int numSpecialCards = getConfig().getInt("BoosterPacks." + name + ".NumSpecialCards");
/*  492 */     String prefix = getConfig().getString("General.BoosterPack-Prefix");
/*  493 */     String normalCardColour = getConfig().getString("Colours.BoosterPackNormalCards");
/*  494 */     String extraCardColour = getConfig().getString("Colours.BoosterPackExtraCards");
/*  495 */     String loreColour = getConfig().getString("Colours.BoosterPackLore");
/*  496 */     String nameColour = getConfig().getString("Colours.BoosterPackName");
/*  497 */     String normalRarity = getConfig().getString("BoosterPacks." + name + ".NormalCardRarity");
/*  498 */     String specialRarity = getConfig().getString("BoosterPacks." + name + ".SpecialCardRarity");
/*  499 */     String extraRarity = "";int numExtraCards = 0;
/*  500 */     boolean hasExtraRarity = false;
/*  501 */     if ((getConfig().contains("BoosterPacks." + name + ".ExtraCardRarity")) && (getConfig().contains("BoosterPacks." + name + ".NumExtraCards"))) {
/*  502 */       hasExtraRarity = true;
/*  503 */       extraRarity = getConfig().getString("BoosterPacks." + name + ".ExtraCardRarity");
/*  504 */       numExtraCards = getConfig().getInt("BoosterPacks." + name + ".NumExtraCards");
/*      */     }
/*  506 */     String specialCardColour = getConfig().getString("Colours.BoosterPackSpecialCards");
/*  507 */     ItemMeta pMeta = boosterPack.getItemMeta();
/*  508 */     pMeta.setDisplayName(cMsg(prefix + nameColour + packName.replaceAll("_", " ")));
/*  509 */     List<String> lore = new ArrayList();
/*  510 */     lore.add(cMsg(normalCardColour + numNormalCards + loreColour + " " + normalRarity.toUpperCase()));
/*  511 */     if (hasExtraRarity) lore.add(cMsg(extraCardColour + numExtraCards + loreColour + " " + extraRarity.toUpperCase()));
/*  512 */     lore.add(cMsg(specialCardColour + numSpecialCards + loreColour + " " + specialRarity.toUpperCase()));
/*  513 */     pMeta.setLore(lore);
/*  514 */     if (getConfig().getBoolean("General.Hide-Enchants", true)) pMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/*  515 */     boosterPack.setItemMeta(pMeta);
/*  516 */     boosterPack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
/*  517 */     return boosterPack;
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void onPlayerInteract(PlayerInteractEvent event) {
/*  522 */     if ((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
/*  523 */       Player p = event.getPlayer();
/*  524 */       if ((p.getItemInHand().getType() == Material.valueOf(getConfig().getString("General.BoosterPack-Material"))) && 
/*  525 */         (event.getPlayer().hasPermission("xptc.openboosterpack"))) {
/*  526 */         if (p.getGameMode() != GameMode.CREATIVE) {
/*  527 */           if (p.getItemInHand().containsEnchantment(Enchantment.ARROW_INFINITE)) {
/*  528 */             if (p.getItemInHand().getAmount() > 1) p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1); else
/*  529 */               p.getInventory().removeItem(new ItemStack[] { p.getItemInHand() });
/*  530 */             ItemStack boosterPack = event.getItem();
/*  531 */             ItemMeta packMeta = boosterPack.getItemMeta();
/*  532 */             List<String> lore = packMeta.getLore();
/*  533 */             boolean hasExtra = false;
/*  534 */             if (lore.size() > 2) hasExtra = true;
/*  535 */             String[] line1 = ((String)lore.get(0)).split(" ", 2);
/*  536 */             String[] line2 = ((String)lore.get(1)).split(" ", 2);
/*  537 */             String[] line3 = { "" };
/*  538 */             if (hasExtra) line3 = ((String)lore.get(2)).split(" ", 2);
/*  539 */             int normalCardAmount = Integer.valueOf(ChatColor.stripColor(line1[0])).intValue();
/*  540 */             int specialCardAmount = Integer.valueOf(ChatColor.stripColor(line2[0])).intValue();
/*  541 */             int extraCardAmount = 0;
/*  542 */             if (hasExtra) extraCardAmount = Integer.valueOf(ChatColor.stripColor(line3[0])).intValue();
/*  543 */             p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.OpenBoosterPack")));
/*  544 */             for (int i = 0; i < normalCardAmount; i++) {
/*  545 */               if (p.getInventory().firstEmpty() != -1) {
/*  546 */                 p.getInventory().addItem(new ItemStack[] { generateCard(WordUtils.capitalizeFully(line1[1]), false) });
/*      */               }
/*      */               else {
/*  549 */                 World curWorld = p.getWorld();
/*  550 */                 if (p.getGameMode() == GameMode.SURVIVAL) {
/*  551 */                   curWorld.dropItem(p.getLocation(), generateCard(WordUtils.capitalizeFully(line1[1]), false));
/*      */                 }
/*      */               }
/*      */             }
/*  555 */             for (int i = 0; i < specialCardAmount; i++) {
/*  556 */               if (p.getInventory().firstEmpty() != -1) {
/*  557 */                 p.getInventory().addItem(new ItemStack[] { generateCard(WordUtils.capitalizeFully(line2[1]), false) });
/*      */               }
/*      */               else {
/*  560 */                 World curWorld = p.getWorld();
/*  561 */                 if (p.getGameMode() == GameMode.SURVIVAL) {
/*  562 */                   curWorld.dropItem(p.getLocation(), generateCard(WordUtils.capitalizeFully(line2[1]), false));
/*      */                 }
/*      */               }
/*      */             }
/*  566 */             if (hasExtra) for (int i = 0; i < extraCardAmount; i++) {
/*  567 */                 if (p.getInventory().firstEmpty() != -1) {
/*  568 */                   p.getInventory().addItem(new ItemStack[] { generateCard(WordUtils.capitalizeFully(line3[1]), false) });
/*      */                 }
/*      */                 else {
/*  571 */                   World curWorld = p.getWorld();
/*  572 */                   if (p.getGameMode() == GameMode.SURVIVAL) {
/*  573 */                     curWorld.dropItem(p.getLocation(), generateCard(WordUtils.capitalizeFully(line3[1]), false));
/*      */                   }
/*      */                 }
/*      */               }
/*      */           }
/*      */         }
/*      */         else
/*  580 */           event.getPlayer().sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCreative")));
/*      */       }
/*  582 */       if (p.getItemInHand().getType() == Material.valueOf(getConfig().getString("General.Deck-Material"))) {
/*  583 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck material...");
/*  584 */         if (p.getGameMode() != GameMode.CREATIVE) {
/*  585 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Not creative...");
/*  586 */           if (p.getItemInHand().containsEnchantment(Enchantment.DURABILITY)) {
/*  587 */             if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Has enchant...");
/*  588 */             if (p.getItemInHand().getEnchantmentLevel(Enchantment.DURABILITY) == 10) {
/*  589 */               if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Enchant is level 10...");
/*  590 */               String name = p.getItemInHand().getItemMeta().getDisplayName();
/*  591 */               String[] nameSplit = name.split("#");
/*  592 */               int num = Integer.valueOf(nameSplit[1]).intValue();
/*  593 */               openDeck(p, num);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public String calculateRarity(EntityType e, boolean alwaysDrop)
/*      */   {
/*  603 */     int shouldItDrop = this.r.nextInt(100) + 1;int bossRarity = 0;
/*  604 */     String type = "";
/*  605 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] shouldItDrop Num: " + shouldItDrop);
/*  606 */     if (isMobHostile(e)) { if (!alwaysDrop) { if (shouldItDrop > getConfig().getInt("Chances.Hostile-Chance")) return "None"; type = "Hostile"; } else { type = "Hostile";
/*  607 */       } } else if (isMobNeutral(e)) { if (!alwaysDrop) { if (shouldItDrop > getConfig().getInt("Chances.Neutral-Chance")) return "None"; type = "Neutral"; } else { type = "Neutral";
/*  608 */       } } else if (isMobPassive(e)) { if (!alwaysDrop) { if (shouldItDrop > getConfig().getInt("Chances.Passive-Chance")) return "None"; type = "Passive"; } else { type = "Passive";
/*  609 */       } } else if (isMobBoss(e)) {
/*  610 */       if (!alwaysDrop) {
/*  611 */         if (shouldItDrop > getConfig().getInt("Chances.Boss-Chance")) return "None";
/*  612 */         if (getConfig().getBoolean("Chances.Boss-Drop")) bossRarity = getConfig().getInt("Chances.Boss-Drop-Rarity");
/*  613 */         type = "Boss";
/*  614 */       } else { type = "Boss";
/*      */       }
/*  616 */     } else return "None";
/*  617 */     ConfigurationSection rarities = getConfig().getConfigurationSection("Rarities");
/*  618 */     Set<String> rarityKeys = rarities.getKeys(false);
/*  619 */     Map<String, Integer> rarityChances = new HashMap();
/*  620 */     Map<Integer, String> rarityIndexes = new HashMap();
/*  621 */     int i = 0;int mini = 0;int random = this.r.nextInt(100000) + 1;
/*  622 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Random Card Num: " + random);
/*  623 */     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Type: " + type);
/*  624 */     for (String key : rarityKeys) {
/*  625 */       rarityIndexes.put(Integer.valueOf(i), key);
/*  626 */       i++;
/*  627 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + i + ", " + key);
/*  628 */       if ((getConfig().contains("Chances." + key + "." + StringUtils.capitalize(e.getName()))) && (mini == 0)) {
/*  629 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mini: " + i);
/*  630 */         mini = i;
/*      */       }
/*  632 */       int chance = getConfig().getInt("Chances." + key + "." + type, -1);
/*  633 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Keys: " + key + ", " + chance + ", i=" + i);
/*  634 */       rarityChances.put(key, Integer.valueOf(chance));
/*      */     }
/*      */     
/*  637 */     if (mini != 0)
/*      */     {
/*  639 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mini: " + mini);
/*  640 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] i: " + i);
/*  641 */       while (i >= mini) {
/*  642 */         i--;
/*  643 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] i: " + i);
/*  644 */         chance = getConfig().getInt("Chances." + (String)rarityIndexes.get(Integer.valueOf(i)) + "." + StringUtils.capitalize(e.getName()), -1);
/*  645 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Chance: " + chance);
/*  646 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Rarity: " + (String)rarityIndexes.get(Integer.valueOf(i)));
/*  647 */         if (chance > 0) {
/*  648 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Chance > 0");
/*  649 */           if (random <= chance) {
/*  650 */             if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Random <= Chance");
/*  651 */             return (String)rarityIndexes.get(Integer.valueOf(i));
/*      */           }
/*      */           
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  659 */       while (i > 0) { int chance;
/*  660 */         i--;
/*  661 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Final loop iteration " + i);
/*  662 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Iteration " + i + " in HashMap is: " + (String)rarityIndexes.get(Integer.valueOf(i)) + ", " + getConfig().getString(new StringBuilder("Rarities.").append((String)rarityIndexes.get(Integer.valueOf(i))).append(".Name").toString()));
/*  663 */         int chance = getConfig().getInt("Chances." + (String)rarityIndexes.get(Integer.valueOf(i)) + "." + type, -1);
/*  664 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + getConfig().getString(new StringBuilder("Rarities.").append((String)rarityIndexes.get(Integer.valueOf(i))).append(".Name").toString()) + "'s chance of dropping: " + chance + " out of 100,000");
/*  665 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] The random number we're comparing that against is: " + random);
/*  666 */         if ((chance > 0) && 
/*  667 */           (random <= chance)) {
/*  668 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Yup, looks like " + random + " is definitely lower than " + chance + "!");
/*  669 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Giving a " + getConfig().getString(new StringBuilder("Rarities.").append((String)rarityIndexes.get(Integer.valueOf(i))).append(".Name").toString()) + " card.");
/*  670 */           return (String)rarityIndexes.get(Integer.valueOf(i));
/*      */         }
/*      */       }
/*      */     }
/*      */     
/*  675 */     return "None";
/*      */   }
/*      */   
/*      */   public boolean isOnList(Player p) {
/*  679 */     List<String> playersOnList = getConfig().getStringList("Blacklist.Players");
/*  680 */     if (playersOnList.contains(p.getName())) {
/*  681 */       return true;
/*      */     }
/*  683 */     return false;
/*      */   }
/*      */   
/*      */   public void addToList(Player p) {
/*  687 */     List<String> playersOnList = getConfig().getStringList("Blacklist.Players");
/*  688 */     playersOnList.add(p.getName());
/*  689 */     getConfig().set("Blacklist.Players", null);
/*  690 */     getConfig().set("Blacklist.Players", playersOnList);
/*  691 */     saveConfig();
/*      */   }
/*      */   
/*      */   public void removeFromList(Player p) {
/*  695 */     List<String> playersOnList = getConfig().getStringList("Blacklist.Players");
/*  696 */     playersOnList.remove(p.getName());
/*  697 */     getConfig().set("Blacklist.Players", null);
/*  698 */     getConfig().set("Blacklist.Players", playersOnList);
/*  699 */     saveConfig();
/*      */   }
/*      */   
/*      */   public char blacklistMode() {
/*  703 */     if (getConfig().getBoolean("Blacklist.Whitelist-Mode")) return 'w';
/*  704 */     return 'b';
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void onEntityDeath(EntityDeathEvent e) {
/*  709 */     boolean drop = false;
/*  710 */     String worldName = "";
/*  711 */     List<String> worlds = new ArrayList();
/*  712 */     if ((e.getEntity().getKiller() instanceof Player)) {
/*  713 */       Player p = e.getEntity().getKiller();
/*  714 */       if ((isOnList(p)) && (blacklistMode() == 'b')) { drop = false;
/*  715 */       } else if ((!isOnList(p)) && (blacklistMode() == 'b')) { drop = true;
/*  716 */       } else if ((isOnList(p)) && (blacklistMode() == 'w')) drop = true; else
/*  717 */         drop = false;
/*  718 */       worldName = p.getWorld().getName();
/*  719 */       worlds = getConfig().getStringList("World-Blacklist");
/*  720 */       if (this.hasMobArena) {
/*  721 */         int i = 0;
/*  722 */         if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mob Arena checks starting.");
/*  723 */         if ((this.am.getArenas() != null) && (!this.am.getArenas().isEmpty())) {
/*  724 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] There is at least 1 arena!");
/*  725 */           for (Arena arena : this.am.getArenas()) {
/*  726 */             i++;
/*  727 */             if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] For arena #" + i + "...");
/*  728 */             if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] In arena?: " + arena.inArena(p));
/*  729 */             if ((arena.inArena(p)) || (arena.inLobby(p))) {
/*  730 */               if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Killer is in an arena/lobby, so let's mess with the drops.");
/*  731 */               if (getConfig().getBoolean("PluginSupport.MobArena.Disable-In-Arena")) drop = false;
/*  732 */               if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Drops are now: " + drop);
/*      */             }
/*  734 */             else if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Killer is not in this arena!");
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     
/*  741 */     if ((drop) && 
/*  742 */       (!worlds.contains(worldName))) {
/*  743 */       String rare = calculateRarity(e.getEntityType(), false);
/*  744 */       if ((getConfig().getBoolean("Chances.Boss-Drop")) && (isMobBoss(e.getEntityType()))) rare = getConfig().getString("Chances.Boss-Drop-Rarity");
/*  745 */       boolean cancelled = false;
/*  746 */       if (rare != "None") {
/*  747 */         if ((getConfig().getBoolean("General.Spawner-Block")) && 
/*  748 */           (e.getEntity().getCustomName() != null) && 
/*  749 */           (e.getEntity().getCustomName().equals(getConfig().getString("General.Spawner-Mob-Name"))))
/*      */         {
/*  751 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mob came from spawner, not dropping card.");
/*  752 */           cancelled = true;
/*      */         }
/*      */         
/*      */ 
/*  756 */         if (!cancelled)
/*      */         {
/*  758 */           if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Successfully generated card.");
/*  759 */           if (generateCard(rare, false) != null) e.getDrops().add(generateCard(rare, false));
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void onPlayerDeath(PlayerDeathEvent e)
/*      */   {
/*  768 */     if ((getConfig().getBoolean("General.Player-Drops-Card")) && (getConfig().getBoolean("General.Auto-Add-Players")))
/*      */     {
/*  770 */       org.bukkit.entity.Entity killer = e.getEntity().getKiller();
/*  771 */       if ((killer != null) && ((killer instanceof Player))) {
/*  772 */         ConfigurationSection rarities = getConfig().getConfigurationSection("Rarities");
/*  773 */         Set<String> rarityKeys = rarities.getKeys(false);
/*  774 */         String k = null;
/*  775 */         for (String key : rarityKeys) {
/*  776 */           if (getCardsData().contains("Cards." + key + "." + e.getEntity().getName()))
/*      */           {
/*  778 */             if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + key);
/*  779 */             k = key;
/*      */           }
/*      */         }
/*  782 */         if (k != null)
/*      */         {
/*  784 */           int rndm = this.r.nextInt(100) + 1;
/*  785 */           if (rndm <= getConfig().getInt("General.Player-Drops-Card-Rarity")) {
/*  786 */             e.getDrops().add(createPlayerCard(e.getEntity().getName(), k, Integer.valueOf(1), false));
/*  787 */             if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + e.getDrops().toString());
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  792 */           System.out.println("k is null");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public ItemStack generateCard(String rare, boolean forcedShiny) {
/*  799 */     if (!rare.equals("None")) {
/*  800 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] generateCard.rare: " + rare);
/*  801 */       ItemStack card = getBlankCard(1);
/*  802 */       reloadCustomConfig();
/*  803 */       ConfigurationSection cardSection = getCardsData().getConfigurationSection("Cards." + rare);
/*  804 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] generateCard.cardSection: " + getCardsData().contains(new StringBuilder("Cards.").append(rare).toString()));
/*  805 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] generateCard.rarity: " + rare);
/*  806 */       Set<String> cards = cardSection.getKeys(false);
/*  807 */       List<String> cardNames = new ArrayList();
/*  808 */       cardNames.addAll(cards);
/*  809 */       int cIndex = this.r.nextInt(cardNames.size());
/*  810 */       String cardName = (String)cardNames.get(cIndex);
/*  811 */       boolean hasShinyVersion = getCardsData().getBoolean("Cards." + rare + "." + cardName + ".Has-Shiny-Version");boolean isShiny = false;
/*  812 */       if (hasShinyVersion) {
/*  813 */         int shinyRandom = this.r.nextInt(100) + 1;
/*  814 */         if (shinyRandom <= getConfig().getInt("Chances.Shiny-Version-Chance")) isShiny = true;
/*      */       }
/*  816 */       if (forcedShiny) isShiny = true;
/*  817 */       String rarityName = rare;
/*  818 */       String rarityColour = getConfig().getString("Rarities." + rare + ".Colour");
/*  819 */       String prefix = getConfig().getString("General.Card-Prefix");
/*  820 */       String series = getCardsData().getString("Cards." + rare + "." + cardName + ".Series");
/*  821 */       String seriesColour = getConfig().getString("Colours.Series");
/*  822 */       String seriesDisplay = getConfig().getString("DisplayNames.Cards.Series", "Series");
/*  823 */       String about = getCardsData().getString("Cards." + rare + "." + cardName + ".About", "None");
/*  824 */       String aboutColour = getConfig().getString("Colours.About");
/*  825 */       String aboutDisplay = getConfig().getString("DisplayNames.Cards.About", "About");
/*  826 */       String type = getCardsData().getString("Cards." + rare + "." + cardName + ".Type");
/*  827 */       String typeColour = getConfig().getString("Colours.Type");
/*  828 */       String typeDisplay = getConfig().getString("DisplayNames.Cards.Type", "Type");
/*  829 */       String info = getCardsData().getString("Cards." + rare + "." + cardName + ".Info");
/*  830 */       String infoColour = getConfig().getString("Colours.Info");
/*  831 */       String infoDisplay = getConfig().getString("DisplayNames.Cards.Info", "Info");
/*  832 */       String shinyPrefix = getConfig().getString("General.Shiny-Name");
/*      */       String cost;
/*  834 */       String cost; if (getCardsData().contains("Cards." + rare + "." + cardName + ".Buy-Price"))
/*  835 */         cost = String.valueOf(getCardsData().getDouble("Cards." + rare + "." + cardName + ".Buy-Price")); else
/*  836 */         cost = "None";
/*  837 */       ItemMeta cmeta = card.getItemMeta();
/*      */       
/*  839 */       if (isShiny) { cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.ShinyTitle").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("%SHINYPREFIX%", shinyPrefix).replaceAll("_", " ")));
/*      */       } else
/*  841 */         cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.Title").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("_", " ")));
/*  842 */       List<String> lore = new ArrayList();
/*  843 */       lore.add(cMsg(typeColour + typeDisplay + ": &f" + type));
/*  844 */       if ((info.equals("None")) || (info.equals(""))) {
/*  845 */         lore.add(cMsg(infoColour + infoDisplay + ": &f" + info));
/*      */       }
/*      */       else
/*      */       {
/*  849 */         lore.add(cMsg(infoColour + infoDisplay + ":"));
/*  850 */         lore.addAll(wrapString(info));
/*      */       }
/*  852 */       lore.add(cMsg(seriesColour + seriesDisplay + ": &f" + series));
/*  853 */       if (getCardsData().contains("Cards." + rare + "." + cardName + ".About")) lore.add(cMsg(aboutColour + aboutDisplay + ": &f" + about));
/*  854 */       if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + getConfig().getString("General.Shiny-Name") + " " + rarityName)); else
/*  855 */         lore.add(cMsg(rarityColour + ChatColor.BOLD + rarityName));
/*  856 */       cmeta.setLore(lore);
/*  857 */       if (getConfig().getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/*  858 */       card.setItemMeta(cmeta);
/*  859 */       if (isShiny) {
/*  860 */         card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
/*      */       }
/*  862 */       return card; }
/*  863 */     return null;
/*      */   }
/*      */   
/*      */   public List<String> wrapString(String s) {
/*  867 */     String parsedString = ChatColor.stripColor(s);
/*  868 */     String addedString = WordUtils.wrap(parsedString, getConfig().getInt("General.Info-Line-Length", 25), "\n", true);
/*  869 */     String[] splitString = addedString.split("\n");
/*  870 */     List<String> finalArray = new ArrayList();
/*  871 */     String[] arrayOfString1; int j = (arrayOfString1 = splitString).length; for (int i = 0; i < j; i++) { String ss = arrayOfString1[i];
/*  872 */       System.out.println(ChatColor.getLastColors(ss));
/*  873 */       finalArray.add(cMsg("&f &7- &f" + ss));
/*      */     }
/*  875 */     return finalArray;
/*      */   }
/*      */   
/*      */   public String[] splitStringEvery(String s, int interval) {
/*  879 */     int arrayLength = (int)Math.ceil(s.length() / interval);
/*  880 */     String[] result = new String[arrayLength];
/*      */     
/*  882 */     int j = 0;
/*  883 */     int lastIndex = result.length - 1;
/*  884 */     for (int i = 0; i < lastIndex; i++) {
/*  885 */       result[i] = s.substring(j, j + interval);
/*  886 */       j += interval;
/*      */     }
/*  888 */     result[lastIndex] = s.substring(j);
/*      */     
/*  890 */     return result;
/*      */   }
/*      */   
/*      */   public ItemStack createPlayerCard(String cardName, String rarity, Integer num, boolean forcedShiny) {
/*  894 */     ItemStack card = getBlankCard(num.intValue());
/*  895 */     boolean hasShinyVersion = getCardsData().getBoolean("Cards." + rarity + "." + cardName + ".Has-Shiny-Version");boolean isShiny = false;
/*  896 */     if (hasShinyVersion) {
/*  897 */       int shinyRandom = this.r.nextInt(100) + 1;
/*  898 */       if (shinyRandom <= getConfig().getInt("Chances.Shiny-Version-Chance")) isShiny = true;
/*      */     }
/*  900 */     if (forcedShiny) isShiny = true;
/*  901 */     String rarityName = rarity;
/*  902 */     String rarityColour = getConfig().getString("Rarities." + rarity + ".Colour");
/*  903 */     String prefix = getConfig().getString("General.Card-Prefix");
/*  904 */     String series = getCardsData().getString("Cards." + rarity + "." + cardName + ".Series");
/*  905 */     String seriesColour = getConfig().getString("Colours.Series");
/*  906 */     String seriesDisplay = getConfig().getString("DisplayNames.Cards.Series", "Series");
/*  907 */     String about = getCardsData().getString("Cards." + rarity + "." + cardName + ".About", "None");
/*  908 */     String aboutColour = getConfig().getString("Colours.About");
/*  909 */     String aboutDisplay = getConfig().getString("DisplayNames.Cards.About", "About");
/*  910 */     String type = getCardsData().getString("Cards." + rarity + "." + cardName + ".Type");
/*  911 */     String typeColour = getConfig().getString("Colours.Type");
/*  912 */     String typeDisplay = getConfig().getString("DisplayNames.Cards.Type", "Type");
/*  913 */     String info = getCardsData().getString("Cards." + rarity + "." + cardName + ".Info");
/*  914 */     String infoColour = getConfig().getString("Colours.Info");
/*  915 */     String infoDisplay = getConfig().getString("DisplayNames.Cards.Info", "Info");
/*  916 */     String shinyPrefix = getConfig().getString("General.Shiny-Name");
/*      */     String cost;
/*  918 */     String cost; if (getCardsData().contains("Cards." + rarity + "." + cardName + ".Buy-Price"))
/*  919 */       cost = String.valueOf(getCardsData().getDouble("Cards." + rarity + "." + cardName + ".Buy-Price")); else
/*  920 */       cost = "None";
/*  921 */     ItemMeta cmeta = card.getItemMeta();
/*      */     
/*  923 */     if (isShiny) { cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.ShinyTitle").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("%SHINYPREFIX%", shinyPrefix).replaceAll("_", " ")));
/*      */     } else
/*  925 */       cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.Title").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("_", " ")));
/*  926 */     List<String> lore = new ArrayList();
/*  927 */     lore.add(cMsg(typeColour + typeDisplay + ": &f" + type));
/*  928 */     if ((info.equals("None")) || (info.equals(""))) {
/*  929 */       lore.add(cMsg(infoColour + infoDisplay + ": &f" + info));
/*      */     }
/*      */     else
/*      */     {
/*  933 */       lore.add(cMsg(infoColour + infoDisplay + ":"));
/*  934 */       lore.addAll(wrapString(info));
/*      */     }
/*  936 */     lore.add(cMsg(seriesColour + seriesDisplay + ": &f" + series));
/*  937 */     if (getCardsData().contains("Cards." + rarity + "." + cardName + ".About")) lore.add(cMsg(aboutColour + aboutDisplay + ": &f" + about));
/*  938 */     if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + getConfig().getString("General.Shiny-Name") + " " + rarityName)); else
/*  939 */       lore.add(cMsg(rarityColour + ChatColor.BOLD + rarityName));
/*  940 */     cmeta.setLore(lore);
/*  941 */     if (getConfig().getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/*  942 */     card.setItemMeta(cmeta);
/*  943 */     if (isShiny) {
/*  944 */       card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
/*      */     }
/*  946 */     return card;
/*      */   }
/*      */   
/*      */   public ItemStack getNormalCard(String cardName, String rarity, Integer num) {
/*  950 */     ItemStack card = getBlankCard(num.intValue());
/*  951 */     String rarityName = rarity;
/*  952 */     String rarityColour = getConfig().getString("Rarities." + rarity + ".Colour");
/*  953 */     String prefix = getConfig().getString("General.Card-Prefix");
/*  954 */     String series = getCardsData().getString("Cards." + rarity + "." + cardName + ".Series");
/*  955 */     String seriesColour = getConfig().getString("Colours.Series");
/*  956 */     String seriesDisplay = getConfig().getString("DisplayNames.Cards.Series", "Series");
/*  957 */     String about = getCardsData().getString("Cards." + rarity + "." + cardName + ".About", "None");
/*  958 */     String aboutColour = getConfig().getString("Colours.About");
/*  959 */     String aboutDisplay = getConfig().getString("DisplayNames.Cards.About", "About");
/*  960 */     String type = getCardsData().getString("Cards." + rarity + "." + cardName + ".Type");
/*  961 */     String typeColour = getConfig().getString("Colours.Type");
/*  962 */     String typeDisplay = getConfig().getString("DisplayNames.Cards.Type", "Type");
/*  963 */     String info = getCardsData().getString("Cards." + rarity + "." + cardName + ".Info");
/*  964 */     String infoColour = getConfig().getString("Colours.Info");
/*  965 */     String infoDisplay = getConfig().getString("DisplayNames.Cards.Info", "Info");
/*      */     String cost;
/*  967 */     String cost; if (getCardsData().contains("Cards." + rarity + "." + cardName + ".Buy-Price"))
/*  968 */       cost = String.valueOf(getCardsData().getDouble("Cards." + rarity + "." + cardName + ".Buy-Price")); else
/*  969 */       cost = "None";
/*  970 */     ItemMeta cmeta = card.getItemMeta();
/*      */     
/*      */ 
/*  973 */     cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.Title").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("_", " ")));
/*  974 */     List<String> lore = new ArrayList();
/*  975 */     lore.add(cMsg(typeColour + typeDisplay + ": &f" + type));
/*  976 */     if ((info.equals("None")) || (info.equals(""))) {
/*  977 */       lore.add(cMsg(infoColour + infoDisplay + ": &f" + info));
/*      */     }
/*      */     else
/*      */     {
/*  981 */       lore.add(cMsg(infoColour + infoDisplay + ":"));
/*  982 */       lore.addAll(wrapString(info));
/*      */     }
/*  984 */     lore.add(cMsg(seriesColour + seriesDisplay + ": &f" + series));
/*  985 */     if (getCardsData().contains("Cards." + rarity + "." + cardName + ".About")) lore.add(cMsg(aboutColour + aboutDisplay + ": &f" + about));
/*  986 */     lore.add(cMsg(rarityColour + ChatColor.BOLD + rarityName));
/*  987 */     cmeta.setLore(lore);
/*  988 */     if (getConfig().getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
/*  989 */     card.setItemMeta(cmeta);
/*  990 */     return card;
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void onMobSpawn(CreatureSpawnEvent e) {
/*  995 */     if ((!(e.getEntity() instanceof Player)) && 
/*  996 */       (e.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER) && (getConfig().getBoolean("General.Spawner-Block"))) {
/*  997 */       e.getEntity().setCustomName(getConfig().getString("General.Spawner-Mob-Name"));
/*  998 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Spawner mob renamed.");
/*  999 */       e.getEntity().setRemoveWhenFarAway(true);
/*      */     }
/*      */   }
/*      */   
/*      */   @EventHandler
/*      */   public void onPlayerJoin(PlayerJoinEvent e)
/*      */   {
/* 1006 */     if (getConfig().getBoolean("General.Auto-Add-Players")) {
/* 1007 */       Player p = e.getPlayer();
/* 1008 */       GregorianCalendar gc = new GregorianCalendar();
/*      */       int year;
/* 1010 */       int date; int month; int year; if (p.hasPlayedBefore()) {
/* 1011 */         gc.setTimeInMillis(p.getFirstPlayed());
/* 1012 */         int date = gc.get(5);
/* 1013 */         int month = gc.get(2) + 1;
/* 1014 */         year = gc.get(1);
/*      */       }
/*      */       else
/*      */       {
/* 1018 */         gc.setTimeInMillis(System.currentTimeMillis());
/* 1019 */         date = gc.get(5);
/* 1020 */         month = gc.get(2) + 1;
/* 1021 */         year = gc.get(1);
/*      */       }
/* 1023 */       ConfigurationSection rarities = getConfig().getConfigurationSection("Rarities");
/* 1024 */       int i = 1;
/* 1025 */       Set<String> rarityKeys = rarities.getKeys(false);
/* 1026 */       Map<String, Boolean> children = permRarities.getChildren();
/* 1027 */       String rarity = getConfig().getString("General.Auto-Add-Player-Rarity");
/* 1028 */       for (String key : rarityKeys) {
/* 1029 */         i++;
/* 1030 */         children.put("xptc.rarity." + key, Boolean.valueOf(false));
/* 1031 */         permRarities.recalculatePermissibles();
/* 1032 */         if (p.hasPermission("xptc.rarity." + key)) {
/* 1033 */           rarity = key;
/* 1034 */           break;
/*      */         }
/*      */       }
/* 1037 */       if (p.isOp()) rarity = getConfig().getString("General.Player-Op-Rarity");
/* 1038 */       if (!getCardsData().contains("Cards." + rarity + "." + p.getName())) {
/* 1039 */         String series = getConfig().getString("General.Player-Series");
/* 1040 */         String type = getConfig().getString("General.Player-Type");
/* 1041 */         boolean hasShiny = getConfig().getBoolean("General.Player-Has-Shiny-Version");
/* 1042 */         getCardsData().set("Cards." + rarity + "." + p.getName() + ".Series", series);
/* 1043 */         getCardsData().set("Cards." + rarity + "." + p.getName() + ".Type", type);
/* 1044 */         getCardsData().set("Cards." + rarity + "." + p.getName() + ".Has-Shiny-Version", Boolean.valueOf(hasShiny));
/* 1045 */         if (getConfig().getBoolean("General.American-Mode")) getCardsData().set("Cards." + rarity + "." + p.getName() + ".Info", "Joined " + month + "/" + date + "/" + year); else
/* 1046 */           getCardsData().set("Cards." + rarity + "." + p.getName() + ".Info", "Joined " + date + "/" + month + "/" + year);
/* 1047 */         saveCardsData();
/* 1048 */         reloadCardsData();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public void createCard(Player creator, String rarity, String name, String series, String type, boolean hasShiny, String info, String about) {
/* 1054 */     if (!getCardsData().contains("Cards." + rarity + "." + name)) {
/* 1055 */       if (name.matches("^[a-zA-Z0-9-_]+$"))
/*      */       {
/* 1057 */         ConfigurationSection rarities = getCardsData().getConfigurationSection("Cards");
/* 1058 */         Set<String> rarityKeys = rarities.getKeys(false);
/* 1059 */         String keyToUse = "";
/* 1060 */         for (String key : rarityKeys) {
/* 1061 */           if (key.equalsIgnoreCase(rarity)) {
/* 1062 */             keyToUse = key;
/*      */           }
/*      */         }
/* 1065 */         if (!keyToUse.equals(""))
/*      */         {
/* 1067 */           String series1 = "";String type1 = "";String info1 = "";
/*      */           
/* 1069 */           if (series.matches("^[a-zA-Z0-9-_]+$")) series1 = series; else series1 = "None";
/* 1070 */           if (type.matches("^[a-zA-Z0-9-_]+$")) type1 = type; else type1 = "None";
/* 1071 */           if (info.matches("^[a-zA-Z0-9-_/ ]+$")) info1 = info; else info1 = "None";
/* 1072 */           boolean hasShiny1; boolean hasShiny1; if ((hasShiny) || (!hasShiny)) hasShiny1 = hasShiny; else hasShiny1 = false;
/* 1073 */           getCardsData().set("Cards." + rarity + "." + name + ".Series", series1);
/* 1074 */           getCardsData().set("Cards." + rarity + "." + name + ".Type", type1);
/* 1075 */           getCardsData().set("Cards." + rarity + "." + name + ".Has-Shiny-Version", Boolean.valueOf(hasShiny1));
/* 1076 */           getCardsData().set("Cards." + rarity + "." + name + ".Info", info1);
/* 1077 */           saveCardsData();
/* 1078 */           reloadCardsData();
/* 1079 */           creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateSuccess").replaceAll("%name%", name).replaceAll("%rarity%", String.valueOf(rarity))));
/* 1080 */         } else { creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoRarity")));
/* 1081 */         } } else { creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateNoName")));
/* 1082 */       } } else creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateExists")));
/*      */   }
/*      */   
/*      */   public void reloadCustomConfig()
/*      */   {
/* 1087 */     File file = new File(getDataFolder() + File.separator + "config.yml");
/* 1088 */     if (!file.exists())
/*      */     {
/* 1090 */       getConfig().options().copyDefaults(true);
/* 1091 */       saveDefaultConfig();
/*      */     }
/* 1093 */     reloadConfig();
/* 1094 */     reloadDeckData();
/* 1095 */     reloadMessagesData();
/* 1096 */     reloadCardsData();
/* 1097 */     reloadDeckData();
/* 1098 */     reloadMessagesData();
/* 1099 */     reloadCardsData();
/*      */   }
/*      */   
/*      */   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
/* 1103 */     if (cmd.getName().equalsIgnoreCase("xptc")) {
/* 1104 */       if (args.length > 0)
/*      */       {
/* 1106 */         if (args[0].equalsIgnoreCase("reload")) {
/* 1107 */           if (sender.hasPermission("xptc.reload"))
/*      */           {
/* 1109 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.Reload")));
/* 1110 */             reloadCustomConfig();
/* 1111 */             if (getConfig().getBoolean("General.Schedule-Cards")) startTimer();
/* 1112 */             return true;
/*      */           }
/* 1114 */           sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */         }
/* 1116 */         else if (args[0].equalsIgnoreCase("toggle")) {
/* 1117 */           Player p = (Player)sender;
/* 1118 */           if ((isOnList(p)) && (blacklistMode() == 'b')) {
/* 1119 */             removeFromList(p);
/* 1120 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleEnabled")));
/*      */           }
/* 1122 */           else if ((isOnList(p)) && (blacklistMode() == 'w')) {
/* 1123 */             removeFromList(p);
/* 1124 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleDisabled")));
/*      */           }
/* 1126 */           else if ((!isOnList(p)) && (blacklistMode() == 'b')) {
/* 1127 */             addToList(p);
/* 1128 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleDisabled")));
/*      */           }
/* 1130 */           else if ((!isOnList(p)) && (blacklistMode() == 'w')) {
/* 1131 */             addToList(p);
/* 1132 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleEnabled")));
/*      */           }
/*      */         }
/* 1135 */         else if (args[0].equalsIgnoreCase("create")) {
/* 1136 */           if (sender.hasPermission("xptc.create")) {
/* 1137 */             Player p = (Player)sender;
/* 1138 */             if (args.length < 8) {
/* 1139 */               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateUsage")));
/*      */             } else {
/* 1141 */               boolean isShiny = false;
/* 1142 */               if ((args[5].equalsIgnoreCase("true")) || (args[5].equalsIgnoreCase("yes")) || (args[5].equalsIgnoreCase("y"))) isShiny = true; else
/* 1143 */                 isShiny = false;
/* 1144 */               createCard(p, args[1].replaceAll("_", " "), args[2], args[3].replaceAll("_", " "), args[4].replaceAll("_", " "), isShiny, args[6].replaceAll("_", " "), args[7].replaceAll("_", " "));
/*      */             }
/*      */           }
/*      */         }
/* 1148 */         else if (args[0].equalsIgnoreCase("givecard")) {
/* 1149 */           if (sender.hasPermission("xptc.givecard"))
/*      */           {
/* 1151 */             if (args.length > 2) {
/* 1152 */               Player p = (Player)sender;
/* 1153 */               if (getCardsData().contains("Cards." + args[1].replaceAll("_", " ") + "." + args[2]))
/* 1154 */                 p.getInventory().addItem(new ItemStack[] { getNormalCard(args[2], args[1].replaceAll("_", " "), Integer.valueOf(1)) }); else {
/* 1155 */                 sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCard")));
/*      */               }
/*      */             }
/*      */             else {
/* 1159 */               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveCardUsage")));
/*      */             }
/*      */           } else {
/* 1162 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           }
/* 1164 */         } else if (args[0].equalsIgnoreCase("giveshinycard")) {
/* 1165 */           if (sender.hasPermission("xptc.giveshinycard"))
/*      */           {
/* 1167 */             if (args.length > 2) {
/* 1168 */               Player p = (Player)sender;
/* 1169 */               if (getCardsData().contains("Cards." + args[1].replaceAll("_", " ") + "." + args[2]))
/* 1170 */                 p.getInventory().addItem(new ItemStack[] { createPlayerCard(args[2], args[1].replaceAll("_", " "), Integer.valueOf(1), true) }); else {
/* 1171 */                 sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCard")));
/*      */               }
/*      */             }
/*      */             else {
/* 1175 */               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveCardUsage")));
/*      */             }
/*      */           } else {
/* 1178 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           }
/* 1180 */         } else if (args[0].equalsIgnoreCase("giveboosterpack")) {
/* 1181 */           if (sender.hasPermission("xptc.giveboosterpack")) {
/* 1182 */             if (args.length > 2) {
/* 1183 */               if (getConfig().contains("BoosterPacks." + args[2].replaceAll(" ", "_"))) {
/* 1184 */                 if (Bukkit.getPlayer(args[1]) != null) {
/* 1185 */                   Player p = Bukkit.getPlayer(args[1]);
/* 1186 */                   if (p.getInventory().firstEmpty() != -1) {
/* 1187 */                     p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoosterPackMsg")));
/* 1188 */                     p.getInventory().addItem(new ItemStack[] { createBoosterPack(args[2]) });
/*      */                   }
/*      */                   else
/*      */                   {
/* 1192 */                     World curWorld = p.getWorld();
/* 1193 */                     if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                     {
/* 1195 */                       p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoosterPackMsg")));
/* 1196 */                       curWorld.dropItem(p.getLocation(), createBoosterPack(args[2]));
/*      */                     }
/*      */                   }
/* 1199 */                 } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPlayer")));
/* 1200 */                 } } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoBoosterPack")));
/*      */             } else
/* 1202 */               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveBoosterPackUsage")));
/*      */           } else {
/* 1204 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           }
/* 1206 */         } else if (args[0].equalsIgnoreCase("getdeck")) {
/* 1207 */           if (sender.hasPermission("xptc.getdeck")) {
/* 1208 */             if (args.length > 1) {
/* 1209 */               if (StringUtils.isNumeric(args[1])) {
/* 1210 */                 if (sender.hasPermission("xptc.decks." + args[1]))
/*      */                 {
/* 1212 */                   Player p = (Player)sender;
/* 1213 */                   if (!hasDeck(p, Integer.valueOf(args[1]).intValue())) {
/* 1214 */                     if (p.getInventory().firstEmpty() != -1) {
/* 1215 */                       p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveDeck")));
/* 1216 */                       p.getInventory().addItem(new ItemStack[] { createDeck(p, Integer.valueOf(args[1]).intValue()) });
/*      */                     }
/*      */                     else
/*      */                     {
/* 1220 */                       World curWorld = p.getWorld();
/* 1221 */                       if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                       {
/* 1223 */                         p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveDeck")));
/* 1224 */                         curWorld.dropItem(p.getLocation(), createDeck(p, Integer.valueOf(args[1]).intValue()));
/*      */                       }
/*      */                     }
/* 1227 */                   } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.AlreadyHaveDeck")));
/* 1228 */                 } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.MaxDecks")));
/* 1229 */                 } } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GetDeckUsage")));
/* 1230 */             } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GetDeckUsage")));
/* 1231 */           } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */         }
/* 1233 */         else if (args[0].equalsIgnoreCase("giverandomcard")) {
/* 1234 */           if (sender.hasPermission("xptc.randomcard")) {
/* 1235 */             if (args.length > 2) {
/* 1236 */               if (Bukkit.getPlayer(args[2]) != null) {
/* 1237 */                 Player p = Bukkit.getPlayer(args[2]);
/*      */                 try {
/* 1239 */                   if (EntityType.valueOf(args[1].toUpperCase()) != null) {
/* 1240 */                     String rare = calculateRarity(EntityType.valueOf(args[1].toUpperCase()), true);
/* 1241 */                     if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] onCommand.rare: " + rare);
/* 1242 */                     sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCardMsg").replaceAll("%player%", p.getName())));
/* 1243 */                     if (p.getInventory().firstEmpty() != -1) {
/* 1244 */                       p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCard")));
/* 1245 */                       if (generateCard(rare, false) != null) p.getInventory().addItem(new ItemStack[] { generateCard(rare, false) });
/*      */                     }
/*      */                     else
/*      */                     {
/* 1249 */                       World curWorld = p.getWorld();
/* 1250 */                       if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                       {
/* 1252 */                         p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCard")));
/* 1253 */                         if (generateCard(rare, false) != null) curWorld.dropItem(p.getLocation(), generateCard(rare, false));
/*      */                       }
/*      */                     }
/*      */                   } else {
/* 1257 */                     sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoEntity")));
/* 1258 */                   } } catch (IllegalArgumentException e) { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoEntity")));
/*      */                 }
/* 1260 */               } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPlayer")));
/*      */               }
/* 1262 */             } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCardUsage")));
/*      */           } else {
/* 1264 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           }
/* 1266 */         } else if (args[0].equalsIgnoreCase("list")) {
/* 1267 */           if (sender.hasPermission("xptc.list")) {
/* 1268 */             ConfigurationSection cards = getCardsData().getConfigurationSection("Cards");
/* 1269 */             Set<String> cardKeys = cards.getKeys(false);
/* 1270 */             String msg = "";
/* 1271 */             int i = 0;
/* 1272 */             String finalMsg = "";
/* 1273 */             for (String key : cardKeys) {
/* 1274 */               ConfigurationSection cardsWithKey = getCardsData().getConfigurationSection("Cards." + key);
/* 1275 */               Set<String> keyKeys = cardsWithKey.getKeys(false);
/* 1276 */               for (String key2 : keyKeys) {
/* 1277 */                 if (i > 41) {
/* 1278 */                   finalMsg = msg + "&7and more!";
/*      */                 } else
/* 1280 */                   msg = msg + "&7" + key2.replaceAll("_", " ") + "&f, ";
/* 1281 */                 i++;
/*      */               }
/* 1283 */               sender.sendMessage(cMsg("&6--- " + key + " &7(&f" + i + "&7)&6" + " ---"));
/* 1284 */               msg = StringUtils.removeEnd(msg, ", ");
/* 1285 */               if (finalMsg.equals("")) sender.sendMessage(cMsg(msg)); else
/* 1286 */                 sender.sendMessage(cMsg(finalMsg));
/* 1287 */               msg = "";
/* 1288 */               finalMsg = "";
/* 1289 */               i = 0;
/*      */             }
/*      */           } else {
/* 1292 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           } } else { boolean hasExtra;
/* 1294 */           if (args[0].equalsIgnoreCase("listpacks")) {
/* 1295 */             if (sender.hasPermission("xptc.listpacks")) {
/* 1296 */               ConfigurationSection cards = getConfig().getConfigurationSection("BoosterPacks");
/* 1297 */               Set<String> cardKeys = cards.getKeys(false);
/* 1298 */               int i = 0;
/* 1299 */               sender.sendMessage(cMsg("&6--- Booster Packs ---"));
/* 1300 */               boolean hasPrice = false;hasExtra = false;
/* 1301 */               for (String key : cardKeys) {
/* 1302 */                 if (getConfig().contains("BoosterPacks." + key + ".Price")) hasPrice = true;
/* 1303 */                 if ((getConfig().contains("BoosterPacks." + key + ".ExtraCardRarity")) && (getConfig().contains("BoosterPacks." + key + ".NumExtraCards"))) hasExtra = true;
/* 1304 */                 i++;
/* 1305 */                 if (hasPrice) sender.sendMessage(cMsg("&6" + i + ") &e" + key + " &7(&aPrice: " + getConfig().getDouble(new StringBuilder("BoosterPacks.").append(key).append(".Price&7").toString()) + ")")); else
/* 1306 */                   sender.sendMessage(cMsg("&6" + i + ") &e" + key));
/* 1307 */                 if (hasExtra) sender.sendMessage(cMsg("  &7- &f&o" + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumNormalCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".NormalCardRarity").toString()) + ", " + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumExtraCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".ExtraCardRarity").toString()) + ", " + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumSpecialCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".SpecialCardRarity").toString()))); else
/* 1308 */                   sender.sendMessage(cMsg("  &7- &f&o" + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumNormalCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".NormalCardRarity").toString()) + ", " + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumSpecialCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".SpecialCardRarity").toString())));
/* 1309 */                 hasPrice = false;
/* 1310 */                 hasExtra = false;
/*      */               }
/*      */             } else {
/* 1313 */               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */             }
/* 1315 */           } else if (args[0].equalsIgnoreCase("giveaway")) {
/* 1316 */             if (sender.hasPermission("xptc.giveaway")) {
/* 1317 */               if (args.length > 1) {
/* 1318 */                 ConfigurationSection rarities = getCardsData().getConfigurationSection("Cards");
/* 1319 */                 Set<String> rarityKeys = rarities.getKeys(false);
/* 1320 */                 String keyToUse = "";
/* 1321 */                 for (String key : rarityKeys) {
/* 1322 */                   if (key.equalsIgnoreCase(args[1].replaceAll("_", " "))) {
/* 1323 */                     keyToUse = key;
/*      */                   }
/*      */                 }
/* 1326 */                 if (!keyToUse.equals("")) {
/* 1327 */                   Bukkit.broadcastMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.Giveaway").replaceAll("%player%", sender.getName()).replaceAll("%rarity%", keyToUse)));
/* 1328 */                   for (Player p : Bukkit.getOnlinePlayers()) {
/* 1329 */                     ConfigurationSection cards = getCardsData().getConfigurationSection("Cards." + keyToUse);
/* 1330 */                     Object cardKeys = cards.getKeys(false);
/* 1331 */                     int rIndex = this.r.nextInt(((Set)cardKeys).size());int i = 0;
/* 1332 */                     String cardName = "";
/* 1333 */                     for (String theCardName : (Set)cardKeys) {
/* 1334 */                       if (i == rIndex) { cardName = theCardName; break; }
/* 1335 */                       i++;
/*      */                     }
/* 1337 */                     if (p.getInventory().firstEmpty() != -1) {
/* 1338 */                       p.getInventory().addItem(new ItemStack[] { createPlayerCard(cardName, keyToUse, Integer.valueOf(1), false) });
/*      */                     }
/*      */                     else
/*      */                     {
/* 1342 */                       World curWorld = p.getWorld();
/* 1343 */                       if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                       {
/* 1345 */                         curWorld.dropItem(p.getLocation(), createPlayerCard(cardName, keyToUse, Integer.valueOf(1), false));
/*      */                       }
/*      */                     }
/*      */                   }
/*      */                 } else {
/* 1350 */                   sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoRarity")));
/*      */                 }
/*      */               }
/*      */               else {
/* 1354 */                 sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveawayUsage")));
/*      */               }
/*      */             } else {
/* 1357 */               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */             }
/* 1359 */           } else if (args[0].equalsIgnoreCase("worth")) {
/* 1360 */             if (sender.hasPermission("xptc.worth")) {
/* 1361 */               if (this.hasVault) {
/* 1362 */                 Player p = (Player)sender;
/* 1363 */                 if (p.getItemInHand().getType() == Material.valueOf(getConfig().getString("General.Card-Material"))) {
/* 1364 */                   ItemStack itemInHand = p.getItemInHand();
/* 1365 */                   String itemName = itemInHand.getItemMeta().getDisplayName();
/* 1366 */                   if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(itemName);
/* 1367 */                   if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(ChatColor.stripColor(itemName));
/* 1368 */                   String[] splitName = ChatColor.stripColor(itemName).split(" ");
/* 1369 */                   String cardName = "";
/* 1370 */                   if (splitName.length > 1) {
/* 1371 */                     cardName = splitName[1];
/*      */                   } else
/* 1373 */                     cardName = splitName[0];
/* 1374 */                   if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(cardName);
/* 1375 */                   List<String> lore = itemInHand.getItemMeta().getLore();
/* 1376 */                   String rarity = ChatColor.stripColor((String)lore.get(3));
/* 1377 */                   if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(rarity);
/* 1378 */                   boolean canBuy = false;
/* 1379 */                   double buyPrice = 0.0D;
/* 1380 */                   if (getCardsData().contains("Cards." + rarity + "." + cardName + ".Buy-Price")) {
/* 1381 */                     buyPrice = getCardsData().getDouble("Cards." + rarity + "." + cardName + ".Buy-Price");
/* 1382 */                     if (buyPrice > 0.0D) canBuy = true;
/*      */                   }
/* 1384 */                   if (canBuy) {
/* 1385 */                     sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CanBuy").replaceAll("%buyAmount%", String.valueOf(buyPrice))));
/*      */                   }
/* 1387 */                   else if (!canBuy) {
/* 1388 */                     sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CanNotBuy")));
/*      */                   }
/*      */                 } else {
/* 1391 */                   sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NotACard")));
/*      */                 }
/* 1393 */               } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoVault")));
/*      */               }
/* 1395 */             } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           }
/* 1397 */           else if (args[0].equalsIgnoreCase("credits")) {
/* 1398 */             sender.sendMessage(cMsg(formatTitle("Credits and Special Thanks")));
/* 1399 */             sender.sendMessage(cMsg("&7[&aDeveloper&7] &aLukas Xenoyia Gentle"));
/* 1400 */             sender.sendMessage(cMsg("   &7- &6&oxPXenoyia&f, &6&oXenoyia&f, &6&oxPLukas&f, &6&oSnoopDogg&f"));
/* 1401 */             sender.sendMessage(cMsg("&7[&eSpecial Thanks&7] XpanD, IrChaos, xtechgamer735, PTsandro, FlyingSquidwolf, iXRaZoRXi, iToxy, TowelieDOH, Miku_Snow, NOBUTSS, doitliketyler, Celebrimbor90, Magz, GypsySix, bumbble, iFosadrink_2, Sunique, TheRealGSD, Zenko, Berkth, TubeCraftXXL, Cra2ytig3r, marcosds13, ericbarbwire, Bonzo"));
/*      */           }
/* 1403 */           else if (args[0].equalsIgnoreCase("buy")) {
/* 1404 */             if (sender.hasPermission("xptc.buy")) {
/* 1405 */               if (this.hasVault) {
/* 1406 */                 Player p = (Player)sender;
/* 1407 */                 if (args.length > 1) {
/* 1408 */                   if (args[1].equalsIgnoreCase("pack")) {
/* 1409 */                     if (args.length > 2) {
/* 1410 */                       if (getConfig().contains("BoosterPacks." + args[2])) {
/* 1411 */                         double buyPrice = 0.0D;
/* 1412 */                         boolean canBuy = false;
/* 1413 */                         if (getConfig().contains("BoosterPacks." + args[2] + ".Price")) {
/* 1414 */                           buyPrice = getConfig().getDouble("BoosterPacks." + args[2] + ".Price");
/* 1415 */                           if (buyPrice > 0.0D) canBuy = true;
/*      */                         }
/* 1417 */                         if (canBuy) {
/* 1418 */                           if (econ.getBalance(p) >= buyPrice) {
/* 1419 */                             if (getConfig().getBoolean("PluginSupport.Vault.Closed-Economy")) {
/* 1420 */                               econ.withdrawPlayer(p, buyPrice);
/* 1421 */                               econ.depositPlayer(getConfig().getString("PluginSupport.Vault.Server-Account"), buyPrice);
/* 1422 */                             } else { econ.withdrawPlayer(p, buyPrice); }
/* 1423 */                             if (p.getInventory().firstEmpty() != -1) {
/* 1424 */                               p.getInventory().addItem(new ItemStack[] { createBoosterPack(args[2]) });
/*      */                             }
/*      */                             else
/*      */                             {
/* 1428 */                               World curWorld = p.getWorld();
/* 1429 */                               if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                               {
/* 1431 */                                 curWorld.dropItem(p.getLocation(), createBoosterPack(args[2]));
/*      */                               }
/*      */                             }
/* 1434 */                             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoughtCard").replaceAll("%amount%", String.valueOf(buyPrice))));
/*      */                           } else {
/* 1436 */                             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NotEnoughMoney")));
/*      */                           }
/* 1438 */                         } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CannotBeBought")));
/*      */                       } else {
/* 1440 */                         sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.PackDoesntExist")));
/*      */                       }
/* 1442 */                     } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ChoosePack")));
/*      */                   }
/* 1444 */                   else if (args[1].equalsIgnoreCase("card")) {
/* 1445 */                     if (args.length > 2) {
/* 1446 */                       if (args.length > 3) {
/* 1447 */                         if (getCardsData().contains("Cards." + args[2] + "." + args[3])) {
/* 1448 */                           double buyPrice = 0.0D;
/* 1449 */                           boolean canBuy = false;
/* 1450 */                           if (getCardsData().contains("Cards." + args[2] + "." + args[3] + ".Buy-Price")) {
/* 1451 */                             buyPrice = getCardsData().getDouble("Cards." + args[2] + "." + args[3] + ".Buy-Price");
/* 1452 */                             if (buyPrice > 0.0D) canBuy = true;
/*      */                           }
/* 1454 */                           if (canBuy) {
/* 1455 */                             if (econ.getBalance(p) >= buyPrice) {
/* 1456 */                               if (getConfig().getBoolean("PluginSupport.Vault.Closed-Economy")) {
/* 1457 */                                 econ.withdrawPlayer(p, buyPrice);
/* 1458 */                                 econ.depositPlayer(getConfig().getString("PluginSupport.Vault.Server-Account"), buyPrice);
/* 1459 */                               } else { econ.withdrawPlayer(p, buyPrice); }
/* 1460 */                               if (p.getInventory().firstEmpty() != -1) {
/* 1461 */                                 p.getInventory().addItem(new ItemStack[] { createPlayerCard(args[3], args[2], Integer.valueOf(1), false) });
/*      */                               }
/*      */                               else
/*      */                               {
/* 1465 */                                 World curWorld = p.getWorld();
/* 1466 */                                 if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                                 {
/* 1468 */                                   curWorld.dropItem(p.getLocation(), createPlayerCard(args[3], args[2], Integer.valueOf(1), false));
/*      */                                 }
/*      */                               }
/* 1471 */                               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoughtCard").replaceAll("%amount%", String.valueOf(buyPrice))));
/*      */                             } else {
/* 1473 */                               sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NotEnoughMoney")));
/*      */                             }
/* 1475 */                           } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CannotBeBought")));
/*      */                         } else {
/* 1477 */                           sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CardDoesntExist")));
/*      */                         }
/* 1479 */                       } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ChooseCard")));
/*      */                     } else
/* 1481 */                       sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ChooseRarity")));
/*      */                   } else
/* 1483 */                     sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BuyUsage")));
/*      */                 } else
/* 1485 */                   sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BuyUsage")));
/*      */               } else {
/* 1487 */                 sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoVault")));
/*      */               }
/* 1489 */             } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
/*      */           } else {
/* 1491 */             sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCmd")));
/*      */           }
/*      */         }
/*      */       } else {
/* 1495 */         boolean showUsage = getConfig().getBoolean("General.Show-Command-Usage", true);
/* 1496 */         sender.sendMessage(cMsg(formatTitle(getConfig().getString("General.Server-Name") + " Trading Cards")));
/* 1497 */         if (sender.hasPermission("xptc.reload")) {
/* 1498 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ReloadUsage")));
/* 1499 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ReloadHelp")));
/*      */         }
/* 1501 */         if (sender.hasPermission("xptc.givecard")) {
/* 1502 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveCardUsage")));
/* 1503 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveCardHelp")));
/*      */         }
/* 1505 */         if (sender.hasPermission("xptc.giveshinycard")) {
/* 1506 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveShinyCardUsage")));
/* 1507 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveShinyCardHelp")));
/*      */         }
/* 1509 */         if (sender.hasPermission("xptc.giverandomcard")) {
/* 1510 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveRandomCardUsage")));
/* 1511 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveRandomCardHelp")));
/*      */         }
/* 1513 */         if (sender.hasPermission("xptc.giveboosterpack")) {
/* 1514 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveBoosterPackUsage")));
/* 1515 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveBoosterPackHelp")));
/*      */         }
/* 1517 */         if (sender.hasPermission("xptc.giveaway")) {
/* 1518 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveawayUsage")));
/* 1519 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveawayHelp")));
/*      */         }
/* 1521 */         if (sender.hasPermission("xptc.getdeck")) {
/* 1522 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GetDeckUsage")));
/* 1523 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GetDeckHelp")));
/*      */         }
/* 1525 */         if (sender.hasPermission("xptc.list")) {
/* 1526 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ListUsage")));
/* 1527 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ListHelp")));
/*      */         }
/* 1529 */         if (sender.hasPermission("xptc.listpacks")) {
/* 1530 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ListPacksUsage")));
/* 1531 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ListPacksHelp")));
/*      */         }
/* 1533 */         if (sender.hasPermission("xptc.toggle")) {
/* 1534 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ToggleUsage")));
/* 1535 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ToggleHelp")));
/*      */         }
/* 1537 */         if (sender.hasPermission("xptc.create")) {
/* 1538 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.CreateUsage")));
/* 1539 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.CreateHelp")));
/*      */         }
/* 1541 */         if ((sender.hasPermission("xptc.buy")) && (this.hasVault)) {
/* 1542 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.BuyUsage")));
/* 1543 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.BuyHelp")));
/*      */         }
/* 1545 */         if ((sender.hasPermission("xptc.worth")) && (this.hasVault)) {
/* 1546 */           sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.WorthUsage")));
/* 1547 */           if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.WorthHelp")));
/*      */         }
/* 1549 */         return true;
/*      */       }
/*      */     }
/* 1552 */     return true;
/*      */   }
/*      */   
/*      */   public String cMsg(String message) {
/* 1556 */     return ChatColor.translateAlternateColorCodes('&', message);
/*      */   }
/*      */   
/*      */   public void startTimer() {
/* 1560 */     int hours = 1;
/* 1561 */     BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
/* 1562 */     if ((scheduler.isQueued(this.taskid)) || (scheduler.isCurrentlyRunning(this.taskid))) {
/* 1563 */       scheduler.cancelTask(this.taskid);
/* 1564 */       if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Successfully cancelled task " + this.taskid);
/*      */     }
/* 1566 */     if (getConfig().getInt("General.Schedule-Card-Time-In-Hours") < 1) hours = 1; else
/* 1567 */       hours = getConfig().getInt("General.Schedule-Card-Time-In-Hours");
/* 1568 */     String tmessage = getMessagesData().getString("Messages.TimerMessage").replaceAll("%hour%", String.valueOf(hours));
/* 1569 */     Bukkit.broadcastMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + tmessage));
/* 1570 */     this.taskid = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
/*      */       public void run() {
/* 1572 */         if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Task running..");
/* 1573 */         if (TradingCards.this.getConfig().getBoolean("General.Schedule-Cards")) {
/* 1574 */           if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Schedule cards is true.");
/* 1575 */           ConfigurationSection rarities = TradingCards.this.getCardsData().getConfigurationSection("Cards");
/* 1576 */           Set<String> rarityKeys = rarities.getKeys(false);
/* 1577 */           String keyToUse = "";
/* 1578 */           for (String key : rarityKeys) {
/* 1579 */             if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Rarity key: " + key);
/* 1580 */             if (key.equalsIgnoreCase(TradingCards.this.getConfig().getString("General.Schedule-Card-Rarity"))) {
/* 1581 */               keyToUse = key;
/*      */             }
/*      */           }
/* 1584 */           if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] keyToUse: " + keyToUse);
/* 1585 */           if (!keyToUse.equals("")) {
/* 1586 */             Bukkit.broadcastMessage(TradingCards.this.cMsg(TradingCards.this.getMessagesData().getString("Messages.Prefix") + " " + TradingCards.this.getMessagesData().getString("Messages.ScheduledGiveaway")));
/* 1587 */             for (Player p : Bukkit.getOnlinePlayers()) {
/* 1588 */               ConfigurationSection cards = TradingCards.this.getCardsData().getConfigurationSection("Cards." + keyToUse);
/* 1589 */               Set<String> cardKeys = cards.getKeys(false);
/* 1590 */               int rIndex = TradingCards.this.r.nextInt(cardKeys.size());int i = 0;
/* 1591 */               String cardName = "";
/* 1592 */               for (String theCardName : cardKeys) {
/* 1593 */                 if (i == rIndex) { cardName = theCardName; break; }
/* 1594 */                 i++;
/*      */               }
/* 1596 */               if (p.getInventory().firstEmpty() != -1) {
/* 1597 */                 p.getInventory().addItem(new ItemStack[] { TradingCards.this.createPlayerCard(cardName, keyToUse, Integer.valueOf(1), false) });
/*      */               }
/*      */               else
/*      */               {
/* 1601 */                 World curWorld = p.getWorld();
/* 1602 */                 if (p.getGameMode() == GameMode.SURVIVAL)
/*      */                 {
/* 1604 */                   curWorld.dropItem(p.getLocation(), TradingCards.this.createPlayerCard(cardName, keyToUse, Integer.valueOf(1), false));
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 1611 */     }, hours * 20 * 60 * 60, hours * 20 * 60 * 60);
/*      */   }
/*      */   
/*      */   public String formatTitle(String title) {
/* 1615 */     String line = "&7[&foOo&7]&f____________________________________________________&7[&foOo&7]&f";
/* 1616 */     int pivot = line.length() / 2;
/* 1617 */     String center = "&7.< &3" + title + "&7" + " >.&f";
/* 1618 */     String out = line.substring(0, Math.max(0, pivot - center.length() / 2));
/* 1619 */     out = out + center + line.substring(pivot + center.length() / 2);
/* 1620 */     return out;
/*      */   }
/*      */ }