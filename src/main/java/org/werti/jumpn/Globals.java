package org.werti.jumpn;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


/**
 * All Globals are available after the plugin has been enabled
 */
public class Globals
{
  public static JavaPlugin plugin;

  public static Server bukkitServer;

  public static ConsoleCommandSender consoleCommandSender;

  public static Logger logger;

  public static String ChatPrefix = String.format("%s[%sJumpN%s]", ChatColor.GRAY, ChatColor.GREEN, ChatColor.GRAY);

  // Overwrites log-finer
  public static boolean DEBUG = true;

  public static final int winScore = 10;

  public static final int maxRandomPlatformTries = 10;

  public static final Material platformMaterial = Material.SNOW_BLOCK;
  public static final Material winningPlatformMaterial = Material.GOLD_BLOCK;

  public static void debug(String message)
  {
    if(DEBUG)
    {
      consoleCommandSender.sendMessage(ChatColor.LIGHT_PURPLE + message);
    }
    else
    {
      logger.finer(message);
    }
  }

}
