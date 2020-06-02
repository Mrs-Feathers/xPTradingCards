package it.forgottenworld.tradingcards;

/*import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.framework.ArenaMaster;*/
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
/*      */
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.*;

import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
/*      */
/*      */
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
/*      */
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
/*      */
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
/*      */
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
/*      */
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitScheduler;

public class TradingCards extends org.bukkit.plugin.java.JavaPlugin implements Listener, org.bukkit.command.CommandExecutor
{
  List<EntityType> hostileMobs = Arrays.asList(EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.BLAZE, EntityType.SILVERFISH, EntityType.GHAST, EntityType.SLIME, EntityType.GUARDIAN, EntityType.MAGMA_CUBE, EntityType.WITCH, EntityType.ENDERMITE);
  List<EntityType> neutralMobs = Arrays.asList(EntityType.ENDERMAN, EntityType.PIG_ZOMBIE, EntityType.WOLF, EntityType.SNOWMAN, EntityType.IRON_GOLEM);
  List<EntityType> passiveMobs = Arrays.asList(EntityType.CHICKEN, EntityType.COW, EntityType.SQUID, EntityType.SHEEP, EntityType.PIG, EntityType.RABBIT, EntityType.VILLAGER, EntityType.BAT, EntityType.HORSE);
  List<EntityType> bossMobs = Arrays.asList(EntityType.ENDER_DRAGON, EntityType.WITHER);
  public static Permission permRarities = new Permission("fwtc.rarity");
  boolean hasVault;
  boolean hasMobArena = false;
  private FileConfiguration deckData = null;
  private File deckDataFile = null;
  private FileConfiguration messagesData = null;
  //public ArenaMaster am;
  private File messagesDataFile = null;
  private FileConfiguration cardsData = null;
  private File cardsDataFile = null;
  public static Economy econ = null;
  public static Permission perms = null;
  public static net.milkbowl.vault.chat.Chat chat = null;
  Random r = new Random();
  int taskid;
  
  public void onEnable() {
    try {
      getConfig().options().copyDefaults(true);
      getServer().getPluginManager().addPermission(permRarities);
      saveDefaultConfig();
      getServer().getPluginManager().registerEvents(this, this);
      PluginCommand cmd = getCommand("fwtc");
      if (cmd != null) cmd.setExecutor(this);
      reloadCustomConfig();
      saveDefaultDeckFile();
      reloadDeckData();
      saveDefaultMessagesFile();
      reloadMessagesData();
      saveDefaultCardsFile();
      reloadCardsData();
    /*if (getConfig().getBoolean("PluginSupport.Towny.Towny-Enabled"))
      if (getServer().getPluginManager().getPlugin("Towny") != null) {
        getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        System.out.println("[xPTradingCards] Towny successfully hooked!");
      } else {
        System.out.println("[xPTradingCards] Towny not found, hook unsuccessful!");
      }*/
      if (getConfig().getBoolean("PluginSupport.Vault.Vault-Enabled"))
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
          setupEconomy();
          System.out.println("[xPTradingCards] Vault hook successful!");
          this.hasVault = true;
        } else {
          System.out.println("[xPTradingCards] Vault not found, hook unsuccessful!");
        }
    /*if (getConfig().getBoolean("PluginSupport.MobArena.MobArena-Enabled"))
      if (getServer().getPluginManager().getPlugin("MobArena") != null) {
        PluginManager pm = getServer().getPluginManager();
        //MobArena maPlugin = (MobArena)pm.getPlugin("MobArena");
        //this.am = maPlugin.getArenaMaster();
        //pm.registerEvents(new MobArenaListener(this), this);
        //System.out.println("[xPTradingCards] Mob Arena hook successful!");
        this.hasMobArena = false;
      } else {
        System.out.println("[xPTradingCards] Mob Arena not found, hook unsuccessful!");
      }*/
      if (getConfig().getBoolean("General.Schedule-Cards")) startTimer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void onDisable() {
    this.deckData = null;
    this.deckDataFile = null;
    this.messagesData = null;
    this.messagesDataFile = null;
    this.cardsData = null;
    this.cardsDataFile = null;
    econ = null;
    perms = null;
    chat = null;
    getServer().getPluginManager().removePermission(permRarities);
  }
  
  private void setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return;
    }
    econ = rsp.getProvider();
  }

  public String formatDouble(double value)
  {
    NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
    nf.setMaximumFractionDigits(2);
    nf.setMinimumFractionDigits(2);
    return nf.format(value);
  }
  
  public boolean isMobHostile(EntityType e) {
    return this.hostileMobs.contains(e);
  }
  
  public boolean isMobNeutral(EntityType e) {
    return this.neutralMobs.contains(e);
  }
  
  public boolean isMobPassive(EntityType e) {
    return this.passiveMobs.contains(e);
  }
  
  public boolean isMobBoss(EntityType e) {
    return this.bossMobs.contains(e);
  }
  
  public ItemStack getBlankCard(int quantity) {
    return new ItemStack(Material.getMaterial(getConfig().getString("General.Card-Material")), quantity);
  }
  
  public ItemStack getBlankBoosterPack() {
    return new ItemStack(Material.getMaterial(getConfig().getString("General.BoosterPack-Material")));
  }
  
  public ItemStack getBlankDeck() {
    return new ItemStack(Material.getMaterial(getConfig().getString("General.Deck-Material")));
  }
  
  public ItemStack createDeck(Player p, int num) {
    ItemStack deck = getBlankDeck();
    ItemMeta deckMeta = deck.getItemMeta();
    deckMeta.setDisplayName(cMsg(getConfig().getString("General.Deck-Prefix") + p.getName() + "'s Deck #" + num));
    if (getConfig().getBoolean("General.Hide-Enchants", true)) deckMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    deck.setItemMeta(deckMeta);
    deck.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
    return deck;
  }
  
  public void reloadDeckData() {
    if (this.deckDataFile == null) {
      this.deckDataFile = new File(getDataFolder(), "decks.yml");
    }
    this.deckData = YamlConfiguration.loadConfiguration(this.deckDataFile);
    Reader defConfigStream = null;
    try {
      defConfigStream = new InputStreamReader(getResource("decks.yml"), "UTF8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      this.deckData.setDefaults(defConfig);
    }
  }
  
  public FileConfiguration getDeckData() {
    if (this.deckData == null) {
      reloadDeckData();
    }
    return this.deckData;
  }
  
  public void saveDeckData() {
    if ((this.deckData == null) || (this.deckDataFile == null)) {
      return;
    }
    try {
      getDeckData().save(this.deckDataFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void saveDefaultDeckFile() {
    if (this.deckDataFile == null) {
      this.deckDataFile = new File(getDataFolder(), "decks.yml");
    }
    if (!this.deckDataFile.exists()) {
      saveResource("decks.yml", false);
    }
  }
  
  public void reloadMessagesData() {
    if (this.messagesDataFile == null) {
      this.messagesDataFile = new File(getDataFolder(), "messages.yml");
    }
    this.messagesData = YamlConfiguration.loadConfiguration(this.messagesDataFile);
    Reader defConfigStream = null;
    try {
      defConfigStream = new InputStreamReader(getResource("messages.yml"), "UTF8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      this.messagesData.setDefaults(defConfig);
    }
  }
  
  public FileConfiguration getMessagesData() {
    if (this.messagesData == null) {
      reloadMessagesData();
    }
    return this.messagesData;
  }
  
  public void saveMessagesData() {
    if ((this.messagesData == null) || (this.messagesDataFile == null)) {
      return;
    }
    try {
      getMessagesData().save(this.messagesDataFile);
    }
    catch (IOException ignored) {}
  }
  
  public void saveDefaultMessagesFile() {
    if (this.messagesDataFile == null) {
      this.messagesDataFile = new File(getDataFolder(), "messages.yml");
    }
    if (!this.messagesDataFile.exists()) {
      saveResource("messages.yml", false);
    }
  }
  
  public void reloadCardsData() {
    if (this.cardsDataFile == null) {
      this.cardsDataFile = new File(getDataFolder(), "cards.yml");
    }
    this.cardsData = YamlConfiguration.loadConfiguration(this.cardsDataFile);
    Reader defConfigStream = null;
    try {
      defConfigStream = new InputStreamReader(getResource("cards.yml"), "UTF8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      this.cardsData.setDefaults(defConfig);
    }
  }
  
  public FileConfiguration getCardsData() {
    if (this.cardsData == null) {
      reloadCardsData();
    }
    return this.cardsData;
  }
  
  public void saveCardsData() {
    if ((this.cardsData == null) || (this.cardsDataFile == null)) {
      return;
    }
    try {
      getCardsData().save(this.cardsDataFile);
    }
    catch (IOException ignored) {}
  }
  
  public void saveDefaultCardsFile() {
    if (this.cardsDataFile == null) {
      this.cardsDataFile = new File(getDataFolder(), "cards.yml");
    }
    if (!this.cardsDataFile.exists()) {
      saveResource("cards.yml", false);
    }
  }
  
  public boolean hasDeck(Player p, int num) {
    for (ItemStack i : p.getInventory()) {
      if ((i != null) && 
        (i.getType() == Material.valueOf(getConfig().getString("General.Deck-Material"))) && 
        (i.containsEnchantment(Enchantment.DURABILITY)) && 
        (i.getEnchantmentLevel(Enchantment.DURABILITY) == 10)) {
        String name = i.getItemMeta().getDisplayName();
        String[] splitName = name.split("#");
        if (num == Integer.parseInt(splitName[1])) { return true;
        }
      }
    }
    

    return false;
  }
  
  public void openDeck(Player p, int deckNum) {
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck opened.");
    String uuidString = p.getUniqueId().toString();
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck UUID: " + uuidString);
    List<String> contents = getDeckData().getStringList("Decks.Inventories." + uuidString + "." + deckNum);
    List<ItemStack> cards = new ArrayList();
    List<Integer> quantity = new ArrayList();
    ItemStack card = null;
    for (String s : contents) {
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck file content: " + s);
      String[] splitContents = s.split(",");
      if (splitContents[3].equalsIgnoreCase("yes")) {
        card = createPlayerCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]), true);
      } else
        card = getNormalCard(splitContents[1], splitContents[0], Integer.valueOf(splitContents[2]));
      cards.add(card);
      quantity.add(Integer.valueOf(splitContents[2]));
      if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Put " + card + "," + splitContents[2] + " into respective lists.");
      }
    }
    Inventory inv = Bukkit.createInventory(null, 27, cMsg("&c" + p.getName() + "'s Deck #" + deckNum));
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Created inventory.");
    int iter = 0;
    for (ItemStack i : cards) {
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Item " + i.getType().toString() + " added to inventory!");
      i.setAmount(quantity.get(iter));
      if (inv.contains(i)) {
        i.setAmount(i.getAmount() + 1);
      } else
        inv.addItem(i);
      iter++;
    }
    iter = 0;
    p.openInventory(inv);
  }
  
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent e) {
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Title: " + e.getView().getTitle());
    if (e.getView().getTitle().contains("s Deck #")) {
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck closed.");
      ItemStack[] contents = e.getInventory().getContents();
      String[] title = e.getView().getTitle().split("'");
      String[] titleNum = e.getView().getTitle().split("#");
      int deckNum = Integer.parseInt(titleNum[1]);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck num: " + deckNum);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Title: " + title[0]);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Title: " + title[1]);
      UUID id = Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])).getUniqueId();
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] New ID: " + id.toString());
      List<String> serialized = new ArrayList();
      ItemStack[] arrayOfItemStack1; int j = (arrayOfItemStack1 = contents).length;
      for (int i = 0; i < j; i++) {
        ItemStack it = arrayOfItemStack1[i];
        if ((it != null) && (it.getType() != Material.AIR) &&
          (it.getType() == Material.valueOf(getConfig().getString("General.Card-Material")))) {
          if (it.getItemMeta().hasDisplayName()) {
            List<String> lore = it.getItemMeta().getLore();
            String shinyPrefix = getConfig().getString("General.Shiny-Name");
            String rarity = ChatColor.stripColor(lore.get(lore.size() - 1)).replaceAll(shinyPrefix + " ", "");
            String card = getCardName(rarity, it.getItemMeta().getDisplayName());
            String amount = String.valueOf(it.getAmount());
            String shiny = "no";
            if (it.containsEnchantment(Enchantment.ARROW_INFINITE)) {
              shiny = "yes";
            }
            String serializedString = rarity + "," + card + "," + amount + "," + shiny;
            serialized.add(serializedString);
            if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Added " + serializedString + " to deck file.");
            }
          }
          else if (Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])).isOnline()) {
            Player p = Bukkit.getPlayer(ChatColor.stripColor(title[0]));
            World w = p.getWorld();
            w.dropItem(p.getLocation(), it);
          }
        }
      }
      

      getDeckData().set("Decks.Inventories." + id.toString() + "." + deckNum, serialized);
      saveDeckData();
    }
  }
  
  public String getCardName(String rarity, String display) {
    boolean hasPrefix = false;
    String prefix = "";
    if ((getConfig().contains("General.Card-Prefix")) && (getConfig().getString("General.Card-Prefix") != "")) {
      hasPrefix = true;
      prefix = ChatColor.stripColor(getConfig().getString("General.Card-Prefix"));
    }
    String shinyPrefix = getConfig().getString("General.Shiny-Name");
    String cleaned = ChatColor.stripColor(display);
    if (hasPrefix) cleaned = cleaned.replaceAll(prefix, "");
    cleaned = cleaned.replaceAll(shinyPrefix + " ", "");
    String[] cleanedArray = cleaned.split(" ");
    ConfigurationSection cs = getCardsData().getConfigurationSection("Cards." + rarity);
    Set<String> keys = cs.getKeys(false);
    for (String s : keys) {
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] getCardName s: " + s);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] getCardName display: " + display);
      if (cleanedArray.length > 1) {
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] cleanedArray > 1");
        if ((cleanedArray[0] + "_" + cleanedArray[1]).matches(".*\\b" + s + "\\b.*"))
          return s;
        if ((cleanedArray.length > 2) && ((cleanedArray[1] + "_" + cleanedArray[2]).matches(".*\\b" + s + "\\b.*")))
          return s;
        if ((cleanedArray.length > 3) && ((cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3]).matches(".*\\b" + s + "\\b.*")))
          return s;
        if ((cleanedArray.length > 4) && ((cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4]).matches(".*\\b" + s + "\\b.*")))
          return s;
        if ((cleanedArray.length > 5) && ((cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4] + "_" + cleanedArray[5]).matches(".*\\b" + s + "\\b.*")))
          return s;
        if (cleanedArray[0].matches(".*\\b" + s + "\\b.*"))
          return s;
        if (cleanedArray[1].matches(".*\\b" + s + "\\b.*")) {
          return s;
        }
      }
      else if (cleanedArray[0].matches(".*\\b" + s + "\\b.*")) {
        return s;
      }
    }
    return "None";
  }
  
  public ItemStack createBoosterPack(String name) {
    ItemStack boosterPack = getBlankBoosterPack();
    String packName = name;
    int numNormalCards = getConfig().getInt("BoosterPacks." + name + ".NumNormalCards");int numSpecialCards = getConfig().getInt("BoosterPacks." + name + ".NumSpecialCards");
    String prefix = getConfig().getString("General.BoosterPack-Prefix");
    String normalCardColour = getConfig().getString("Colours.BoosterPackNormalCards");
    String extraCardColour = getConfig().getString("Colours.BoosterPackExtraCards");
    String loreColour = getConfig().getString("Colours.BoosterPackLore");
    String nameColour = getConfig().getString("Colours.BoosterPackName");
    String normalRarity = getConfig().getString("BoosterPacks." + name + ".NormalCardRarity");
    String specialRarity = getConfig().getString("BoosterPacks." + name + ".SpecialCardRarity");
    String extraRarity = "";int numExtraCards = 0;
    boolean hasExtraRarity = false;
    if ((getConfig().contains("BoosterPacks." + name + ".ExtraCardRarity")) && (getConfig().contains("BoosterPacks." + name + ".NumExtraCards"))) {
      hasExtraRarity = true;
      extraRarity = getConfig().getString("BoosterPacks." + name + ".ExtraCardRarity");
      numExtraCards = getConfig().getInt("BoosterPacks." + name + ".NumExtraCards");
    }
    String specialCardColour = getConfig().getString("Colours.BoosterPackSpecialCards");
    ItemMeta pMeta = boosterPack.getItemMeta();
    pMeta.setDisplayName(cMsg(prefix + nameColour + packName.replaceAll("_", " ")));
    List<String> lore = new ArrayList();
    lore.add(cMsg(normalCardColour + numNormalCards + loreColour + " " + normalRarity.toUpperCase()));
    if (hasExtraRarity) lore.add(cMsg(extraCardColour + numExtraCards + loreColour + " " + extraRarity.toUpperCase()));
    lore.add(cMsg(specialCardColour + numSpecialCards + loreColour + " " + specialRarity.toUpperCase()));
    pMeta.setLore(lore);
    if (getConfig().getBoolean("General.Hide-Enchants", true)) pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    boosterPack.setItemMeta(pMeta);
    boosterPack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
    return boosterPack;
  }
  
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if ((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
      Player p = event.getPlayer();
      if ((p.getItemInHand().getType() == Material.valueOf(getConfig().getString("General.BoosterPack-Material"))) && 
        (event.getPlayer().hasPermission("fwtc.openboosterpack"))) {
        if (p.getGameMode() != GameMode.CREATIVE) {
          if (p.getItemInHand().containsEnchantment(Enchantment.ARROW_INFINITE)) {
            if (p.getItemInHand().getAmount() > 1) p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1); else
              p.getInventory().removeItem(p.getItemInHand());
            ItemStack boosterPack = event.getItem();
            ItemMeta packMeta = boosterPack.getItemMeta();
            List<String> lore = packMeta.getLore();
            boolean hasExtra = false;
            if (lore.size() > 2) hasExtra = true;
            String[] line1 = lore.get(0).split(" ", 2);
            String[] line2 = lore.get(1).split(" ", 2);
            String[] line3 = { "" };
            if (hasExtra) line3 = lore.get(2).split(" ", 2);
            int normalCardAmount = Integer.parseInt(ChatColor.stripColor(line1[0]));
            int specialCardAmount = Integer.parseInt(ChatColor.stripColor(line2[0]));
            int extraCardAmount = 0;
            if (hasExtra) extraCardAmount = Integer.parseInt(ChatColor.stripColor(line3[0]));
            p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.OpenBoosterPack")));
            for (int i = 0; i < normalCardAmount; i++) {
              if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(generateCard(WordUtils.capitalizeFully(line1[1]), false));
              }
              else {
                World curWorld = p.getWorld();
                if (p.getGameMode() == GameMode.SURVIVAL) {
                  curWorld.dropItem(p.getLocation(), generateCard(WordUtils.capitalizeFully(line1[1]), false));
                }
              }
            }
            for (int i = 0; i < specialCardAmount; i++) {
              if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(generateCard(WordUtils.capitalizeFully(line2[1]), false));
              }
              else {
                World curWorld = p.getWorld();
                if (p.getGameMode() == GameMode.SURVIVAL) {
                  curWorld.dropItem(p.getLocation(), generateCard(WordUtils.capitalizeFully(line2[1]), false));
                }
              }
            }
            if (hasExtra) for (int i = 0; i < extraCardAmount; i++) {
                if (p.getInventory().firstEmpty() != -1) {
                  p.getInventory().addItem(generateCard(WordUtils.capitalizeFully(line3[1]), false));
                }
                else {
                  World curWorld = p.getWorld();
                  if (p.getGameMode() == GameMode.SURVIVAL) {
                    curWorld.dropItem(p.getLocation(), generateCard(WordUtils.capitalizeFully(line3[1]), false));
                  }
                }
              }
          }
        }
        else
          event.getPlayer().sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCreative")));
      }
      if (p.getItemInHand().getType() == Material.valueOf(getConfig().getString("General.Deck-Material"))) {
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Deck material...");
        if (p.getGameMode() != GameMode.CREATIVE) {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Not creative...");
          if (p.getItemInHand().containsEnchantment(Enchantment.DURABILITY)) {
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Has enchant...");
            if (p.getItemInHand().getEnchantmentLevel(Enchantment.DURABILITY) == 10) {
              if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Enchant is level 10...");
              String name = p.getItemInHand().getItemMeta().getDisplayName();
              String[] nameSplit = name.split("#");
              int num = Integer.parseInt(nameSplit[1]);
              openDeck(p, num);
            }
          }
        }
      }
    }
  }
  
  public String calculateRarity(EntityType e, boolean alwaysDrop)
  {
    int shouldItDrop = this.r.nextInt(100) + 1;int bossRarity = 0;
    String type = "";
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] shouldItDrop Num: " + shouldItDrop);
    if (isMobHostile(e)) { if (!alwaysDrop) { if (shouldItDrop > getConfig().getInt("Chances.Hostile-Chance")) return "None"; type = "Hostile"; } else { type = "Hostile";
      } } else if (isMobNeutral(e)) { if (!alwaysDrop) { if (shouldItDrop > getConfig().getInt("Chances.Neutral-Chance")) return "None"; type = "Neutral"; } else { type = "Neutral";
      } } else if (isMobPassive(e)) { if (!alwaysDrop) { if (shouldItDrop > getConfig().getInt("Chances.Passive-Chance")) return "None"; type = "Passive"; } else { type = "Passive";
      } } else if (isMobBoss(e)) {
      if (!alwaysDrop) {
        if (shouldItDrop > getConfig().getInt("Chances.Boss-Chance")) return "None";
        if (getConfig().getBoolean("Chances.Boss-Drop")) bossRarity = getConfig().getInt("Chances.Boss-Drop-Rarity");
      }
      type = "Boss";
    } else return "None";
    ConfigurationSection rarities = getConfig().getConfigurationSection("Rarities");
    Set<String> rarityKeys = rarities.getKeys(false);
    Map<String, Integer> rarityChances = new HashMap();
    Map<Integer, String> rarityIndexes = new HashMap();
    int i = 0;int mini = 0;int random = this.r.nextInt(100000) + 1;
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Random Card Num: " + random);
    if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Type: " + type);
    for (String key : rarityKeys) {
      rarityIndexes.put(i, key);
      i++;
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + i + ", " + key);
      if ((getConfig().contains("Chances." + key + "." + StringUtils.capitalize(e.getName()))) && (mini == 0)) {
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mini: " + i);
        mini = i;
      }
      int chance = getConfig().getInt("Chances." + key + "." + type, -1);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Keys: " + key + ", " + chance + ", i=" + i);
      rarityChances.put(key, chance);
    }
    
    if (mini != 0)
    {
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mini: " + mini);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] i: " + i);
      while (i >= mini) {
        i--;
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] i: " + i);
        int chance = getConfig().getInt("Chances." + rarityIndexes.get(i) + "." + StringUtils.capitalize(e.getName()), -1);
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Chance: " + chance);
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Rarity: " + rarityIndexes.get(i));
        if (chance > 0) {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Chance > 0");
          if (random <= chance) {
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Random <= Chance");
            return rarityIndexes.get(i);
          }
          
        }
      }
    }
    else
    {
      while (i > 0) { int chance;
        i--;
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Final loop iteration " + i);
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Iteration " + i + " in HashMap is: " + rarityIndexes.get(i) + ", " + getConfig().getString(new StringBuilder("Rarities.").append(rarityIndexes.get(i)).append(".Name").toString()));
        chance = getConfig().getInt("Chances." + rarityIndexes.get(i) + "." + type, -1);
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + getConfig().getString(new StringBuilder("Rarities.").append(rarityIndexes.get(i)).append(".Name").toString()) + "'s chance of dropping: " + chance + " out of 100,000");
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] The random number we're comparing that against is: " + random);
        if ((chance > 0) && 
          (random <= chance)) {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Yup, looks like " + random + " is definitely lower than " + chance + "!");
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Giving a " + getConfig().getString(new StringBuilder("Rarities.").append(rarityIndexes.get(i)).append(".Name").toString()) + " card.");
          return rarityIndexes.get(i);
        }
      }
    }
    
    return "None";
  }
  
  public boolean isOnList(Player p) {
    List<String> playersOnList = getConfig().getStringList("Blacklist.Players");
    return playersOnList.contains(p.getName());
  }
  
  public void addToList(Player p) {
    List<String> playersOnList = getConfig().getStringList("Blacklist.Players");
    playersOnList.add(p.getName());
    getConfig().set("Blacklist.Players", null);
    getConfig().set("Blacklist.Players", playersOnList);
    saveConfig();
  }
  
  public void removeFromList(Player p) {
    List<String> playersOnList = getConfig().getStringList("Blacklist.Players");
    playersOnList.remove(p.getName());
    getConfig().set("Blacklist.Players", null);
    getConfig().set("Blacklist.Players", playersOnList);
    saveConfig();
  }
  
  public char blacklistMode() {
    if (getConfig().getBoolean("Blacklist.Whitelist-Mode")) return 'w';
    return 'b';
  }
  
  @EventHandler
  public void onEntityDeath(EntityDeathEvent e) {
    boolean drop = false;
    String worldName = "";
    List<String> worlds = new ArrayList<String>();
    if ((e.getEntity().getKiller() != null)) {
      Player p = e.getEntity().getKiller();
      if ((isOnList(p)) && (blacklistMode() == 'b')) { drop = false;
      } else if ((!isOnList(p)) && (blacklistMode() == 'b')) { drop = true;
      } else drop = (isOnList(p)) && (blacklistMode() == 'w');
      worldName = p.getWorld().getName();
      worlds = getConfig().getStringList("World-Blacklist");
      /*if (this.hasMobArena) {
        int i = 0;
        if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mob Arena checks starting.");
        if ((this.am.getArenas() != null) && (!this.am.getArenas().isEmpty())) {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] There is at least 1 arena!");
          for (Arena arena : this.am.getArenas()) {
            i++;
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] For arena #" + i + "...");
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] In arena?: " + arena.inArena(p));
            if ((arena.inArena(p)) || (arena.inLobby(p))) {
              if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Killer is in an arena/lobby, so let's mess with the drops.");
              if (getConfig().getBoolean("PluginSupport.MobArena.Disable-In-Arena")) drop = false;
              if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Drops are now: " + drop);
            }
            else if (getConfig().getBoolean("General.Debug-Mode")) { System.out.println("[Cards] Killer is not in this arena!");
            }
          }
        }
      }*/
    }
    
    if ((drop) && 
      (!worlds.contains(worldName))) {
      String rare = calculateRarity(e.getEntityType(), false);
      if ((getConfig().getBoolean("Chances.Boss-Drop")) && (isMobBoss(e.getEntityType()))) rare = getConfig().getString("Chances.Boss-Drop-Rarity");
      boolean cancelled = false;
      if (rare != "None") {
        if ((getConfig().getBoolean("General.Spawner-Block")) && 
          (e.getEntity().getCustomName() != null) && 
          (e.getEntity().getCustomName().equals(getConfig().getString("General.Spawner-Mob-Name"))))
        {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mob came from spawner, not dropping card.");
          cancelled = true;
        }
        

        if (!cancelled)
        {
          if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Successfully generated card.");
          if (generateCard(rare, false) != null) e.getDrops().add(generateCard(rare, false));
        }
      }
    }
  }
  
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent e)
  {
    if ((getConfig().getBoolean("General.Player-Drops-Card")) && (getConfig().getBoolean("General.Auto-Add-Players")))
    {
      org.bukkit.entity.Entity killer = e.getEntity().getKiller();
      if (killer != null) {
        ConfigurationSection rarities = getConfig().getConfigurationSection("Rarities");
        Set<String> rarityKeys = rarities.getKeys(false);
        String k = null;
        for (String key : rarityKeys) {
          if (getCardsData().contains("Cards." + key + "." + e.getEntity().getName()))
          {
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + key);
            k = key;
          }
        }
        if (k != null)
        {
          int rndm = this.r.nextInt(100) + 1;
          if (rndm <= getConfig().getInt("General.Player-Drops-Card-Rarity")) {
            e.getDrops().add(createPlayerCard(e.getEntity().getName(), k, 1, false));
            if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + e.getDrops().toString());
          }
        }
        else
        {
          System.out.println("k is null");
        }
      }
    }
  }
  
  public ItemStack generateCard(String rare, boolean forcedShiny) {
    if (!rare.equals("None")) {
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] generateCard.rare: " + rare);
      ItemStack card = getBlankCard(1);
      reloadCustomConfig();
      ConfigurationSection cardSection = getCardsData().getConfigurationSection("Cards." + rare);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] generateCard.cardSection: " + getCardsData().contains(new StringBuilder("Cards.").append(rare).toString()));
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] generateCard.rarity: " + rare);
      Set<String> cards = cardSection.getKeys(false);
      List<String> cardNames = new ArrayList();
      cardNames.addAll(cards);
      int cIndex = this.r.nextInt(cardNames.size());
      String cardName = cardNames.get(cIndex);
      boolean hasShinyVersion = getCardsData().getBoolean("Cards." + rare + "." + cardName + ".Has-Shiny-Version");boolean isShiny = false;
      if (hasShinyVersion) {
        int shinyRandom = this.r.nextInt(100) + 1;
        if (shinyRandom <= getConfig().getInt("Chances.Shiny-Version-Chance")) isShiny = true;
      }
      if (forcedShiny) isShiny = true;
      String rarityName = rare;
      String rarityColour = getConfig().getString("Rarities." + rare + ".Colour");
      String prefix = getConfig().getString("General.Card-Prefix");
      String series = getCardsData().getString("Cards." + rare + "." + cardName + ".Series");
      String seriesColour = getConfig().getString("Colours.Series");
      String seriesDisplay = getConfig().getString("DisplayNames.Cards.Series", "Series");
      String about = getCardsData().getString("Cards." + rare + "." + cardName + ".About", "None");
      String aboutColour = getConfig().getString("Colours.About");
      String aboutDisplay = getConfig().getString("DisplayNames.Cards.About", "About");
      String type = getCardsData().getString("Cards." + rare + "." + cardName + ".Type");
      String typeColour = getConfig().getString("Colours.Type");
      String typeDisplay = getConfig().getString("DisplayNames.Cards.Type", "Type");
      String info = getCardsData().getString("Cards." + rare + "." + cardName + ".Info");
      String infoColour = getConfig().getString("Colours.Info");
      String infoDisplay = getConfig().getString("DisplayNames.Cards.Info", "Info");
      String shinyPrefix = getConfig().getString("General.Shiny-Name");
      String cost;
      if (getCardsData().contains("Cards." + rare + "." + cardName + ".Buy-Price"))
        cost = String.valueOf(getCardsData().getDouble("Cards." + rare + "." + cardName + ".Buy-Price")); else
        cost = "None";
      ItemMeta cmeta = card.getItemMeta();
      
      if (isShiny) { cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.ShinyTitle").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("%SHINYPREFIX%", shinyPrefix).replaceAll("_", " ")));
      } else
        cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.Title").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("_", " ")));
      List<String> lore = new ArrayList();
      lore.add(cMsg(typeColour + typeDisplay + ": &f" + type));
      if ((info.equals("None")) || (info.equals(""))) {
        lore.add(cMsg(infoColour + infoDisplay + ": &f" + info));
      }
      else
      {
        lore.add(cMsg(infoColour + infoDisplay + ":"));
        lore.addAll(wrapString(info));
      }
      lore.add(cMsg(seriesColour + seriesDisplay + ": &f" + series));
      if (getCardsData().contains("Cards." + rare + "." + cardName + ".About")) lore.add(cMsg(aboutColour + aboutDisplay + ": &f" + about));
      if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + getConfig().getString("General.Shiny-Name") + " " + rarityName)); else
        lore.add(cMsg(rarityColour + ChatColor.BOLD + rarityName));
      cmeta.setLore(lore);
      if (getConfig().getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      card.setItemMeta(cmeta);
      if (isShiny) {
        card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
      }
      return card; }
    return null;
  }
  
  public List<String> wrapString(String s) {
    String parsedString = ChatColor.stripColor(s);
    String addedString = WordUtils.wrap(parsedString, getConfig().getInt("General.Info-Line-Length", 25), "\n", true);
    String[] splitString = addedString.split("\n");
    List<String> finalArray = new ArrayList();
    String[] arrayOfString1; int j = (arrayOfString1 = splitString).length; for (int i = 0; i < j; i++) { String ss = arrayOfString1[i];
      System.out.println(ChatColor.getLastColors(ss));
      finalArray.add(cMsg("&f &7- &f" + ss));
    }
    return finalArray;
  }
  
  public String[] splitStringEvery(String s, int interval) {
    int arrayLength = (int)Math.ceil(s.length() / interval);
    String[] result = new String[arrayLength];
    
    int j = 0;
    int lastIndex = result.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      result[i] = s.substring(j, j + interval);
      j += interval;
    }
    result[lastIndex] = s.substring(j);
    
    return result;
  }
  
  public ItemStack createPlayerCard(String cardName, String rarity, Integer num, boolean forcedShiny) {
    ItemStack card = getBlankCard(num);
    boolean hasShinyVersion = getCardsData().getBoolean("Cards." + rarity + "." + cardName + ".Has-Shiny-Version");boolean isShiny = false;
    if (hasShinyVersion) {
      int shinyRandom = this.r.nextInt(100) + 1;
      if (shinyRandom <= getConfig().getInt("Chances.Shiny-Version-Chance")) isShiny = true;
    }
    if (forcedShiny) isShiny = true;
    String rarityName = rarity;
    String rarityColour = getConfig().getString("Rarities." + rarity + ".Colour");
    String prefix = getConfig().getString("General.Card-Prefix");
    String series = getCardsData().getString("Cards." + rarity + "." + cardName + ".Series");
    String seriesColour = getConfig().getString("Colours.Series");
    String seriesDisplay = getConfig().getString("DisplayNames.Cards.Series", "Series");
    String about = getCardsData().getString("Cards." + rarity + "." + cardName + ".About", "None");
    String aboutColour = getConfig().getString("Colours.About");
    String aboutDisplay = getConfig().getString("DisplayNames.Cards.About", "About");
    String type = getCardsData().getString("Cards." + rarity + "." + cardName + ".Type");
    String typeColour = getConfig().getString("Colours.Type");
    String typeDisplay = getConfig().getString("DisplayNames.Cards.Type", "Type");
    String info = getCardsData().getString("Cards." + rarity + "." + cardName + ".Info");
    String infoColour = getConfig().getString("Colours.Info");
    String infoDisplay = getConfig().getString("DisplayNames.Cards.Info", "Info");
    String shinyPrefix = getConfig().getString("General.Shiny-Name");
    String cost;
    if (getCardsData().contains("Cards." + rarity + "." + cardName + ".Buy-Price"))
      cost = String.valueOf(getCardsData().getDouble("Cards." + rarity + "." + cardName + ".Buy-Price")); else
      cost = "None";
    ItemMeta cmeta = card.getItemMeta();
    
    if (isShiny) { cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.ShinyTitle").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("%SHINYPREFIX%", shinyPrefix).replaceAll("_", " ")));
    } else
      cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.Title").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("_", " ")));
    List<String> lore = new ArrayList();
    lore.add(cMsg(typeColour + typeDisplay + ": &f" + type));
    if ((info.equals("None")) || (info.equals(""))) {
      lore.add(cMsg(infoColour + infoDisplay + ": &f" + info));
    }
    else
    {
      lore.add(cMsg(infoColour + infoDisplay + ":"));
      lore.addAll(wrapString(info));
    }
    lore.add(cMsg(seriesColour + seriesDisplay + ": &f" + series));
    if (getCardsData().contains("Cards." + rarity + "." + cardName + ".About")) lore.add(cMsg(aboutColour + aboutDisplay + ": &f" + about));
    if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + getConfig().getString("General.Shiny-Name") + " " + rarityName)); else
      lore.add(cMsg(rarityColour + ChatColor.BOLD + rarityName));
    cmeta.setLore(lore);
    if (getConfig().getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    card.setItemMeta(cmeta);
    if (isShiny) {
      card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
    }
    return card;
  }
  
  public ItemStack getNormalCard(String cardName, String rarity, Integer num) {
    ItemStack card = getBlankCard(num);
    String rarityName = rarity;
    String rarityColour = getConfig().getString("Rarities." + rarity + ".Colour");
    String prefix = getConfig().getString("General.Card-Prefix");
    String series = getCardsData().getString("Cards." + rarity + "." + cardName + ".Series");
    String seriesColour = getConfig().getString("Colours.Series");
    String seriesDisplay = getConfig().getString("DisplayNames.Cards.Series", "Series");
    String about = getCardsData().getString("Cards." + rarity + "." + cardName + ".About", "None");
    String aboutColour = getConfig().getString("Colours.About");
    String aboutDisplay = getConfig().getString("DisplayNames.Cards.About", "About");
    String type = getCardsData().getString("Cards." + rarity + "." + cardName + ".Type");
    String typeColour = getConfig().getString("Colours.Type");
    String typeDisplay = getConfig().getString("DisplayNames.Cards.Type", "Type");
    String info = getCardsData().getString("Cards." + rarity + "." + cardName + ".Info");
    String infoColour = getConfig().getString("Colours.Info");
    String infoDisplay = getConfig().getString("DisplayNames.Cards.Info", "Info");
    String cost;
    if (getCardsData().contains("Cards." + rarity + "." + cardName + ".Buy-Price"))
      cost = String.valueOf(getCardsData().getDouble("Cards." + rarity + "." + cardName + ".Buy-Price")); else
      cost = "None";
    ItemMeta cmeta = card.getItemMeta();
    

    cmeta.setDisplayName(cMsg(getConfig().getString("DisplayNames.Cards.Title").replaceAll("%PREFIX%", prefix).replaceAll("%COLOUR%", rarityColour).replaceAll("%NAME%", cardName).replaceAll("%COST%", cost).replaceAll("_", " ")));
    List<String> lore = new ArrayList();
    lore.add(cMsg(typeColour + typeDisplay + ": &f" + type));
    if ((info.equals("None")) || (info.equals(""))) {
      lore.add(cMsg(infoColour + infoDisplay + ": &f" + info));
    }
    else
    {
      lore.add(cMsg(infoColour + infoDisplay + ":"));
      lore.addAll(wrapString(info));
    }
    lore.add(cMsg(seriesColour + seriesDisplay + ": &f" + series));
    if (getCardsData().contains("Cards." + rarity + "." + cardName + ".About")) lore.add(cMsg(aboutColour + aboutDisplay + ": &f" + about));
    lore.add(cMsg(rarityColour + ChatColor.BOLD + rarityName));
    cmeta.setLore(lore);
    if (getConfig().getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    card.setItemMeta(cmeta);
    return card;
  }
  
  @EventHandler
  public void onMobSpawn(CreatureSpawnEvent e) {
    if ((!(e.getEntity() instanceof Player)) && 
      (e.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER) && (getConfig().getBoolean("General.Spawner-Block"))) {
      e.getEntity().setCustomName(getConfig().getString("General.Spawner-Mob-Name"));
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Spawner mob renamed.");
      e.getEntity().setRemoveWhenFarAway(true);
    }
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e)
  {
    if (getConfig().getBoolean("General.Auto-Add-Players")) {
      Player p = e.getPlayer();
      GregorianCalendar gc = new GregorianCalendar();
      int date;
      int month;
      int year;
      if (p.hasPlayedBefore()) {
        gc.setTimeInMillis(p.getFirstPlayed());
      }
      else
      {
        gc.setTimeInMillis(System.currentTimeMillis());
      }
      date = gc.get(Calendar.DATE);
      month = gc.get(Calendar.MONTH) + 1;
      year = gc.get(Calendar.YEAR);
      ConfigurationSection rarities = getConfig().getConfigurationSection("Rarities");
      int i = 1;
      Set<String> rarityKeys = rarities.getKeys(false);
      Map<String, Boolean> children = permRarities.getChildren();
      String rarity = getConfig().getString("General.Auto-Add-Player-Rarity");
      for (String key : rarityKeys) {
        i++;
        children.put("fwtc.rarity." + key, Boolean.FALSE);
        permRarities.recalculatePermissibles();
        if (p.hasPermission("fwtc.rarity." + key)) {
          rarity = key;
          break;
        }
      }
      if (p.isOp()) rarity = getConfig().getString("General.Player-Op-Rarity");
      if (!getCardsData().contains("Cards." + rarity + "." + p.getName())) {
        String series = getConfig().getString("General.Player-Series");
        String type = getConfig().getString("General.Player-Type");
        boolean hasShiny = getConfig().getBoolean("General.Player-Has-Shiny-Version");
        getCardsData().set("Cards." + rarity + "." + p.getName() + ".Series", series);
        getCardsData().set("Cards." + rarity + "." + p.getName() + ".Type", type);
        getCardsData().set("Cards." + rarity + "." + p.getName() + ".Has-Shiny-Version", hasShiny);
        if (getConfig().getBoolean("General.American-Mode")) getCardsData().set("Cards." + rarity + "." + p.getName() + ".Info", "Joined " + month + "/" + date + "/" + year); else
          getCardsData().set("Cards." + rarity + "." + p.getName() + ".Info", "Joined " + date + "/" + month + "/" + year);
        saveCardsData();
        reloadCardsData();
      }
    }
  }
  
  public void createCard(Player creator, String rarity, String name, String series, String type, boolean hasShiny, String info, String about) {
    if (!getCardsData().contains("Cards." + rarity + "." + name)) {
      if (name.matches("^[a-zA-Z0-9-_]+$"))
      {
        ConfigurationSection rarities = getCardsData().getConfigurationSection("Cards");
        Set<String> rarityKeys = rarities.getKeys(false);
        String keyToUse = "";
        for (String key : rarityKeys) {
          if (key.equalsIgnoreCase(rarity)) {
            keyToUse = key;
          }
        }
        if (!keyToUse.equals(""))
        {
          String series1 = "";String type1 = "";String info1 = "";
          
          if (series.matches("^[a-zA-Z0-9-_]+$")) series1 = series; else series1 = "None";
          if (type.matches("^[a-zA-Z0-9-_]+$")) type1 = type; else type1 = "None";
          if (info.matches("^[a-zA-Z0-9-_/ ]+$")) info1 = info; else info1 = "None";
          boolean hasShiny1;
          hasShiny1 = hasShiny;
          getCardsData().set("Cards." + rarity + "." + name + ".Series", series1);
          getCardsData().set("Cards." + rarity + "." + name + ".Type", type1);
          getCardsData().set("Cards." + rarity + "." + name + ".Has-Shiny-Version", hasShiny1);
          getCardsData().set("Cards." + rarity + "." + name + ".Info", info1);
          saveCardsData();
          reloadCardsData();
          creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateSuccess").replaceAll("%name%", name).replaceAll("%rarity%", rarity)));
        } else { creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoRarity")));
        } } else { creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateNoName")));
      } } else creator.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateExists")));
  }
  
  public void reloadCustomConfig()
  {
    File file = new File(getDataFolder() + File.separator + "config.yml");
    if (!file.exists())
    {
      getConfig().options().copyDefaults(true);
      saveDefaultConfig();
    }
    reloadConfig();
    reloadDeckData();
    reloadMessagesData();
    reloadCardsData();
    reloadDeckData();
    reloadMessagesData();
    reloadCardsData();
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("fwtc")) {
      if (args.length > 0)
      {
        if (args[0].equalsIgnoreCase("reload")) {
          if (sender.hasPermission("fwtc.reload"))
          {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.Reload")));
            reloadCustomConfig();
            if (getConfig().getBoolean("General.Schedule-Cards")) startTimer();
            return true;
          }
          sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
        }
        else if (args[0].equalsIgnoreCase("toggle")) {
          Player p = (Player)sender;
          if ((isOnList(p)) && (blacklistMode() == 'b')) {
            removeFromList(p);
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleEnabled")));
          }
          else if ((isOnList(p)) && (blacklistMode() == 'w')) {
            removeFromList(p);
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleDisabled")));
          }
          else if ((!isOnList(p)) && (blacklistMode() == 'b')) {
            addToList(p);
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleDisabled")));
          }
          else if ((!isOnList(p)) && (blacklistMode() == 'w')) {
            addToList(p);
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ToggleEnabled")));
          }
        }
        else if (args[0].equalsIgnoreCase("create")) {
          if (sender.hasPermission("fwtc.create")) {
            Player p = (Player)sender;
            if (args.length < 8) {
              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CreateUsage")));
            } else {
              boolean isShiny = false;
              isShiny = (args[5].equalsIgnoreCase("true")) || (args[5].equalsIgnoreCase("yes")) || (args[5].equalsIgnoreCase("y"));
              createCard(p, args[1].replaceAll("_", " "), args[2], args[3].replaceAll("_", " "), args[4].replaceAll("_", " "), isShiny, args[6].replaceAll("_", " "), args[7].replaceAll("_", " "));
            }
          }
        }
        else if (args[0].equalsIgnoreCase("givecard")) {
          if (sender.hasPermission("fwtc.givecard"))
          {
            if (args.length > 2) {
              Player p = (Player)sender;
              if (getCardsData().contains("Cards." + args[1].replaceAll("_", " ") + "." + args[2]))
                p.getInventory().addItem(getNormalCard(args[2], args[1].replaceAll("_", " "), 1)); else {
                sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCard")));
              }
            }
            else {
              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveCardUsage")));
            }
          } else {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          }
        } else if (args[0].equalsIgnoreCase("giveshinycard")) {
          if (sender.hasPermission("fwtc.giveshinycard"))
          {
            if (args.length > 2) {
              Player p = (Player)sender;
              if (getCardsData().contains("Cards." + args[1].replaceAll("_", " ") + "." + args[2]))
                p.getInventory().addItem(createPlayerCard(args[2], args[1].replaceAll("_", " "), 1, true)); else {
                sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCard")));
              }
            }
            else {
              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveCardUsage")));
            }
          } else {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          }
        } else if (args[0].equalsIgnoreCase("giveboosterpack")) {
          if (sender.hasPermission("fwtc.giveboosterpack")) {
            if (args.length > 2) {
              if (getConfig().contains("BoosterPacks." + args[2].replaceAll(" ", "_"))) {
                if (Bukkit.getPlayer(args[1]) != null) {
                  Player p = Bukkit.getPlayer(args[1]);
                  if (p.getInventory().firstEmpty() != -1) {
                    p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoosterPackMsg")));
                    p.getInventory().addItem(createBoosterPack(args[2]));
                  }
                  else
                  {
                    World curWorld = p.getWorld();
                    if (p.getGameMode() == GameMode.SURVIVAL)
                    {
                      p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoosterPackMsg")));
                      curWorld.dropItem(p.getLocation(), createBoosterPack(args[2]));
                    }
                  }
                } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPlayer")));
                } } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoBoosterPack")));
            } else
              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveBoosterPackUsage")));
          } else {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          }
        } else if (args[0].equalsIgnoreCase("getdeck")) {
          if (sender.hasPermission("fwtc.getdeck")) {
            if (args.length > 1) {
              if (StringUtils.isNumeric(args[1])) {
                if (sender.hasPermission("fwtc.decks." + args[1]))
                {
                  Player p = (Player)sender;
                  if (!hasDeck(p, Integer.parseInt(args[1]))) {
                    if (p.getInventory().firstEmpty() != -1) {
                      p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveDeck")));
                      p.getInventory().addItem(createDeck(p, Integer.parseInt(args[1])));
                    }
                    else
                    {
                      World curWorld = p.getWorld();
                      if (p.getGameMode() == GameMode.SURVIVAL)
                      {
                        p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveDeck")));
                        curWorld.dropItem(p.getLocation(), createDeck(p, Integer.parseInt(args[1])));
                      }
                    }
                  } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.AlreadyHaveDeck")));
                } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.MaxDecks")));
                } } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GetDeckUsage")));
            } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GetDeckUsage")));
          } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
        }
        else if (args[0].equalsIgnoreCase("giverandomcard")) {
          if (sender.hasPermission("fwtc.randomcard")) {
            if (args.length > 2) {
              if (Bukkit.getPlayer(args[2]) != null) {
                Player p = Bukkit.getPlayer(args[2]);
                try {
                  EntityType.valueOf(args[1].toUpperCase());
                  String rare = calculateRarity(EntityType.valueOf(args[1].toUpperCase()), true);
                  if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] onCommand.rare: " + rare);
                  sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCardMsg").replaceAll("%player%", p.getName())));
                  if (p.getInventory().firstEmpty() != -1) {
                    p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCard")));
                    if (generateCard(rare, false) != null) p.getInventory().addItem(generateCard(rare, false));
                  }
                  else
                  {
                    World curWorld = p.getWorld();
                    if (p.getGameMode() == GameMode.SURVIVAL)
                    {
                      p.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCard")));
                      if (generateCard(rare, false) != null) curWorld.dropItem(p.getLocation(), generateCard(rare, false));
                    }
                  }
                } catch (IllegalArgumentException e) { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoEntity")));
                }
              } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPlayer")));
              }
            } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveRandomCardUsage")));
          } else {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          }
        } else if (args[0].equalsIgnoreCase("list")) {
          if (sender.hasPermission("fwtc.list")) {
            ConfigurationSection cards = getCardsData().getConfigurationSection("Cards");
            Set<String> cardKeys = cards.getKeys(false);
            String msg = "";
            int i = 0;
            String finalMsg = "";
            for (String key : cardKeys) {
              ConfigurationSection cardsWithKey = getCardsData().getConfigurationSection("Cards." + key);
              Set<String> keyKeys = cardsWithKey.getKeys(false);
              for (String key2 : keyKeys) {
                if (i > 41) {
                  finalMsg = msg + "&7and more!";
                } else
                  msg = msg + "&7" + key2.replaceAll("_", " ") + "&f, ";
                i++;
              }
              sender.sendMessage(cMsg("&6--- " + key + " &7(&f" + i + "&7)&6" + " ---"));
              msg = StringUtils.removeEnd(msg, ", ");
              if (finalMsg.equals("")) sender.sendMessage(cMsg(msg)); else
                sender.sendMessage(cMsg(finalMsg));
              msg = "";
              finalMsg = "";
              i = 0;
            }
          } else {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          } } else { boolean hasExtra;
          if (args[0].equalsIgnoreCase("listpacks")) {
            if (sender.hasPermission("fwtc.listpacks")) {
              ConfigurationSection cards = getConfig().getConfigurationSection("BoosterPacks");
              Set<String> cardKeys = cards.getKeys(false);
              int i = 0;
              sender.sendMessage(cMsg("&6--- Booster Packs ---"));
              boolean hasPrice = false;hasExtra = false;
              for (String key : cardKeys) {
                if (getConfig().contains("BoosterPacks." + key + ".Price")) hasPrice = true;
                if ((getConfig().contains("BoosterPacks." + key + ".ExtraCardRarity")) && (getConfig().contains("BoosterPacks." + key + ".NumExtraCards"))) hasExtra = true;
                i++;
                if (hasPrice) sender.sendMessage(cMsg("&6" + i + ") &e" + key + " &7(&aPrice: " + getConfig().getDouble(new StringBuilder("BoosterPacks.").append(key).append(".Price&7").toString()) + ")")); else
                  sender.sendMessage(cMsg("&6" + i + ") &e" + key));
                if (hasExtra) sender.sendMessage(cMsg("  &7- &f&o" + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumNormalCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".NormalCardRarity").toString()) + ", " + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumExtraCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".ExtraCardRarity").toString()) + ", " + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumSpecialCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".SpecialCardRarity").toString()))); else
                  sender.sendMessage(cMsg("  &7- &f&o" + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumNormalCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".NormalCardRarity").toString()) + ", " + getConfig().getInt(new StringBuilder("BoosterPacks.").append(key).append(".NumSpecialCards").toString()) + " " + getConfig().getString(new StringBuilder("BoosterPacks.").append(key).append(".SpecialCardRarity").toString())));
                hasPrice = false;
                hasExtra = false;
              }
            } else {
              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
            }
          } else if (args[0].equalsIgnoreCase("giveaway")) {
            if (sender.hasPermission("fwtc.giveaway")) {
              if (args.length > 1) {
                ConfigurationSection rarities = getCardsData().getConfigurationSection("Cards");
                Set<String> rarityKeys = rarities.getKeys(false);
                String keyToUse = "";
                for (String key : rarityKeys) {
                  if (key.equalsIgnoreCase(args[1].replaceAll("_", " "))) {
                    keyToUse = key;
                  }
                }
                if (!keyToUse.equals("")) {
                  Bukkit.broadcastMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.Giveaway").replaceAll("%player%", sender.getName()).replaceAll("%rarity%", keyToUse)));
                  for (Player p : Bukkit.getOnlinePlayers()) {
                    ConfigurationSection cards = getCardsData().getConfigurationSection("Cards." + keyToUse);
                    Object cardKeys = cards.getKeys(false);
                    int rIndex = this.r.nextInt(((Set)cardKeys).size());int i = 0;
                    String cardName = "";
                    for (String theCardName : (Set<String>)cardKeys) {
                      if (i == rIndex) { cardName = theCardName; break; }
                      i++;
                    }
                    if (p.getInventory().firstEmpty() != -1) {
                      p.getInventory().addItem(createPlayerCard(cardName, keyToUse, 1, false));
                    }
                    else
                    {
                      World curWorld = p.getWorld();
                      if (p.getGameMode() == GameMode.SURVIVAL)
                      {
                        curWorld.dropItem(p.getLocation(), createPlayerCard(cardName, keyToUse, 1, false));
                      }
                    }
                  }
                } else {
                  sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoRarity")));
                }
              }
              else {
                sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.GiveawayUsage")));
              }
            } else {
              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
            }
          } else if (args[0].equalsIgnoreCase("worth")) {
            if (sender.hasPermission("fwtc.worth")) {
              if (this.hasVault) {
                Player p = (Player)sender;
                if (p.getItemInHand().getType() == Material.valueOf(getConfig().getString("General.Card-Material"))) {
                  ItemStack itemInHand = p.getItemInHand();
                  String itemName = itemInHand.getItemMeta().getDisplayName();
                  if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(itemName);
                  if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(ChatColor.stripColor(itemName));
                  String[] splitName = ChatColor.stripColor(itemName).split(" ");
                  String cardName = "";
                  if (splitName.length > 1) {
                    cardName = splitName[1];
                  } else
                    cardName = splitName[0];
                  if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(cardName);
                  List<String> lore = itemInHand.getItemMeta().getLore();
                  String rarity = ChatColor.stripColor(lore.get(3));
                  if (getConfig().getBoolean("General.Debug-Mode")) System.out.println(rarity);
                  boolean canBuy = false;
                  double buyPrice = 0.0D;
                  if (getCardsData().contains("Cards." + rarity + "." + cardName + ".Buy-Price")) {
                    buyPrice = getCardsData().getDouble("Cards." + rarity + "." + cardName + ".Buy-Price");
                    if (buyPrice > 0.0D) canBuy = true;
                  }
                  if (canBuy) {
                    sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CanBuy").replaceAll("%buyAmount%", String.valueOf(buyPrice))));
                  }
                  else {
                    sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CanNotBuy")));
                  }
                } else {
                  sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NotACard")));
                }
              } else { sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoVault")));
              }
            } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          }
          else if (args[0].equalsIgnoreCase("credits")) {
            sender.sendMessage(cMsg(formatTitle("Credits and Special Thanks")));
            sender.sendMessage(cMsg("&7[&aDeveloper&7] &aLukas Xenoyia Gentle"));
            sender.sendMessage(cMsg("   &7- &6&oxPXenoyia&f, &6&oXenoyia&f, &6&oxPLukas&f, &6&oSnoopDogg&f"));
            sender.sendMessage(cMsg("&7[&eSpecial Thanks&7] XpanD, IrChaos, xtechgamer735, PTsandro, FlyingSquidwolf, iXRaZoRXi, iToxy, TowelieDOH, Miku_Snow, NOBUTSS, doitliketyler, Celebrimbor90, Magz, GypsySix, bumbble, iFosadrink_2, Sunique, TheRealGSD, Zenko, Berkth, TubeCraftXXL, Cra2ytig3r, marcosds13, ericbarbwire, Bonzo"));
          }
          else if (args[0].equalsIgnoreCase("buy")) {
            if (sender.hasPermission("fwtc.buy")) {
              if (this.hasVault) {
                Player p = (Player)sender;
                if (args.length > 1) {
                  if (args[1].equalsIgnoreCase("pack")) {
                    if (args.length > 2) {
                      if (getConfig().contains("BoosterPacks." + args[2])) {
                        double buyPrice = 0.0D;
                        boolean canBuy = false;
                        if (getConfig().contains("BoosterPacks." + args[2] + ".Price")) {
                          buyPrice = getConfig().getDouble("BoosterPacks." + args[2] + ".Price");
                          if (buyPrice > 0.0D) canBuy = true;
                        }
                        if (canBuy) {
                          if (econ.getBalance(p) >= buyPrice) {
                            if (getConfig().getBoolean("PluginSupport.Vault.Closed-Economy")) {
                              econ.withdrawPlayer(p, buyPrice);
                              econ.depositPlayer(getConfig().getString("PluginSupport.Vault.Server-Account"), buyPrice);
                            } else { econ.withdrawPlayer(p, buyPrice); }
                            if (p.getInventory().firstEmpty() != -1) {
                              p.getInventory().addItem(createBoosterPack(args[2]));
                            }
                            else
                            {
                              World curWorld = p.getWorld();
                              if (p.getGameMode() == GameMode.SURVIVAL)
                              {
                                curWorld.dropItem(p.getLocation(), createBoosterPack(args[2]));
                              }
                            }
                            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoughtCard").replaceAll("%amount%", String.valueOf(buyPrice))));
                          } else {
                            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NotEnoughMoney")));
                          }
                        } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CannotBeBought")));
                      } else {
                        sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.PackDoesntExist")));
                      }
                    } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ChoosePack")));
                  }
                  else if (args[1].equalsIgnoreCase("card")) {
                    if (args.length > 2) {
                      if (args.length > 3) {
                        if (getCardsData().contains("Cards." + args[2] + "." + args[3])) {
                          double buyPrice = 0.0D;
                          boolean canBuy = false;
                          if (getCardsData().contains("Cards." + args[2] + "." + args[3] + ".Buy-Price")) {
                            buyPrice = getCardsData().getDouble("Cards." + args[2] + "." + args[3] + ".Buy-Price");
                            if (buyPrice > 0.0D) canBuy = true;
                          }
                          if (canBuy) {
                            if (econ.getBalance(p) >= buyPrice) {
                              if (getConfig().getBoolean("PluginSupport.Vault.Closed-Economy")) {
                                econ.withdrawPlayer(p, buyPrice);
                                econ.depositPlayer(getConfig().getString("PluginSupport.Vault.Server-Account"), buyPrice);
                              } else { econ.withdrawPlayer(p, buyPrice); }
                              if (p.getInventory().firstEmpty() != -1) {
                                p.getInventory().addItem(createPlayerCard(args[3], args[2], 1, false));
                              }
                              else
                              {
                                World curWorld = p.getWorld();
                                if (p.getGameMode() == GameMode.SURVIVAL)
                                {
                                  curWorld.dropItem(p.getLocation(), createPlayerCard(args[3], args[2], 1, false));
                                }
                              }
                              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BoughtCard").replaceAll("%amount%", String.valueOf(buyPrice))));
                            } else {
                              sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NotEnoughMoney")));
                            }
                          } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CannotBeBought")));
                        } else {
                          sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.CardDoesntExist")));
                        }
                      } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ChooseCard")));
                    } else
                      sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.ChooseRarity")));
                  } else
                    sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BuyUsage")));
                } else
                  sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.BuyUsage")));
              } else {
                sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoVault")));
              }
            } else sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoPerms")));
          } else {
            sender.sendMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + getMessagesData().getString("Messages.NoCmd")));
          }
        }
      } else {
        boolean showUsage = getConfig().getBoolean("General.Show-Command-Usage", true);
        sender.sendMessage(cMsg(formatTitle(getConfig().getString("General.Server-Name") + " Trading Cards")));
        if (sender.hasPermission("fwtc.reload")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ReloadUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ReloadHelp")));
        }
        if (sender.hasPermission("fwtc.givecard")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveCardUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveCardHelp")));
        }
        if (sender.hasPermission("fwtc.giveshinycard")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveShinyCardUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveShinyCardHelp")));
        }
        if (sender.hasPermission("fwtc.giverandomcard")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveRandomCardUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveRandomCardHelp")));
        }
        if (sender.hasPermission("fwtc.giveboosterpack")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveBoosterPackUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveBoosterPackHelp")));
        }
        if (sender.hasPermission("fwtc.giveaway")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GiveawayUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GiveawayHelp")));
        }
        if (sender.hasPermission("fwtc.getdeck")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.GetDeckUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.GetDeckHelp")));
        }
        if (sender.hasPermission("fwtc.list")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ListUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ListHelp")));
        }
        if (sender.hasPermission("fwtc.listpacks")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ListPacksUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ListPacksHelp")));
        }
        if (sender.hasPermission("fwtc.toggle")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.ToggleUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.ToggleHelp")));
        }
        if (sender.hasPermission("fwtc.create")) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.CreateUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.CreateHelp")));
        }
        if ((sender.hasPermission("fwtc.buy")) && (this.hasVault)) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.BuyUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.BuyHelp")));
        }
        if ((sender.hasPermission("fwtc.worth")) && (this.hasVault)) {
          sender.sendMessage(cMsg("&7> &3" + getMessagesData().getString("Messages.WorthUsage")));
          if (showUsage) sender.sendMessage(cMsg("   &7- &f&o" + getMessagesData().getString("Messages.WorthHelp")));
        }
        return true;
      }
    }
    return true;
  }
  
  public String cMsg(String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }
  
  public void startTimer() {
    int hours = 1;
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    if ((scheduler.isQueued(this.taskid)) || (scheduler.isCurrentlyRunning(this.taskid))) {
      scheduler.cancelTask(this.taskid);
      if (getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Successfully cancelled task " + this.taskid);
    }
    if (getConfig().getInt("General.Schedule-Card-Time-In-Hours") < 1) hours = 1; else
      hours = getConfig().getInt("General.Schedule-Card-Time-In-Hours");
    String tmessage = getMessagesData().getString("Messages.TimerMessage").replaceAll("%hour%", String.valueOf(hours));
    Bukkit.broadcastMessage(cMsg(getMessagesData().getString("Messages.Prefix") + " " + tmessage));
    this.taskid = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Task running..");
        if (TradingCards.this.getConfig().getBoolean("General.Schedule-Cards")) {
          if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Schedule cards is true.");
          ConfigurationSection rarities = TradingCards.this.getCardsData().getConfigurationSection("Cards");
          Set<String> rarityKeys = rarities.getKeys(false);
          String keyToUse = "";
          for (String key : rarityKeys) {
            if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Rarity key: " + key);
            if (key.equalsIgnoreCase(TradingCards.this.getConfig().getString("General.Schedule-Card-Rarity"))) {
              keyToUse = key;
            }
          }
          if (TradingCards.this.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] keyToUse: " + keyToUse);
          if (!keyToUse.equals("")) {
            Bukkit.broadcastMessage(TradingCards.this.cMsg(TradingCards.this.getMessagesData().getString("Messages.Prefix") + " " + TradingCards.this.getMessagesData().getString("Messages.ScheduledGiveaway")));
            for (Player p : Bukkit.getOnlinePlayers()) {
              ConfigurationSection cards = TradingCards.this.getCardsData().getConfigurationSection("Cards." + keyToUse);
              Set<String> cardKeys = cards.getKeys(false);
              int rIndex = TradingCards.this.r.nextInt(cardKeys.size());int i = 0;
              String cardName = "";
              for (String theCardName : cardKeys) {
                if (i == rIndex) { cardName = theCardName; break; }
                i++;
              }
              if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(TradingCards.this.createPlayerCard(cardName, keyToUse, 1, false));
              }
              else
              {
                World curWorld = p.getWorld();
                if (p.getGameMode() == GameMode.SURVIVAL)
                {
                  curWorld.dropItem(p.getLocation(), TradingCards.this.createPlayerCard(cardName, keyToUse, 1, false));
                }
              }
            }
          }
        }
      }
    }, hours * 20 * 60 * 60, hours * 20 * 60 * 60);
  }
  
  public String formatTitle(String title) {
    String line = "&7[&foOo&7]&f____________________________________________________&7[&foOo&7]&f";
    int pivot = line.length() / 2;
    String center = "&7.< &3" + title + "&7" + " >.&f";
    String out = line.substring(0, Math.max(0, pivot - center.length() / 2));
    out = out + center + line.substring(pivot + center.length() / 2);
    return out;
  }
}