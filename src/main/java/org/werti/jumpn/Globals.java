package org.werti.jumpn;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
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

  public static final String ChatPrefix = String.format("%s[%sJumpN%s]", ChatColor.GRAY, ChatColor.GREEN, ChatColor.GRAY);

  public static final String pluginName = "JumpN";

  // Overwrites log-finer
  public static boolean DEBUG = true;

  public static final int winScore = 10;

  public static final int maxRandomPlatformTries = 10;

  public static final Material platformMaterial = Material.GLASS;
  public static final Material winningPlatformMaterial = Material.GOLD_BLOCK;

  public static void debug(String playerName, String message)
  {
    if(DEBUG)
    {
      consoleCommandSender.sendMessage(String.format("[%s - %s] %s%s", pluginName, playerName, ChatColor.LIGHT_PURPLE, message));
    }
    else
    {
      logger.finer(String.format("[%s-%s] %s", pluginName, playerName, message));
    }
  }

  public static void debug(String message)
  {
    if(DEBUG)
    {
      consoleCommandSender.sendMessage(String.format("[%s] %s%s", pluginName, ChatColor.LIGHT_PURPLE, message));
    }
    else
    {
      logger.finer(String.format("[%s] %s", pluginName, message));
    }
  }

}
