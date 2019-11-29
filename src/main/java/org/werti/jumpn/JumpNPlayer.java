package org.werti.jumpn;

import jdk.internal.jline.internal.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Class for managing all players that are currently playing the jumpnrun
 *
 * Adding and getting could've been done dynamically, but it's not needed for our purposes
 */
public class JumpNPlayer
{

  private Player player;

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