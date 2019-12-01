package org.werti.jumpn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Class for managing all players that have a running instance of JumpN
 *
 * Mostly used to have a expendable interface
 */
public class JumpNPlayer
{
  private Player player;

  /**
   * Types of messages and their corresponding color
   */
  public enum MessageType
  {
    Info(ChatColor.GRAY),
    Positive(ChatColor.GREEN),
    Negative(ChatColor.RED),
    Error(ChatColor.DARK_RED);

    private ChatColor color;

    MessageType(ChatColor color)
    {
      this.color = color;
    }

    public ChatColor getColor()
    {
      return color;
    }
  }

  JumpNPlayer(Player player)
  {
    this.player = player;
  }

  public Player getPlayer()
  {
    return player;
  }

  public Location getLocation()
  {
    return player.getLocation();
  }

  public World getWorld()
  {
    return player.getWorld();
  }

  public Vector toVector()
  {
    return player.getLocation().toVector();
  }

  public String getName()
  {
    return player.getName();
  }

  public void sendMessage(MessageType messageType, String message)
  {
    sendMessage(player, messageType, message);
  }

  public static void sendMessage(Player player, MessageType messageType, String message)
  {
    String formattedMessage = String.format("%s %s%s", Globals.ChatPrefix, messageType.getColor(), message);

    player.sendMessage(formattedMessage);
  }
}