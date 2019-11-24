package org.werti.jumpn.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.werti.jumpn.JumpN;
import org.werti.jumpn.JumpNPlayer;

public class CommandJumpN implements CommandExecutor
{
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings)
  {
    if(!(commandSender instanceof Player))
    {
      commandSender.sendMessage("Command only allowed for Players!");

      return true;
    }

    Player player = (Player)commandSender;

    JumpNPlayer jumpNPlayer = JumpNPlayer.GetJumpNPlayer(player);

    if(jumpNPlayer!= null)
    {
      // TODO: Add String-Resource or sth
      jumpNPlayer.sendMessage(JumpNPlayer.MessageType.Error, "You can't start 2 JumpNRuns while your first one isn't even finished!");
      return true;
    }

    jumpNPlayer = JumpNPlayer.Add(player);

    JumpN jumpN = new JumpN(jumpNPlayer);

    jumpN.SetNextPlatform();

    return true;
  }
}
