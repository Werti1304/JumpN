package org.werti.jumpn.BlockHandling;

import org.bukkit.Material;

public class BlockHelper
{
  public static boolean isAir(Material material)
  {
    return (material == Material.AIR) || (material == Material.CAVE_AIR) || (material == Material.VOID_AIR);
  }
}
