package org.werti.jumpn.BlockHandling;

import org.werti.jumpn.Globals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    Globals.debug(String.format("Platform generated at Offsets: F:%d S:%d H:%d D:%s SD:%s",
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
    // J.. Forward Jump
    // S.. Sideways jump
    // H.. Jump with height-difference
    J1(1,0,0,1),
    J2(2,0,0,5),
    J3(3,0,0,70),
    J4(4,0,0,80),
    J5(5,0,0,30),
    JH1(1,1,0,10),
    JH2(2,1,0,30),
    JH3(3,1,0,100),
    JH4(4,1,0,80),
    JS1(1,0,1,1),
    JS2(2,0,1,15),
    JS3(3,0,1,50),
    JS4(4,0,1,70),
    JS5(5,0,1,20),
    JHS1(1,1,1,1),
    JHS2(2,1,1,10),
    JHS3(3,1,1,25),
    JHS4(4,1,1,50),
    JSS2(2,0,2,10),
    JSS3(3,0,2,20),
    JSS4(4,0,2,30),
    JSS5(5,0,2,1),
    JSSS3(3,0,3,30),
    JSSS4(4,0,3,15),
    JSSSS4(4,0,4,5),
    ;

    int forwardOffset;
    int heightOffset;
    int sidewaysOffset;
    int probability;
    int iterativeprobability;

    /**
     * @param forwardOffset
     * @param heightOffset
     * @param sidewaysOffset
     * @param probability Probability-Coefficient from 1-100 of the platform to spawn (1min, 10max)
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
      int rnd = ThreadLocalRandom.current().nextInt(probabilityCount) + 1;

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

    public static Direction GetRandom()
    {
      return LIST.get(ThreadLocalRandom.current().nextInt(SIZE));
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

    // Return Left or Right random, None is not needed
    public static SidewaysDirection GetRandom()
    {
      return LIST.get(ThreadLocalRandom.current().nextInt(SIZE - 1));
    }
  }

  public static int GetRandomStartingHeight()
  {
    return ThreadLocalRandom.current().nextInt(Globals.maxHeight - Globals.minHeight) + Globals.minHeight;
  }
}
