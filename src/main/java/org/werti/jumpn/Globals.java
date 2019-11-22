package org.werti.jumpn;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


/**
 * All Globals are available after the plugin has been enabled
 */
public class Globals
{
  public static JavaPlugin plugin;

  public static Server bukkitServer;

  public static Logger logger;

  public static String ChatPrefix = String.format("%s[%sJumpN%s]", ChatColor.GRAY, ChatColor.GREEN, ChatColor.GRAY);

}
