package org.werti.jumpn;

import jdk.internal.jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.werti.jumpn.BlockHandling.Platform;
import org.werti.jumpn.BlockHandling.VectorHelper;

public class JumpN
{
  private static Material blockMaterial = Material.GLASS;

  private JumpNPlayer jumpNPlayer;

  @Nullable
  private Location oldBlockLocation;
  @Nullable
  private Material oldBlockMaterial;

  @Nullable
  private Location newBlockLocation;
  @Nullable
  private  Material newBlockMaterial;

  public JumpN(JumpNPlayer jumpNPlayer)
  {
    this.jumpNPlayer = jumpNPlayer;
  }

  public void nextPlatform()
  {
    removeOldPlatform();

    for(int i = 0; i < 500; i++)
    {
      if(setNewPlatform())
      {
        return;
      }
    }

    Globals.logger.severe("Couldn't set platform after [500] tries! Aborting jumpn..");

    JumpNPlayer.Remove(jumpNPlayer);
  }

  private void removeOldPlatform()
  {
    if(oldBlockLocation != null && oldBlockMaterial != null)
    {
      oldBlockLocation.getBlock().setType(oldBlockMaterial);
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

    Block newBlock = platformCoords.toLocation(jumpNPlayer.getWorld()).getBlock();

    if(newBlock.getType() != Material.AIR)
    {
      return false;
    }

    oldBlockLocation = newBlock.getLocation().clone();
    oldBlockMaterial = newBlockMaterial;

    this.newBlockLocation = newBlock.getLocation();
    newBlockMaterial = newBlock.getType();

    newBlock.setType(blockMaterial);

    return true;
  }

  public Location getOldBlockLocation()
  {
    return oldBlockLocation;
  }

  public Location getNewBlockLocation()
  {
    return newBlockLocation;
  }
}
