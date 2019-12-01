package org.werti.jumpn;

import jdk.internal.jline.internal.Nullable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class JumpN
{
  /**
   * List of all currently running JumpNs
   */
  private static ArrayList<JumpN> jumpNList = new ArrayList<>();

  /**
   * "Mutex" Lock, used for (more or less) thread-safeness between OnMove and Platform-Generation
   */
  private ReentrantLock platformLock;

  public JumpNPlayer jumpNPlayer;

  /**
   * Current score of player
   */
  private int score = 0;

  private TextComponent actionBarTextComponent = new TextComponent();

  /**
   * BukkitTask responsible for the actionBar
   */
  private BukkitTask actionBarTask;

  /**
   * BukkitTask responsible for particle-generation
   */
  private BukkitTask particleTask;

  public State getState()
  {
    return state;
  }

  /**
   * Possible States of JumpN
   */
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
  private Location currentPlatformLocation;
  @Nullable
  private Material currentPlatformMaterial;

  @Nullable
  private Location newPlatformLocation;
  @Nullable
  private Material newPlatformMaterial;

  /**
   * @param player Generates new JumpN instance for a player (jump'n'run is idle at this point)
   */
  public JumpN(Player player)
  {
    this.jumpNPlayer = new JumpNPlayer(player);

    jumpNList.add(this);

    Color color = Color.fromBGR(ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255));

    dustOptions = new Particle.DustOptions(color, 5);

    platformLock = new ReentrantLock();
  }

  /**
   * Destructor of JumpN Class
   */
  public void tearDown()
  {
    jumpNList.remove(this);

    if(state == State.Running)
    {
      setState(State.Terminate);
    }

    // As long as the state is idle, there is no actionBarTask to cancel yet.
    Globals.bukkitServer.getScheduler().runTaskLater(Globals.plugin, () ->
    {
      if(actionBarTask != null)
      {
        actionBarTask.cancel();
      }
    }, Globals.tick * 3);

    if(particleTask != null)
    {
      particleTask.cancel();
    }
  }

  /**
   * Returns the JumpN of a player (if existent)
   * Wanted to try something new instead of a ol' boring for-loop
   * @param player Player who started the JumpN
   * @return JumpN of the player
   */
  @Nullable
  public static JumpN getFrom(Player player)
  {
    final Optional<JumpN> result = jumpNList.stream()
        .filter(item -> item.jumpNPlayer.getPlayer().equals(player))
        .findAny();

    // For readability and intellijs conscience
    return result.orElse(null);
  }

  /**
   * Set the state of the jump'n'run (NOT only a setter!)
   * E.g. if you want the win-sequence to be activated, just call this function with State.Win
   * @param state New State
   */
  public void setState(State state)
  {
    debug(String.format("Setting JumpN-State from %s to %s", this.state.name(), state.name()));

    State previousState = this.state;

    this.state = state;

    switch (state)
    {
      case Idle:
        break;
      case Running:
        platformLock.lock();

        if(trySettingStartingPlatform() && trySettingNewPlatform())
        {
          teleportPlayerToStartingPlatform();

          // Calls the Start-Event
          StartEvent startEvent = new StartEvent(jumpNPlayer.getPlayer());
          Globals.bukkitServer.getPluginManager().callEvent(startEvent);

          enableParticles();
        }
        else
        {
          updateActionBar(ChatColor.RED, "Not enough space!");

          Globals.logger.warning(String.format("No space for a platform found for \'%s\'", jumpNPlayer.getName()));

          jumpNPlayer.sendMessage(JumpNPlayer.MessageType.Negative, "There is no space for a jump'n'run here!");

          setState(State.Terminate);
        }
        enableActionBar();

        platformLock.unlock();
        break;
      case Win:
        resetOldPlatform();

        setBlock(newPlatformLocation, Globals.winningPlatformMaterial);

        dustOptions = new Particle.DustOptions(Color.YELLOW, 5);

        // Make our winning platform the new platform
        currentPlatformLocation = newPlatformLocation.clone();
        currentPlatformMaterial = newPlatformMaterial;

        // There is no new platform anymore
        newPlatformLocation = null;
        newPlatformMaterial = null;

        // Calls the Win-Event
        WinEvent winEvent = new WinEvent(jumpNPlayer.getPlayer());
        Globals.bukkitServer.getPluginManager().callEvent(winEvent);
        break;
      case Lose:
        resetBlocks();
        if(previousState == State.Running)
        {
          // Calls the Lose-Event
          LoseEvent loseEvent = new LoseEvent(jumpNPlayer.getPlayer(), score);
          Globals.bukkitServer.getPluginManager().callEvent(loseEvent);
        }
        tearDown();
        break;
      case Terminate:
        // "Silent" completion of the jump'n'run
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

    Player player = jumpNPlayer.getPlayer();

    if(score == Globals.winScore)
    {
      setState(State.Win);
      return;
    }
    else
    {
      // Calls the ReachedPlatform-Event
      PlatformReachedEvent reachedPlatformEvent = new PlatformReachedEvent(jumpNPlayer.getPlayer(), score);
      Globals.bukkitServer.getPluginManager().callEvent(reachedPlatformEvent);
    }

    resetOldPlatform();

    if(!trySettingNewPlatform())
    {
      updateActionBar(ChatColor.RED, "Not enough space!");

      Globals.logger.warning(String.format("No space for a platform found for \'%s\'", jumpNPlayer.getName()));

      setState(State.Terminate);
    }
  }

  /**
   * Removes old platform
   */
  private void resetOldPlatform()
  {
    if(currentPlatformLocation != null && currentPlatformMaterial != null)
    {
      setBlock(currentPlatformLocation, currentPlatformMaterial);
    }
  }

  /**
   * Tries to set the new platform (if it can, it does so, if not, it returns false)
   * @return Whether the platform was set
   */
  private boolean trySettingNewPlatform()
  {
    // Firstly, try to generate a right platform by trying 50 random platform configurations
    for (int i = 0; i < Globals.maxRandomPlatformTries; i++)
    {
      if(setNewPlatform())
      {
        return true;
      }
    }

    debug(String.format("Generated %d random platforms for %s, but none of them fit.", Globals.maxRandomPlatformTries, jumpNPlayer.getName()));

    // Secondly, iterates through every single platform-possibility to find a platform that's possible (or at least probable) for the player to get to
    for(Platform.PlatformConfiguration platformConfiguration : Platform.PlatformConfiguration.values())
    {
      for(Platform.Direction direction : Platform.Direction.values())
      {
        Platform platform;

        if(platformConfiguration.getSidewaysOffset() == 0)
        {
          // Tries to set the platform without SidewaysDirection (none needed)
          platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.None);
          if(setNewPlatform(platform))
          {
            return true;
          }
        }
        else
        {
          // Tries to set the platform with Right SidewaysDirection
          platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Left);
          if(setNewPlatform(platform))
          {
            return true;
          }

          // Tries to set the platform with Left SidewaysDirection
          platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Right);
          if(setNewPlatform(platform))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Tries to set the starting platform x blocks over the player while giving him enough space
   * @return Whether the platform was placed
   */
  private boolean trySettingStartingPlatform()
  {
    int height = Platform.GetRandomStartingHeight();
    Location playerLocation = jumpNPlayer.getLocation();

    // Start checking from head-height of player...
    Vector startingVector = playerLocation.clone().add(0, 1, 0).toVector();
    // ...all the way up to 3 over the block so the player always has to have enough space to jump freely
    Vector maxHeightPlayerVector = playerLocation.clone().add(0, height + 3, 0).toVector();

    if(VectorHelper.isAreaAirOnly( startingVector, maxHeightPlayerVector, jumpNPlayer.getWorld()))
    {
      // Set current platform to the location of the player + the height where the platform should be
      currentPlatformLocation = playerLocation.clone().add(0, height, 0);
      currentPlatformMaterial = currentPlatformLocation.getBlock().getType();

      setBlock(currentPlatformLocation, Globals.platformMaterial);
      return true;
    }
    return false;
  }

  /**
   * Generates a new random platform and sets it.
   * @return whether the platform has been placed.
   */
  private boolean setNewPlatform()
  {
    return setNewPlatform(Platform.GetRandomPlatform());
  }

  /**
   * Sets new platform
   * @param platform platform to be set
   * @return Whether the platform has been set (enough space)
   */
  private boolean setNewPlatform(Platform platform)
  {
    if(this.newPlatformLocation != null)
    {
      currentPlatformLocation = this.newPlatformLocation.clone();
      currentPlatformMaterial = newPlatformMaterial;
    }

    Vector platformCoords = VectorHelper.AdjustJump(currentPlatformLocation, platform);

    Vector checkCoords = platformCoords.clone();
    checkCoords.setY(checkCoords.getBlockY() + 3);

    if(!VectorHelper.isAreaAirOnly(currentPlatformLocation.toVector(), checkCoords, jumpNPlayer.getWorld()))
    {
      return false;
    }

    Location newPlatformLocation = platformCoords.toLocation(jumpNPlayer.getWorld());
    Block newBlock = newPlatformLocation.getBlock();

    if(!BlockHelper.isAir(newBlock.getType()) || (currentPlatformLocation != null && BlockHelper.isSameLocation(currentPlatformLocation, newPlatformLocation)))
    {
      return false;
    }

    this.newPlatformLocation = newPlatformLocation;
    newPlatformMaterial = newBlock.getType();

    setBlock(newPlatformLocation, Globals.platformMaterial);

    return true;
  }

  /**
   * Sets block to a new material (only! for jumpnplayer, everyone else won't see it)
   * @param platformLocation location of block to change
   * @param platformMaterial new material for the block
   */
  private void setBlock(Location platformLocation, Material platformMaterial)
  {
    debug(String.format("Setting block at %d;%d;%d from %s to %s",
                        platformLocation.getBlockX(),
                        platformLocation.getBlockY(),
                        platformLocation.getBlockZ(),
                        platformLocation.getBlock().getType().name(),
                        platformMaterial.name()));

    // Only set the block for one player
    jumpNPlayer.getPlayer().sendBlockChange(platformLocation, platformMaterial.createBlockData());

    // Set actual block:
    //platformLocation.getBlock().setType(platformMaterial);
  }

  /**
   * Changes blocks back to their former self (:p)
   */
  void resetBlocks()
  {
    resetOldPlatform();

    if(newPlatformLocation != null)
    {
      setBlock(newPlatformLocation, newPlatformMaterial);
    }
  }


  /**
   * Activates timer for generating particles
   */
  private void enableParticles()
  {
    particleTask = Globals.bukkitServer.getScheduler().runTaskTimer(Globals.plugin, this::sendParticles, 0, Globals.tick / 2);
  }

  private Particle.DustOptions dustOptions;
  private static final double particleSpreadVertically = 0.1;
  private static final double particleSpreadHorizontally = 0.5;
  private static int count = 10;
  private static double speed = 0;

  /**
   * Generates particles (which everyone can see) at the current jump'n'run blocks
   */
  private void sendParticles()
  {
    jumpNPlayer.getWorld().spawnParticle(Particle.REDSTONE, currentPlatformLocation, count, particleSpreadHorizontally, particleSpreadVertically, particleSpreadHorizontally, speed, dustOptions);

    if(newPlatformLocation != null)
    {
      jumpNPlayer.getWorld().spawnParticle(Particle.REDSTONE, newPlatformLocation, count, particleSpreadHorizontally, particleSpreadVertically, particleSpreadHorizontally, speed, dustOptions);
    }
  }

  /**
   * Starts a timer that repeats the current message (textcomponent), so that the actionbar always stays the same
   */
  private void enableActionBar()
  {
    actionBarTask = Globals.bukkitServer.getScheduler().runTaskTimer(Globals.plugin, this::sendActionBarMessage, 0, Globals.tick * 2);
  }

  private void sendActionBarMessage()
  {
    jumpNPlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarTextComponent);
  }


  /**
   * Instantaneously updates action bar to new color/message
   * @param chatColor new Color
   * @param message new Message
   */
  public void updateActionBar(ChatColor chatColor, String message)
  {
    actionBarTextComponent.setColor(chatColor);
    actionBarTextComponent.setText(message);

    sendActionBarMessage();
  }

  private void teleportPlayerToStartingPlatform()
  {
    jumpNPlayer.getPlayer().teleport(currentPlatformLocation.clone().add(0, 2, 0));
  }

  public static ArrayList<JumpN> getJumpNList()
  {
    return jumpNList;
  }

  public TextComponent getActionBarTextComponent()
  {
    return actionBarTextComponent;
  }

  public Location getCurrentPlatformLocation()
  {
    return currentPlatformLocation;
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