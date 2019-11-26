package org.werti.jumpn.Events.Jumpn;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.werti.jumpn.Globals;
import org.werti.jumpn.JumpNPlayer;


/**
 * All nonessential components to the Jump'n'run are covered here (e.g. sounds, messages, etc.)
 */
public class JumpnEventListener implements Listener
{
  @EventHandler(priority = EventPriority.HIGH)
  public void onStart(StartEvent startEvent)
  {
    JumpNPlayer.sendMessage(startEvent.getPlayer(), JumpNPlayer.MessageType.Info,
                            String.format("You've started a new Jump and Run! The score you must reach to win is %d", Globals.winScore));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlatformReached(PlatformReachedEvent platformReachedEvent)
  {
    JumpNPlayer.sendMessage(platformReachedEvent.getPlayer(), JumpNPlayer.MessageType.Info,
                            String.format("Platform %d reached!", platformReachedEvent.getScore()));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onWin(WinEvent winEvent)
  {
    JumpNPlayer.sendMessage(winEvent.getPlayer(), JumpNPlayer.MessageType.Positive,
                            "You won! Congratulations!");
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onLose(LoseEvent loseEvent)
  {
    JumpNPlayer.sendMessage(loseEvent.getPlayer(), JumpNPlayer.MessageType.Negative,
                            String.format("You lost with a score of %s%d%s!",
                                          ChatColor.GOLD,
                                          loseEvent.getScore(),
                                          JumpNPlayer.MessageType.Negative.getColor()));
  }
}
