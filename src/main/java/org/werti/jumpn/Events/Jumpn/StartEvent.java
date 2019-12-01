package org.werti.jumpn.Events.Jumpn;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that gets called when someone starts the jump'n'run.
 * (Only gets called if the starting-platforms were generated successfully)
 */
public class StartEvent extends Event
{
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;

  public StartEvent(Player player)
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
