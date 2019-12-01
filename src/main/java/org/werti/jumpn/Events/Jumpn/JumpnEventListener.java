package org.werti.jumpn.Events.Jumpn;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.werti.jumpn.JumpN;

/**
 * All nonessential components to the Jump'n'run are covered here (e.g. sounds, messages, etc.)
 */
public class JumpnEventListener implements Listener
{
  @EventHandler(priority = EventPriority.HIGH)
  public void onStart(StartEvent startEvent)
  {
    Player player = startEvent.getPlayer();

    JumpN jumpN = JumpN.getFrom(player);

    // Plays sound for starting a new jumpnrun
    player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.f);

    // Shows started jumpnrun message in the actionbar
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.YELLOW, "Started new jump'n'run, have fun!");
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlatformReached(PlatformReachedEvent platformReachedEvent)
  {
    Player player = platformReachedEvent.getPlayer();
    String score = Integer.toString(platformReachedEvent.getScore());

    // Plays sound for reaching a new platform
    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.f);

    // Shows current score in actionbar
    JumpN jumpN = JumpN.getFrom(player);
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.YELLOW, score);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onWin(WinEvent winEvent)
  {
    Player player = winEvent.getPlayer();

    // Plays level-up sound for winning
    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.f);

    // Shows "You win!" in the actionbar
    JumpN jumpN = JumpN.getFrom(player);
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.GREEN, "You win!");
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onLose(LoseEvent loseEvent)
  {
    Player player = loseEvent.getPlayer();

    // Plays sound for loosing
    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5f, 1.f);

    // Shows lose-message in the actionbar
    JumpN jumpN = JumpN.getFrom(player);
    jumpN.updateActionBar(net.md_5.bungee.api.ChatColor.RED, String.format("You lost with a score of %d!", loseEvent.getScore()));
  }
}
