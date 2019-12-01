package org.werti.jumpn.Events;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.werti.jumpn.Globals;
import org.werti.jumpn.JumpN;

public class OnMove implements Listener
{
  @EventHandler (priority = EventPriority.LOW)
  public void onPlayerMoveEvent(PlayerMoveEvent playerMoveEvent)
  {
    JumpN jumpN = JumpN.getFrom(playerMoveEvent.getPlayer());

    if(jumpN == null)
    {
      return;
    }

    if(jumpN.getPlatformLock().isLocked())
    {
      return;
    }

    Location playerLocation = jumpN.jumpNPlayer.getLocation();

    Location currentPlatformLocation = jumpN.getCurrentPlatformLocation();

    JumpN.State state = jumpN.getState();

    Location newPlatformLocation = jumpN.getNewPlatformLocation();

    // Checking if player has reached the new platform
    if(newPlatformLocation != null)
    {
      Block playerStandBlock = playerLocation.clone().subtract(0, 1, 0).getBlock();
      Block newPlatformBlock = newPlatformLocation.getBlock();

      if(playerStandBlock.equals(newPlatformBlock) && state == JumpN.State.Running)
      {
        jumpN.getPlatformLock().lock();
        jumpN.nextPlatform();
        jumpN.getPlatformLock().unlock();
        return;
      }
    }

    // This terminates only if the player is 2 blocks lower than the platform hes supposed to be jumping on
    if(playerLocation.getBlockY() < currentPlatformLocation.getBlockY() - 1)
    {
      if(state == JumpN.State.Running)
      {
        Globals.debug(jumpN.jumpNPlayer.getName(), "Terminating, because player is significantly lower then new platform");
        jumpN.setState(JumpN.State.Lose);
      }
      else if(state == JumpN.State.Win)
      {
        spawnFirework(jumpN.getCurrentPlatformLocation());

        jumpN.setState(JumpN.State.Terminate);
      }
    }
  }

  /**
   * To celebrate victory, spawns a last firework at destruction of the winning-block
   */
  private static void spawnFirework(Location location)
  {
    Firework firework = (Firework)location.getWorld().spawnEntity(location, EntityType.FIREWORK);
    FireworkMeta fireworkMeta = firework.getFireworkMeta();

    fireworkMeta.setPower(0);
    fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).build());

    firework.setFireworkMeta(fireworkMeta);
    firework.detonate();
  }
}