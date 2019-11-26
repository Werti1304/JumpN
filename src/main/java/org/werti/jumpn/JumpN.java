package org.werti.jumpn;

import jdk.internal.jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.werti.jumpn.BlockHandling.Platform;
import org.werti.jumpn.BlockHandling.VectorHelper;
import org.werti.jumpn.Events.Jumpn.LoseEvent;
import org.werti.jumpn.Events.Jumpn.ReachedPlatformEvent;
import org.werti.jumpn.Events.Jumpn.StartEvent;
import org.werti.jumpn.Events.Jumpn.WinEvent;

import java.util.concurrent.locks.ReentrantLock;

public class JumpN
{
  private ReentrantLock platformLock;

  private static Material blockMaterial = Material.SNOW_BLOCK;

  private JumpNPlayer jumpNPlayer;

  private int score = 0;

  @Nullable
  private Location oldPlatformLocation;
  @Nullable
  private Material oldPlatformMaterial;

  @Nullable
  private Location newPlatformLocation;
  @Nullable
  private Material newPlatformMaterial;

  public JumpN(JumpNPlayer jumpNPlayer)
  {
    this.jumpNPlayer = jumpNPlayer;

    platformLock = new ReentrantLock();

    // Calls the Start-Event
    StartEvent startEvent = new StartEvent(jumpNPlayer.getPlayer());
    Globals.bukkitServer.getPluginManager().callEvent(startEvent);

    platformLock.lock();
    trySettingNewPlatform();
  }

  /**
   * Deletes old platform and generates new platform (hopefully enough) thread-safe
   */
  public void nextPlatform()
  {
    platformLock.lock();

    Globals.debug("Setting next platform..");

    // Next platform was reached, so we increment the score
    score++;

    // Calls the ReachedPlatform-Event
    ReachedPlatformEvent reachedPlatformEvent = new ReachedPlatformEvent(jumpNPlayer.getPlayer(), score);
    Globals.bukkitServer.getPluginManager().callEvent(reachedPlatformEvent);

    if(score == Globals.winScore)
    {
      // Calls the Win-Event
      WinEvent winEvent = new WinEvent(jumpNPlayer.getPlayer());
      Globals.bukkitServer.getPluginManager().callEvent(winEvent);
    }

    removeOldPlatform();

    trySettingNewPlatform();
  }

  /**
   * Removes old platform
   */
  private void removeOldPlatform()
  {
    if(oldPlatformLocation != null && oldPlatformMaterial != null)
    {
      oldPlatformLocation.getBlock().setType(oldPlatformMaterial);
    }
  }

  private void trySettingNewPlatform()
  {
    for (int i = 0; i < 500; i++)
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

  public void end()
  {
    resetBlocks();

    // Calls the Lose-Event
    LoseEvent loseEvent = new LoseEvent(jumpNPlayer.getPlayer(), score);
    Globals.bukkitServer.getPluginManager().callEvent(loseEvent);
  }

  private void resetBlocks()
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