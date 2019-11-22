package org.werti.jumpn;

import com.google.gson.internal.$Gson$Preconditions;
import jdk.internal.jline.internal.Nullable;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import sun.tools.java.Environment;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Class for managing all players that are currently playing the jumpnrun
 *
 * Adding and getting could've been done dynamically, but it's not needed for our purposes
 */
public class JumpNPlayer
{
  public static ArrayList<JumpNPlayer> jumpNPlayerList = new ArrayList<>();

  private Player player;

  /**
   * Own function for adding, because I think it's more fitting for a Class that adds itself
   * to a static list.
   * @param player Player to add as JumpPlayer
   * @return Newly created JumpPlayer
   */
  public static JumpNPlayer Add(Player player)
  {
    JumpNPlayer newPlayer = new JumpNPlayer(player);

    jumpNPlayerList.add(newPlayer);

    return newPlayer;
  }

  /**
   * @param jumpNPlayer JumpPlayer to be removed as a JumpNPlayer
   */
  public static void Remove(JumpNPlayer jumpNPlayer)
  {
    jumpNPlayerList.remove(jumpNPlayer);
  }

  /**
   * @param player Player to search for
   * @return First player it can find
   */
  @Nullable
  public static JumpNPlayer GetJumpNPlayer(Player player)
  {
    final Optional<JumpNPlayer> result = jumpNPlayerList.stream()
        .filter(item -> item.player.equals(player))
        .findAny();

    // For readability and intellijs conscience
    return result.orElse(null);
  }

  private JumpNPlayer(Player player)
  {
    this.player = player;
  }

  public Player GetPlayer()
  {
    return player;
  }

  enum MessageType
  {
    Info(Color.GRAY),
    Positive(Color.GREEN),
    Warning(Color.ORANGE),
    Error(Color.RED);

    private Color color;

    MessageType(org.bukkit.Color color)
    {
      this.color = color;
    }

    public Color getColor()
    {
      return color;
    }
  }

  public void sendMessage(MessageType messageType, String message)
  {
    String formattedMessage = String.format("%s %s%s", Globals.ChatPrefix, messageType.getColor(), message);

    player.sendMessage(formattedMessage);
  }

}
