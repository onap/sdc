package org.openecomp.core.tools.main;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import org.openecomp.core.tools.Commands.HealAll;
import org.openecomp.core.tools.Commands.SetHealingFlag;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.importinfo.ImportDataCommand;
import org.openecomp.core.tools.util.ToolsUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

import static org.openecomp.core.tools.util.Utils.printMessage;

public class ZusammenMainTool {

  private static final String GLOBAL_USER = "GLOBAL_USER";
  private static Logger logger = LoggerFactory.getLogger(ZusammenMainTool.class);
  private static int status = 0;

  public static void main(String[] args) {

    String command = ToolsUtil.getParam("c",args);
    if(command == null){
      printMessage(logger,
              "parameter -c is mandatory. script usage: zusammenMainTool.sh -c {command name} " +
                      "[additional arguments depending on the command] ");
      System.exit(-1);
    }
    Instant startTime = Instant.now();

    SessionContext context = new SessionContext();
    context.setUser(new UserInfo(GLOBAL_USER));
    context.setTenant("dox");


    switch (COMMANDS.valueOf(command)){
      case RESET_OLD_VERSION:
        SetHealingFlag.populateHealingTable(ToolsUtil.getParam("v",args));
        break;
      case EXPORT:
        ExportDataCommand.exportData(ToolsUtil.getParam("i",args));
        break;
      case IMPORT:
        ImportDataCommand.execute(context, ToolsUtil.getParam("f",args));
        break;
      case HEAL_ALL:
        HealAll.healAll(ToolsUtil.getParam("t",args));

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

  private enum COMMANDS{


    RESET_OLD_VERSION("reset-old-version"),
    EXPORT("export"),
    IMPORT("import"),
    HEAL_ALL("heal-all");

    COMMANDS(String command) {
      this.command  = command;
    }

    private String command;
  }

}
