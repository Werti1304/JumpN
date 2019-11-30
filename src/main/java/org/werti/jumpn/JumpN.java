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
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.material.Redstone;
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
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class JumpN
{
  private static ArrayList<JumpN> jumpNList = new ArrayList<>();

  private ReentrantLock platformLock;

  public JumpNPlayer jumpNPlayer;

  private int score = 0;

  private TextComponent textComponent = new TextComponent();

  private BukkitTask actionBarTask;
  private BukkitTask particleTask;

  private Random random = new Random();

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

    Color color = Color.fromBGR(random.nextInt(255), random.nextInt(255),random.nextInt(255));

    dustOptions = new Particle.DustOptions(color, 5);

    platformLock = new ReentrantLock();
  }

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
        platformLock.lock();
        if(trySettingNewPlatform())
        {
          // Calls the Start-Event
          StartEvent startEvent = new StartEvent(jumpNPlayer.getPlayer());
          Globals.bukkitServer.getPluginManager().callEvent(startEvent);
        }
        else
        {
          updateActionBar(ChatColor.RED, "Not enough space!");

          Globals.logger.warning(String.format("No space for a platform found for \'%s\'", jumpNPlayer.getName()));

          jumpNPlayer.sendMessage(JumpNPlayer.MessageType.Negative, "There is no space for a jump'n'run here!");

          setState(State.Terminate);
        }
        platformLock.unlock();

        enableActionBar();
        enableParticles();
        break;
      case Win:
        resetOldPlatform();

        setBlock(newPlatformLocation, Globals.winningPlatformMaterial);

        dustOptions = new Particle.DustOptions(Color.YELLOW, 5);

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
    if(oldPlatformLocation != null && oldPlatformMaterial != null)
    {
      setBlock(oldPlatformLocation, oldPlatformMaterial);
    }

    oldPlatformLocation = null;
    oldPlatformMaterial = null;
  }

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

    for(Platform.PlatformConfiguration platformConfiguration : Platform.PlatformConfiguration.values())
    {
      for(Platform.Direction direction : Platform.Direction.values())
      {
        Platform platform;

        platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Left);
        if(setNewPlatform(platform))
        {
          return true;
        }

        platform = new Platform(platformConfiguration, direction, Platform.SidewaysDirection.Right);
        if(setNewPlatform(platform))
        {
          return true;
        }
      }
    }
    return false;
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

    // Only set the block for one player
    jumpNPlayer.getPlayer().sendBlockChange(platformLocation, platformMaterial.createBlockData());

    // Set actual block:
    //platformLocation.getBlock().setType(platformMaterial);
  }

  void resetBlocks()
  {
    resetOldPlatform();

    if(newPlatformLocation != null)
    {
      setBlock(newPlatformLocation, newPlatformMaterial);
    }
  }

  private  void enableParticles()
  {
    particleTask = Globals.bukkitServer.getScheduler().runTaskTimer(Globals.plugin, this::sendParticles, 0, Globals.tick / 2);
  }

  private Particle.DustOptions dustOptions;
  private static final double particleSpreadVertically = 0.1;
  private static final double particleSpreadHorizontally = 0.5;
  private static int count = 10;
  private static double speed = 0;

  private void sendParticles()
  {
    if(oldPlatformLocation != null)
    {
      jumpNPlayer.getWorld().spawnParticle(Particle.REDSTONE, oldPlatformLocation, count, particleSpreadHorizontally, particleSpreadVertically, particleSpreadHorizontally, speed, dustOptions);
    }

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
    jumpNPlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
  }


  public void updateActionBar(ChatColor chatColor, String message)
  {
    textComponent.setColor(chatColor);
    textComponent.setText(message);

    sendActionBarMessage();
  }

  public static ArrayList<JumpN> getJumpNList()
  {
    return jumpNList;
  }

  public TextComponent getTextComponent()
  {
    return textComponent;
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