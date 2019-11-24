package org.werti.jumpn.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.werti.jumpn.BlockHandling.Platform;
import org.werti.jumpn.Globals;

import java.sql.Array;
import java.util.Arrays;

public class CommandTest implements CommandExecutor
{
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings)
  {
    Platform tempPlatform;

    int times[] = new int[5];

    Arrays.fill(times, 0);

    Globals.DEBUG = false;

    for(int i = 0; i < 100000; i++)
    {
      tempPlatform = Platform.GetRandomPlatform();

      times[tempPlatform.platformConfiguration.getForwardOffset()]++;
    }

    Globals.DEBUG = true;

    for(int i = 0; i < times.length; i++)
    {
      Globals.debug(String.format("Forward %d happened %d", i, times[i]));
    }

    return true;
  }
}
