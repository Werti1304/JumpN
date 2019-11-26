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
    JumpNPlayer jumpNPlayer = JumpNPlayer.GetJumpNPlayer(playerMoveEvent.getPlayer());

    if (jumpNPlayer == null)
    {
      return;
    }

    JumpN jumpn = jumpNPlayer.getJumpN();

    if(jumpn.getPlatformLock().isLocked())
    {
      return;
    }

    Location playerLocation = jumpNPlayer.getLocation();
    Block playerStandBlock = playerLocation.clone().subtract(0, 1, 0).getBlock();

    Location newPlatformLocation = jumpn.getNewPlatformLocation();
    Block newPlatformBlock = newPlatformLocation.getBlock();
    Location oldPlatformLocation = jumpn.getOldPlatformLocation();

    if (playerStandBlock.equals(newPlatformBlock))
    {
      jumpn.nextPlatform();
      return;
    }

    if(oldPlatformLocation == null)
    {
      // This terminates only if the player is 2 blocks lower than the platform hes supposed to be jumping on
      if(playerLocation.getBlockY() < newPlatformLocation.getBlockY() - 1)
      {
        Globals.debug("Terminating, because player is significantly lower then new platform");
        jumpNPlayer.lost();
      }
    }
    else
    {
      // This terminates if the player is lower than the platform he was standing on
      if (playerLocation.getBlockY() < oldPlatformLocation.getBlockY())
      {
        Globals.debug("Terminating, because player is lower then old platform");
        jumpNPlayer.lost();
      }
    }
  }
}