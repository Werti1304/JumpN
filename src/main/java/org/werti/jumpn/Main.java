package org.werti.jumpn;
import org.bukkit.plugin.java.JavaPlugin;
import org.werti.jumpn.Commands.CommandJumpN;
import org.werti.jumpn.Events.Jumpn.JumpnEventListener;
import org.werti.jumpn.Events.OnMove;

import java.util.Objects;
import java.util.logging.Logger;

/// Aufgabenstellung
/*
Hätten dann folgende Testaufgabe: Ein kleines Plugin, dass (auf Command) ein kleines
sich selbst aufbauendes JumpAndRun erstellt. D.h. Glasblöcke mit ein paar Partikeln,
sobald du auf einen springst spawnt der nächste und der vorherige despawnt. Dabei die
aktuelle Punktzahl in der Actionbar anzeigen und beim Fall das Endergebnis in den
Chat schreiben
*/

public class Main extends JavaPlugin
{
  @Override
  public void onEnable()
  {
    Logger logger = getLogger();

    logger.info("Setting Globals");
    Globals.plugin = this;
    Globals.bukkitServer = this.getServer();
    Globals.logger = logger;
    Globals.consoleCommandSender = Globals.bukkitServer.getConsoleSender();

    logger.info("Registering events");
    registerEvents();

    logger.info("Registering commands");
    registerCommands();

    logger.info("JumpN is now enabled!");
  }

  @Override
  public void onDisable()
  {
    Globals.logger.info("Disabling JumpN");

    Globals.logger.info("Resetting all changed blocks");
    resetAllBlocks();

    Globals.logger.info("JumpN is now disabled!");
  }

  private void registerEvents()
  {
    Globals.bukkitServer.getPluginManager().registerEvents(new OnMove(), this);
    Globals.bukkitServer.getPluginManager().registerEvents(new JumpnEventListener(), this);
  }

  private void registerCommands()
  {
    Objects.requireNonNull(this.getCommand("jumpn")).setExecutor(new CommandJumpN());
  }

  /**
   * Resets all blocks for all JumpN players, so they don't see blocks that aren't even registered for the plugin anymore
   */
  private void resetAllBlocks()
  {
    for(JumpN jumpN : JumpN.getJumpNList())
    {
      jumpN.resetBlocks();
    }
  }

}
