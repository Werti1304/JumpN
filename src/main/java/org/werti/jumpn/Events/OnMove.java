package org.werti.jumpn.Events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.werti.jumpn.Globals;
import org.werti.jumpn.JumpN;
import org.werti.jumpn.JumpNPlayer;

public class OnMove implements Listener
{
  @EventHandler (priority = EventPriority.LOW)
  public void onPlayerMoveEvent(PlayerMoveEvent playerMoveEvent)
  {
    JumpN jumpN = JumpN.getFrom(playerMoveEvent.getPlayer());

    if (jumpN == null)
    {
      return;
    }

    if(jumpN.getPlatformLock().isLocked())
    {
      return;
    }

    Location playerLocation = jumpN.jumpNPlayer.getLocation();
    Block playerStandBlock = playerLocation.clone().subtract(0, 1, 0).getBlock();

    Location newPlatformLocation = jumpN.getNewPlatformLocation();
    Block newPlatformBlock = newPlatformLocation.getBlock();
    Location oldPlatformLocation = jumpN.getOldPlatformLocation();

    JumpN.State state = jumpN.getState();

    if (playerStandBlock.equals(newPlatformBlock) && state == JumpN.State.Running)
    {
      jumpN.getPlatformLock().lock();
      jumpN.nextPlatform();
      jumpN.getPlatformLock().unlock();
      return;
    }

    if(oldPlatformLocation == null)
    {
      // This terminates only if the player is 2 blocks lower than the platform hes supposed to be jumping on
      if(playerLocation.getBlockY() < newPlatformLocation.getBlockY() - 1)
      {
        if(state == JumpN.State.Running)
        {
          Globals.debug(jumpN.jumpNPlayer.getName(), "Terminating, because player is significantly lower then new platform");
          jumpN.setState(JumpN.State.Lose);
        }
        else if(state == JumpN.State.Win)
        {
          jumpN.setState(JumpN.State.Terminate);
        }
      }
    }
    else
    {
      // This terminates if the player is lower than the platform he was standing on
      if (playerLocation.getBlockY() < oldPlatformLocation.getBlockY())
      {
        Globals.debug(jumpN.jumpNPlayer.getName(), "Terminating, because player is lower then old platform");
        jumpN.setState(JumpN.State.Lose);
      }
    }
  }
}