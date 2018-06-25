package org.openecomp.core.tools.main;

import static org.openecomp.core.tools.util.Utils.printMessage;

import java.time.Duration;
import java.time.Instant;
import org.openecomp.core.tools.commands.AddContributorCommand;
import org.openecomp.core.tools.commands.CleanUserDataCommand;
import org.openecomp.core.tools.commands.DeletePublicVersionCommand;
import org.openecomp.core.tools.commands.HealAll;
import org.openecomp.core.tools.commands.PopulateUserPermissions;
import org.openecomp.core.tools.commands.SetHealingFlag;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.importinfo.ImportDataCommand;
import org.openecomp.core.tools.util.ToolsUtil;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class ZusammenMainTool {

  private static final String GLOBAL_USER = "GLOBAL_USER";
  private static Logger logger = LoggerFactory.getLogger(ZusammenMainTool.class);
  private static int status = 0;

  public static void main(String[] args) {

    COMMANDS command = getCommand(args);

    if (command == null) {
      printMessage(logger,
          "parameter -c is mandatory. script usage: zusammenMainTool.sh -c {command name} " +
              "[additional arguments depending on the command] ");
      printMessage(logger,
          "reset old version: -c RESET_OLD_VERSION [-v {version}]");
      printMessage(logger,
          "export: -c EXPORT [-i {item id}]");
      printMessage(logger,
          "import: -c IMPORT -f {zip file full path}");
      printMessage(logger,
          "heal all: -c HEAL_ALL [-t {number of threads}]");
      printMessage(logger,
          "add users as contributors: -c ADD_CONTRIBUTOR [-p {item id list file path}] -u {user " +
              "list file path}");
      printMessage(logger,
              "clean user data: -c CLEAN_USER_DATA -i {item id} -u {user}");
      printMessage(logger,
              "delete public version: -c DELETE_PUBLIC_VERSION -i {item id} -v {version_id}");
      System.exit(-1);
    }
    Instant startTime = Instant.now();

    SessionContextProviderFactory.getInstance().createInterface().create(GLOBAL_USER, "dox");

    switch (command) {
      case RESET_OLD_VERSION:
        SetHealingFlag.populateHealingTable(ToolsUtil.getParam("v", args));
        break;
      case EXPORT:
        ExportDataCommand.exportData(ToolsUtil.getParam("i", args));
        break;
      case IMPORT:
        ImportDataCommand.execute(ToolsUtil.getParam("f", args));
        break;
      case HEAL_ALL:
        HealAll.healAll(ToolsUtil.getParam("t", args));
        break;
      case POPULATE_USER_PERMISSIONS:
        PopulateUserPermissions.execute();
        break;
      case ADD_CONTRIBUTOR:
        AddContributorCommand.add(ToolsUtil.getParam("p", args), ToolsUtil.getParam("u", args));
        break;
      case CLEAN_USER_DATA:
        CleanUserDataCommand.execute(ToolsUtil.getParam("i", args), ToolsUtil.getParam("u", args));
        break;
      case DELETE_PUBLIC_VERSION:
        DeletePublicVersionCommand.execute(ToolsUtil.getParam("i", args), ToolsUtil.getParam("v", args));
        break;

    }

    Instant stopTime = Instant.now();
    Duration duration = Duration.between(startTime, stopTime);
    long minutesPart = duration.toMinutes();
    long secondsPart = duration.minusMinutes(minutesPart).getSeconds();


    printMessage(logger,
        "Zusammen tools command:[] finished . Total run time was : " + minutesPart + ":" +
            secondsPart
            + " minutes");
    System.exit(status);

  }

  private static COMMANDS getCommand(String[] args) {
    String commandSrt = ToolsUtil.getParam("c", args);
    try {
      return COMMANDS.valueOf(commandSrt);
    } catch (IllegalArgumentException iae) {
      printMessage(logger, "message:" + commandSrt + " is illegal.");
    }
    return null;
  }

  private enum COMMANDS {
    RESET_OLD_VERSION,
    EXPORT,
    IMPORT,
    HEAL_ALL,
    POPULATE_USER_PERMISSIONS,
    ADD_CONTRIBUTOR,
    CLEAN_USER_DATA,
    DELETE_PUBLIC_VERSION
  }
}
