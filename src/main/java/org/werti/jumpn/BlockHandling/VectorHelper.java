package org.werti.jumpn.BlockHandling;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class VectorHelper
{
  private static final Vector halfABlock = new Vector(0.5, 0.5, 0.5);

  /**
   * Adds a relative platform to the absolute position of a player
   */
  public static Vector AdjustJump(Location oldPlatform, Platform newPlatform)
  {
    Vector relativeCoords = AdjustCoordinatesToDirection(newPlatform);

    Vector absoluteCoords = new Vector(oldPlatform.getBlockX(), oldPlatform.getBlockY(), oldPlatform.getBlockZ()).add(halfABlock);

    absoluteCoords.add(relativeCoords);

    return absoluteCoords;
  }

  private static Vector AdjustCoordinatesToDirection(Platform platform)
  {
    int x = 0;
    // Height = 0 should mean under the player, not inside him, so we have to correct the y-coordinate by -1
    int y = platform.platformConfiguration.getHeightOffset();
    int z = 0;

    int forwardOffset = platform.platformConfiguration.forwardOffset;
    int sidewaysOffset = platform.platformConfiguration.sidewaysOffset;

    // Sets the forward and sideways length to corresponding, relative coordinates
    // (Basically changes forwardOffset and sidewaysOffset with information from the directions to x & z
    // coordinates relative to the player)
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

    return new Vector(x, y, z);
  }

  /**
   * This function checks if an area is completely made out of air. Point A and B are interchangeable.
   * @param coords1 Point A
   * @param coords2 Point By
   * @param world World for which the two points are made for
   * @return If area between (including) Point A & B is made out of thin air (Material AIR)
   */
  public static boolean isAreaAirOnly(Vector coords1, Vector coords2, World world)
  {
    Vector start = new Vector();
    Vector end = new Vector();

    // Replaces Math.Max and Math.Min with only 1 operation (not as pretty, but it runs faster)
    if(coords1.getBlockX() > coords2.getBlockX())
    {
      start.setX(coords2.getBlockX());
      end.setX(coords1.getBlockX());
    }
    else
    {
      start.setX(coords1.getBlockX());
      end.setX(coords2.getBlockX());
    }

    if(coords1.getBlockY() > coords2.getBlockY())
    {
      start.setY(coords2.getBlockY());
      end.setY(coords1.getBlockY());
    }
    else
    {
      start.setY(coords1.getBlockY());
      end.setY(coords2.getBlockY());
    }

    if(coords1.getBlockZ() > coords2.getBlockZ())
    {
      start.setZ(coords2.getBlockZ());
      end.setZ(coords1.getBlockZ());
    }
    else
    {
      start.setZ(coords1.getBlockZ());
      end.setZ(coords2.getBlockZ());
    }

    Location location = new Location(world, 0, 0, 0);

    for(int x = start.getBlockX(); x <= end.getX(); x++)
    {
      for(int y = start.getBlockY(); y <= end.getY(); y++)
      {
        for(int z = start.getBlockZ(); z <= end.getZ(); z++)
        {
          location.setX(x);
          location.setY(y);
          location.setZ(z);

          if(!BlockHelper.isAir(location.getBlock().getType()))
          {
            return false;
          }

          //location.getBlock().setType(Material.GOLD_BLOCK);
        }
      }
    }
    return true;
  }

}