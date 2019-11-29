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

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class JumpN
{
  private static ArrayList<JumpN> jumpNList = new ArrayList<>();

  private ReentrantLock platformLock;

  public JumpNPlayer jumpNPlayer;

  private int score = 0;

  public State getState()
  {
    return state;
  }

  public enum State
  {
    Idle,
    Running,
    Win,
    Lose,
    Terminate
  };

  private State state = State.Idle;

  @Nullable
  private Location oldPlatformLocation;
  @Nullable
  private Material oldPlatformMaterial;

  @Nullable
  private Location newPlatformLocation;
  @Nullable
  private Material newPlatformMaterial;

  public JumpN(Player player)
  {
    this.jumpNPlayer = new JumpNPlayer(player);

    jumpNList.add(this);

    platformLock = new ReentrantLock();
  }

  public void tearDown()
  {
    jumpNList.remove(this);

    if(state == State.Running)
    {
      setState(State.Terminate);
    }
  }

  @Nullable
  public static JumpN getFrom(Player player)
  {
    final Optional<JumpN> result = jumpNList.stream()
        .filter(item -> item.jumpNPlayer.getPlayer().equals(player))
        .findAny();

    // For readability and intellijs conscience
    return result.orElse(null);
  }

  public void setState(State state)
  {
    debug(String.format("Setting JumpN-State from %s to %s", this.state.name(), state.name()));

    this.state = state;

    switch (state)
    {
      case Idle:
        break;
      case Running:
        // Calls the Start-Event
        StartEvent startEvent = new StartEvent(jumpNPlayer.getPlayer());
        Globals.bukkitServer.getPluginManager().callEvent(startEvent);

        platformLock.lock();
        trySettingNewPlatform();
        platformLock.unlock();
        break;
      case Win:
        resetOldPlatform();

        setBlock(newPlatformLocation, Globals.winningPlatformMaterial);

        // Calls the Win-Event
        WinEvent winEvent = new WinEvent(jumpNPlayer.getPlayer());
        Globals.bukkitServer.getPluginManager().callEvent(winEvent);
        break;
      case Lose:
        resetBlocks();

        // Calls the Lose-Event
        LoseEvent loseEvent = new LoseEvent(jumpNPlayer.getPlayer(), score);
        Globals.bukkitServer.getPluginManager().callEvent(loseEvent);

        tearDown();
        break;
      case Terminate:
        resetBlocks();

        tearDown();
        break;
    }
  }

  /**
   * Deletes old platform and generates new platform
   */
  public void nextPlatform()
  {
    // Next platform was reached, so we increment the score
    score++;

    // Calls the ReachedPlatform-Event
    PlatformReachedEvent reachedPlatformEvent = new PlatformReachedEvent(jumpNPlayer.getPlayer(), score);
    Globals.bukkitServer.getPluginManager().callEvent(reachedPlatformEvent);

    Player player = jumpNPlayer.getPlayer();

    if(score == Globals.winScore)
    {
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.f);
      setState(State.Win);
      return;
    }
    else
    {
      player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.f);
    }

    resetOldPlatform();

    trySettingNewPlatform();
  }

  /**
   * Removes old platform
   */
  private void resetOldPlatform()
  {
    if(oldPlatformLocation != null && oldPlatformMaterial != null)
    {
      setBlock(oldPlatformLocation, oldPlatformMaterial);
    }

    oldPlatformLocation = null;
    oldPlatformMaterial = null;
  }

  private void trySettingNewPlatform()
  {
    // Firstly, try to generate a right platform by trying 50 random platform configurations
    for (int i = 0; i < Globals.maxRandomPlatformTries; i++)
    {
      if(setNewPlatform())
      {
        return;
      }
    }

    debug(String.format("Generated %d random platforms for %s, but none of them fit.", Globals.maxRandomPlatformTries, jumpNPlayer.getName()));

    for(Platform.PlatformConfiguration platformConfiguration : Platform.PlatformConfiguration.values())
    {
      for(Platform.Direction direction : Platform.Direction.values())
      {
        Platform platform;

        platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Left);
        if(setNewPlatform(platform))
        {
          return;
        }

        platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Right);
        if(setNewPlatform(platform))
        {
          return;
        }
      }
    }

    Globals.logger.warning(String.format("No space for a platform found for \'%s\'", jumpNPlayer.getName()));

    setState(State.Terminate);

    jumpNPlayer.sendMessage(JumpNPlayer.MessageType.Negative, "There is no space for a jump'n'run here!");
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

    if(!VectorHelper.isAreaAirOnly(playerCoords, checkCoords, jumpNPlayer.getWorld()))
    {
      return false;
    }

    Location newPlatformLocation = platformCoords.toLocation(jumpNPlayer.getWorld());
    Block newBlock = newPlatformLocation.getBlock();

    if(!BlockHelper.isAir(newBlock.getType()) || (oldPlatformLocation != null && BlockHelper.isSameLocation(oldPlatformLocation, newPlatformLocation)))
    {
      return false;
    }

    if(this.newPlatformLocation != null)
    {
      oldPlatformLocation = this.newPlatformLocation.clone();
      oldPlatformMaterial = newPlatformMaterial;
    }

    this.newPlatformLocation = newPlatformLocation;
    newPlatformMaterial = newBlock.getType();

    setBlock(newPlatformLocation, Globals.platformMaterial);

    return true;
  }

  private void setBlock(Location platformLocation, Material platformMaterial)
  {
    debug(String.format("Setting block at %d;%d;%d from %s to %s",
                        platformLocation.getBlockX(),
                        platformLocation.getBlockY(),
                        platformLocation.getBlockZ(),
                        platformLocation.getBlock().getType().name(),
                        platformMaterial.name()));

    platformLocation.getBlock().setType(platformMaterial);
  }

  void resetBlocks()
  {
    resetOldPlatform();

    if(newPlatformLocation != null)
    {
      setBlock(newPlatformLocation, newPlatformMaterial);
    }
  }

  public static ArrayList<JumpN> getJumpNList()
  {
    return jumpNList;
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

  private void debug(String message)
  {
    Globals.debug(jumpNPlayer.getName(), message);
  }
}