/*    */ package com.xpgaming.tradingcards;
/*    */ 
/*    */ import com.palmergames.bukkit.towny.event.NewTownEvent;
/*    */ import com.palmergames.bukkit.towny.object.Resident;
/*    */ import com.palmergames.bukkit.towny.object.Town;
/*    */ import java.io.PrintStream;
/*    */ import java.util.GregorianCalendar;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ 
/*    */ public class TownyListener
/*    */   implements org.bukkit.event.Listener
/*    */ {
/*    */   public static TradingCards plugin;
/*    */   
/* 17 */   public TownyListener(TradingCards plugin) { plugin = plugin; }
/*    */   
/*    */   @org.bukkit.event.EventHandler
/*    */   public void onNewTown(NewTownEvent e) {
/* 21 */     if (plugin.getConfig().getBoolean("PluginSupport.Towny.Towny-Enabled")) {
/* 22 */       if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
/* 23 */         if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Towny detected, starting card creation..");
/* 24 */         GregorianCalendar gc = new GregorianCalendar();
/*    */         
/* 26 */         gc.setTimeInMillis(System.currentTimeMillis());
/* 27 */         int date = gc.get(5);
/* 28 */         int month = gc.get(2) + 1;
/* 29 */         int year = gc.get(1);
/* 30 */         String townRarity = plugin.getConfig().getString("PluginSupport.Towny.Town-Rarity");
/* 31 */         String townName = e.getTown().getName();
/* 32 */         if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + townName);
/* 33 */         String townSeries = plugin.getConfig().getString("PluginSupport.Towny.Town-Series");
/* 34 */         if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + townSeries);
/* 35 */         String townType = plugin.getConfig().getString("PluginSupport.Towny.Town-Type");
/* 36 */         if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + townType);
/* 37 */         boolean hasShiny = plugin.getConfig().getBoolean("PluginSupport.Towny.Has-Shiny");
/* 38 */         String prefix = plugin.getConfig().getString("General.Card-Prefix");
/* 39 */         if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] " + prefix);
/* 40 */         if (plugin.getConfig().contains("Cards." + townRarity + "." + townName)) {
/* 41 */           System.out.println("[Cards] Town already exists!");
/* 42 */           if (plugin.getConfig().getBoolean("PluginSupport.Towny.Allow-Duplicates")) {
/* 43 */             int num = 1;
/* 44 */             String dPrefix = plugin.getConfig().getString("PluginSupport.Towny.Town-Duplicate-Prefix").replaceAll("%num%", String.valueOf(num));
/* 45 */             String dSuffix = plugin.getConfig().getString("PluginSupport.Towny.Town-Duplicate-Suffix").replaceAll("%num%", String.valueOf(num));
/* 46 */             while (plugin.getConfig().contains("Cards." + townRarity + "." + dPrefix + townName + dSuffix)) {
/* 47 */               num++;
/* 48 */               dPrefix = plugin.getConfig().getString("PluginSupport.Towny.Town-Duplicate-Prefix").replaceAll("%num%", String.valueOf(num));
/* 49 */               dSuffix = plugin.getConfig().getString("PluginSupport.Towny.Town-Duplicate-Suffix").replaceAll("%num%", String.valueOf(num));
/* 50 */               if (num > 100) {
/* 51 */                 System.out.println("[Cards] Something went wrong!");
/* 52 */                 break;
/*    */               }
/*    */             }
/* 55 */             if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Let's do this!");
/* 56 */             Player p = org.bukkit.Bukkit.getPlayer(e.getTown().getMayor().getName());
/* 57 */             if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mayor name: " + e.getTown().getMayor().getName());
/* 58 */             String townInfo = "";
/* 59 */             if (plugin.getConfig().getBoolean("General.American-Mode")) townInfo = "Created " + month + "/" + date + "/" + year; else
/* 60 */               townInfo = "Created " + date + "/" + month + "/" + year;
/* 61 */             plugin.createCard(p, townRarity, dPrefix + townName + dSuffix, townSeries, townType, hasShiny, townInfo, "Founder: " + p.getName());
/*    */           }
/*    */         }
/*    */         else {
/* 65 */           if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Let's do this!");
/* 66 */           Player p = org.bukkit.Bukkit.getPlayer(e.getTown().getMayor().getName());
/* 67 */           if (plugin.getConfig().getBoolean("General.Debug-Mode")) System.out.println("[Cards] Mayor name: " + e.getTown().getMayor().getName());
/* 68 */           String townInfo = "";
/* 69 */           if (plugin.getConfig().getBoolean("General.American-Mode")) townInfo = "Created " + month + "/" + date + "/" + year; else
/* 70 */             townInfo = "Created " + date + "/" + month + "/" + year;
/* 71 */           plugin.createCard(p, townRarity, townName, townSeries, townType, hasShiny, townInfo, "Founder: " + p.getName());
/*    */         }
/* 73 */       } else { System.out.println("[Cards] Cannot detect Towny!");
/*    */       }
/*    */     }
/*    */   }
/*    */ }


/* Location:              C:\Users\Lukas Xenoyia Gentle\Downloads\xPTradingCards-2.9.jar!\com\xpgaming\tradingcards\TownyListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */