package org.werti.jumpn.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.werti.jumpn.JumpNPlayer;

public class OnMove implements Listener
{
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent playerMoveEvent)
    {
        JumpNPlayer jumpNPlayer = JumpNPlayer.GetJumpNPlayer(playerMoveEvent.getPlayer());

        if(jumpNPlayer == null)
        {
            return;
        }


    }
}
