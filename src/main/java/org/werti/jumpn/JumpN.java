package org.werti.jumpn;

import jdk.internal.jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.werti.jumpn.BlockHandling.Platform;
import org.werti.jumpn.BlockHandling.VectorHelper;

import java.util.concurrent.locks.ReentrantLock;

public class JumpN
{
  private ReentrantLock platformLock = new ReentrantLock();

  private static Material blockMaterial = Material.SNOW_BLOCK;

  private JumpNPlayer jumpNPlayer;

  @Nullable
  private Location oldPlatformLocation;
  @Nullable
  private Material oldPlatformMaterial;

  @Nullable
  private Location newPlatformLocation;
  @Nullable
  private  Material newPlatformMaterial;

  public JumpN(JumpNPlayer jumpNPlayer)
  {
    this.jumpNPlayer = jumpNPlayer;

    nextPlatform();
  }

  public void nextPlatform()
  {
    platformLock.lock();

    Globals.debug("Setting next platform..");

    removeOldPlatform();

    for(int i = 0; i < 500; i++)
    {
      if(setNewPlatform())
      {
        platformLock.unlock();
        return;
      }
    }

    Globals.logger.severe("Couldn't set platform after [500] tries! Aborting jumpn..");

    jumpNPlayer.terminate();
  }

  private void removeOldPlatform()
  {
    if(oldPlatformLocation != null && oldPlatformMaterial != null)
    {
      oldPlatformLocation.getBlock().setType(oldPlatformMaterial);
    }
  }

  private boolean setNewPlatform()
  {
    Platform nextPlatform = Platform.GetRandomPlatform();

    Vector platformCoords = VectorHelper.AdjustJump(jumpNPlayer, nextPlatform);

    Vector playerCoords = jumpNPlayer.toVector();

    Vector checkCoords = platformCoords.clone();
    checkCoords.setY(checkCoords.getBlockY() + 3);

    Globals.debug(String.format("Setting new platform at absolute: x:%d y:%d z:%d", platformCoords.getBlockX(), platformCoords.getBlockY(), platformCoords.getBlockZ()));

    if(!VectorHelper.isAreaAirOnly(playerCoords, checkCoords, jumpNPlayer.getWorld()))
    {
      return false;
    }

    if(newPlatformLocation != null)
    {
      oldPlatformLocation = newPlatformLocation.clone();
      oldPlatformMaterial = newPlatformMaterial;
    }

    Block newBlock = platformCoords.toLocation(jumpNPlayer.getWorld()).getBlock();

    if(newBlock.getType() != Material.AIR)
    {
      return false;
    }

    newPlatformLocation = newBlock.getLocation();
    newPlatformMaterial = newBlock.getType();

    newBlock.setType(blockMaterial);

    return true;
  }

  public void destroyBlocks()
  {
    if(oldPlatformLocation != null)
    {
      oldPlatformLocation.getBlock().setType(oldPlatformMaterial);
    }

    if(newPlatformLocation != null)
    {
      newPlatformLocation.getBlock().setType(newPlatformMaterial);
    }
  }

  public Location getOldPlatformLocation()
  {
    return oldPlatformLocation;
  }

  public Location getNewPlatformLocation()
  {
    return newPlatformLocation;
  }

  public ReentrantLock getPlatformLock()
  {
    return platformLock;
  }
}
