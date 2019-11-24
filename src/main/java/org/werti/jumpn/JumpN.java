package org.werti.jumpn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.werti.jumpn.BlockHandling.BlockCoords;
import org.werti.jumpn.BlockHandling.Platform;

import java.util.*;

public class JumpN
{
  static Material blockType = Material.GLASS;

  private JumpNPlayer jumpNPlayer;

  public JumpN(JumpNPlayer jumpNPlayer)
  {
    this.jumpNPlayer = jumpNPlayer;
  }

  public void SetNextPlatform()
  {
    Platform nextPlatform = Platform.GetRandomPlatform();

    BlockCoords platformCoords = new BlockCoords(jumpNPlayer, nextPlatform);

    Globals.debug(String.format("Setting new platform at absolute: x:%d y:%d z:%d", platformCoords.getBlockX(), platformCoords.getBlockY(), platformCoords.getBlockZ()));

    platformCoords.toLocation(jumpNPlayer.getWorld()).getBlock().setType(blockType);

    // TODO: Check if platform can be placed (if blocks leading to the platform are free, etc.)
  }
}
