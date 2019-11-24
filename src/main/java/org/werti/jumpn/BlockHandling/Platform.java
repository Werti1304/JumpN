package org.werti.jumpn.BlockHandling;

import org.werti.jumpn.Globals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Platform
{
  public PlatformConfiguration platformConfiguration;
  Direction direction;
  SidewaysDirection sidewaysDirection;

  public Platform(PlatformConfiguration platformConfiguration, Direction direction, SidewaysDirection sidewaysDirection)
  {
    this.platformConfiguration = platformConfiguration;
    this.direction = direction;
    this.sidewaysDirection = sidewaysDirection;
  }

  public static Platform GetRandomPlatform()
  {
    PlatformConfiguration platformConfiguration = PlatformConfiguration.GetRandom();
    Direction direction = Direction.GetRandom();
    SidewaysDirection sidewaysDirection;

    if(platformConfiguration.getSidewaysOffset() == 0)
    {
      sidewaysDirection = SidewaysDirection.None;
    }
    else
    {
      sidewaysDirection = SidewaysDirection.GetRandom();
    }

    Globals.debug(String.format("Platform generated at Offsets:\nForward:%d\nSideways:%d\nHeight:%d\nDirection:%s\nSideways Direction:%s",
                                platformConfiguration.forwardOffset,
                                platformConfiguration.sidewaysOffset,
                                platformConfiguration.heightOffset,
                                direction.toString(),
                                sidewaysDirection.toString()));

    return new Platform(platformConfiguration, direction, sidewaysDirection);
  }

  // Declared outside to be able to be used inside enum PlatformConfiguration
  private static int probabilityCount = 0;

  public enum PlatformConfiguration
  {
    // TODO: More platforms
    J1(2,0,0,5),
    J2(3,0,0,10),
    J3(4,0,0,2),
    J4(2,1,0,3),
    J5(3,1,0,8),
    J6(4,1,0,5)
    ;

    int forwardOffset;
    int heightOffset;
    int sidewaysOffset;
    int probability;
    int iterativeprobability;

    private static final Random random = new Random();

    /**
     * @param forwardOffset
     * @param heightOffset
     * @param sidewaysOffset
     * @param probability Probability-Coefficient from 1-10 of the platform to spawn (1min, 10max)
     */
    PlatformConfiguration(int forwardOffset, int heightOffset, int sidewaysOffset, int probability)
    {
      this.forwardOffset = forwardOffset;
      this.heightOffset = heightOffset;
      this.sidewaysOffset = sidewaysOffset;
      this.probability = probability;

      probabilityCount += probability;
      iterativeprobability = probabilityCount;
    }

    public int getForwardOffset()
    {
      return forwardOffset;
    }

    public int getHeightOffset()
    {
      return heightOffset;
    }

    public int getSidewaysOffset()
    {
      return sidewaysOffset;
    }

    public int getProbability()
    {
      return probability;
    }

    public int getIterativeprobability()
    {
      return iterativeprobability;
    }

    private static PlatformConfiguration GetRandom()
    {
      int rnd = random.nextInt(probabilityCount) + 1;

      for(PlatformConfiguration platformConfiguration : PlatformConfiguration.values())
      {
        if(platformConfiguration.iterativeprobability >= rnd)
        {
          return platformConfiguration;
        }
      }

      // Default value. Arithmetically, this line can never be executed
      return PlatformConfiguration.J3;
    }
  }

  public enum Direction
  {
    North,
    East,
    South,
    West
    ;

    private static final List<Direction> LIST = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = LIST.size();
    private static final Random RANDOM = new Random();

    public static Direction GetRandom()
    {
      return LIST.get(RANDOM.nextInt(SIZE));
    }
  }

  public enum SidewaysDirection
  {
    Left,
    Right,
    None
    ;

    private static final List<SidewaysDirection> LIST = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = LIST.size();
    private static final Random RANDOM = new Random();

    // Return Left or Right random, None is not needed
    public static SidewaysDirection GetRandom()
    {
      return LIST.get(RANDOM.nextInt(SIZE - 1));
    }
  }
}
