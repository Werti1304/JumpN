package org.werti.jumpn;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Random;

public class JumpN
{
  static Material blockType = Material.GLASS;

  JumpNPlayer jumpNPlayer;
  Random random;

  public JumpN(JumpNPlayer jumpNPlayer)
  {
    this.jumpNPlayer = jumpNPlayer;
  }

  public void start()
  {
    random = new Random();
  }
}
