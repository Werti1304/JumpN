package org.werti.jumpn.Events.Jumpn;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.werti.jumpn.Globals;
import org.werti.jumpn.JumpN;
import org.werti.jumpn.JumpNPlayer;

/**
 * All nonessential components to the Jump'n'run are covered here (e.g. sounds, messages, etc.)
 */
public class JumpnEventListener implements Listener
{
  @EventHandler(priority = EventPriority.HIGH)
  public void onStart(StartEvent startEvent)
  {
    JumpN jumpN = JumpN.getFrom(startEvent.getPlayer());
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.YELLOW, "Started new jump'n'run, have fun!");

    JumpNPlayer.sendMessage(startEvent.getPlayer(), JumpNPlayer.MessageType.Info,
                            String.format("You've started a new Jump and Run! The score you must reach to win is %d", Globals.winScore));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlatformReached(PlatformReachedEvent platformReachedEvent)
  {
    Player player = platformReachedEvent.getPlayer();
    String score = Integer.toString(platformReachedEvent.getScore());

    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.f);

    // Shows current score in actionbar
    JumpN jumpN = JumpN.getFrom(player);
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.YELLOW, score);

    JumpNPlayer.sendMessage(player, JumpNPlayer.MessageType.Info,
                            String.format("Platform %s reached!", score));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onWin(WinEvent winEvent)
  {
    Player player = winEvent.getPlayer();

    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.f);

    JumpN jumpN = JumpN.getFrom(player);
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.GREEN, "You win!");

    JumpNPlayer.sendMessage(player, JumpNPlayer.MessageType.Positive,
                            "You won! Congratulations!");
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onLose(LoseEvent loseEvent)
  {
    Player player = loseEvent.getPlayer();

    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5f, 1.f);

    JumpN jumpN = JumpN.getFrom(player);
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.RED, "You lost with a score of " + loseEvent.getScore() + "!");

    JumpNPlayer.sendMessage(player, JumpNPlayer.MessageType.Negative,
                            String.format("You lost with a score of %s%d%s!",
                                          ChatColor.GOLD,
                                          loseEvent.getScore(),
                                          JumpNPlayer.MessageType.Negative.getColor()));
  }
}
