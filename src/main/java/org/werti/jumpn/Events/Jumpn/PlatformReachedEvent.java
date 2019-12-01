package org.werti.jumpn.Events.Jumpn;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that gets called when someone reaches another platform in the jump'n'run.
 */
public class PlatformReachedEvent extends Event
{
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final int score;

  public PlatformReachedEvent(Player player, int score)
  {
    this.player = player;
    this.score = score;
  }

  @Override
  @NotNull
  public HandlerList getHandlers()
  {
    return HANDLERS;
  }

  public static HandlerList getHandlerList()
  {
    return HANDLERS;
  }

  public Player getPlayer()
  {
    return player;
  }

  public int getScore()
  {
    return score;
  }
}
