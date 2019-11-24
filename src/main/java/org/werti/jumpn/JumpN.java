package org.werti.jumpn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.werti.jumpn.BlockHandling.VectorHelper;
import org.werti.jumpn.BlockHandling.Platform;

public class JumpN
{
  private static Material blockType = Material.GLASS;

  private JumpNPlayer jumpNPlayer;

  public JumpN(JumpNPlayer jumpNPlayer)
  {
    this.jumpNPlayer = jumpNPlayer;
  }

  public boolean SetNextPlatform()
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

    newBlock.setType(blockType);

    return true;
  }
}
