package org.werti.jumpn.BlockHandling;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockHelper
{
  public static boolean isAir(Material material)
  {
    return (material == Material.AIR) || (material == Material.CAVE_AIR) || (material == Material.VOID_AIR);
  }

  public static boolean isSameLocation(Location location1, Location location2)
  {
    return location1.getBlockX() == location2.getBlockX()
           && location1.getBlockY() == location2.getBlockY()
           && location1.getBlockZ() == location2.getBlockZ();
  }
}
