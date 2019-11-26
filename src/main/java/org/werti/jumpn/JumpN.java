package org.werti.jumpn;

import jdk.internal.jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.werti.jumpn.BlockHandling.BlockHelper;
import org.werti.jumpn.BlockHandling.Platform;
import org.werti.jumpn.BlockHandling.VectorHelper;
import org.werti.jumpn.Events.Jumpn.LoseEvent;
import org.werti.jumpn.Events.Jumpn.PlatformReachedEvent;
import org.werti.jumpn.Events.Jumpn.StartEvent;
import org.werti.jumpn.Events.Jumpn.WinEvent;

import java.util.concurrent.locks.ReentrantLock;

public class JumpN
{
  private ReentrantLock platformLock;

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
  }

  public void start()
  {
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
    PlatformReachedEvent reachedPlatformEvent = new PlatformReachedEvent(jumpNPlayer.getPlayer(), score);
    Globals.bukkitServer.getPluginManager().callEvent(reachedPlatformEvent);

    Player player = jumpNPlayer.getPlayer();

    if(score == Globals.winScore)
    {
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.f);
      jumpNPlayer.won();
      return;
    }
    else
    {
      player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.f);
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
    // Firstly, try to generate a right platform by trying 50 random platform configurations
    for (int i = 0; i < Globals.maxRandomPlatformTries; i++)
    {
      if(setNewPlatform())
      {
        platformLock.unlock();
        return;
      }
    }

    Globals.logger.warning(String.format("Generated 10 random platforms for %s, but none of them fit.", jumpNPlayer.getName()));

    for(Platform.PlatformConfiguration platformConfiguration : Platform.PlatformConfiguration.values())
    {
      for(Platform.Direction direction : Platform.Direction.values())
      {
        Platform platform;

        platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Left);
        if(setNewPlatform(platform))
        {
          platformLock.unlock();
          return;
        }

        platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Right);
        if(setNewPlatform(platform))
        {
          platformLock.unlock();
          return;
        }
      }
    }

    Globals.logger.severe("No space for a platform found for " + jumpNPlayer.getName());

    jumpNPlayer.lost();
  }

  /**
   * Generates a new random platform.
   * @return whether the platform has been placed.
   */
  private boolean setNewPlatform()
  {
    return setNewPlatform(Platform.GetRandomPlatform());
  }

  private boolean setNewPlatform(Platform platform)
  {
    Vector platformCoords = VectorHelper.AdjustJump(jumpNPlayer, platform);

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

    if(!BlockHelper.isAir(newBlock.getType()))
    {
      return false;
    }

    newPlatformLocation = newBlock.getLocation();
    newPlatformMaterial = newBlock.getType();

    newBlock.setType(Globals.platformMaterial);

    return true;
  }

  public void lost()
  {
    resetBlocks();

    // Calls the Lose-Event
    LoseEvent loseEvent = new LoseEvent(jumpNPlayer.getPlayer(), score);
    Globals.bukkitServer.getPluginManager().callEvent(loseEvent);
  }

  public void won()
  {
    removeOldPlatform();

    newPlatformLocation.getBlock().setType(Globals.winningPlatformMaterial);

    // Calls the Win-Event
    WinEvent winEvent = new WinEvent(jumpNPlayer.getPlayer());
    Globals.bukkitServer.getPluginManager().callEvent(winEvent);
  }

  private void resetBlocks()
  {
    removeOldPlatform();

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