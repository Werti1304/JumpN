package org.werti.jumpn;
import org.bukkit.plugin.java.JavaPlugin;

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

    logger.info("Enabling JumpN...");

    logger.info("Setting Globals..");
    Globals.plugin = this;
    Globals.bukkitServer = this.getServer();
    Globals.logger = logger;

    logger.info("JumpN is now enabled!");
  }

  @Override
  public void onDisable()
  {
    Globals.logger.info("Disabling JumpN..");

    Globals.logger.info("JumpN is now disabled!");
  }
}
