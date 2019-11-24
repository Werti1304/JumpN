package org.werti.jumpn.BlockHandling;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.werti.jumpn.Globals;
import org.werti.jumpn.JumpNPlayer;
import sun.jvm.hotspot.opto.Block;

public class BlockCoords extends Vector
{
  public BlockCoords(int x, int y, int z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public BlockCoords(JumpNPlayer player, Platform platform)
  {
    Location playerLocation = player.getLocation().clone();

    BlockCoords relativeCoords = AdjustCoordinatesToDirection(platform);

    this.setX(playerLocation.getX());
    this.setY(playerLocation.getY());
    this.setZ(playerLocation.getZ());

    this.add(relativeCoords);
  }

  private BlockCoords AdjustCoordinatesToDirection(Platform platform)
  {
    int x = 0;
    // Height = 0 should mean under the player, not inside him, so we have to correct the y-coordinate by -1
    int y = platform.platformConfiguration.getHeightOffset() - 1;
    int z = 0;

    int forwardOffset = platform.platformConfiguration.forwardOffset;
    int sidewaysOffset = platform.platformConfiguration.sidewaysOffset;

    switch (platform.direction)
    {
      case North:
        z = -forwardOffset;
        switch (platform.sidewaysDirection)
        {
          case Left:
            x = -sidewaysOffset;
            break;
          case Right:
            x = sidewaysOffset;
            break;
        }
        break;
      case East:
        x = forwardOffset;
        switch (platform.sidewaysDirection)
        {
          case Left:
            z = -sidewaysOffset;
            break;
          case Right:
            z = sidewaysOffset;
            break;
        }
        break;
      case South:
        z = forwardOffset;
        switch (platform.sidewaysDirection)
        {
          case Left:
            x = sidewaysOffset;
            break;
          case Right:
            x = -sidewaysOffset;
            break;
        }
        break;
      case West:
        x = -forwardOffset;
        switch (platform.sidewaysDirection)
        {
          case Left:
            z = sidewaysOffset;
            break;
          case Right:
            z = -sidewaysOffset;
            break;
        }
        break;
    }

    return new BlockCoords(x, y, z);
  }
}