package org.werti.jumpn.Events.Jumpn;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that gets called once a player has successfully completed the jump'n'run.
 * TODO: Implement Scoring-System and WinEvent-Call
 */
public class WinEvent extends Event
{
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;

  public WinEvent(Player player)
  {
    this.player = player;
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
}
