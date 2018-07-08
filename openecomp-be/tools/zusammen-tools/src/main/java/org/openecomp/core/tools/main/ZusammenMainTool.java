package org.openecomp.core.tools.main;

import static org.openecomp.core.tools.util.Utils.printMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.openecomp.core.tools.commands.Command;
import org.openecomp.core.tools.commands.CommandsHolder;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class ZusammenMainTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZusammenMainTool.class);

    public static void main(String[] args) {
        Command command = getCommandToRun(args);

        Instant startTime = Instant.now();
        SessionContextProviderFactory.getInstance().createInterface().create("GLOBAL_USER", "dox");
        if (!command.execute(args)) {
            command.printUsage();
            System.exit(-1);
        }
        Instant stopTime = Instant.now();

        printDuration(command, startTime, stopTime);
        System.exit(0);
    }

    private static Command getCommandToRun(String[] args) {
        Optional<Command> command = CommandsHolder.getCommand(args);
        if (!command.isPresent()) {
            LOGGER.error("Illegal execution.");
            CommandsHolder.printUsages();
            System.exit(-1);
        }
        return command.get();
    }

    private static void printDuration(Command command, Instant startTime, Instant stopTime) {
        Duration duration = Duration.between(startTime, stopTime);
        long minutesPart = duration.toMinutes();
        long secondsPart = duration.minusMinutes(minutesPart).getSeconds();

        printMessage(LOGGER, String.format("Zusammen tools command %s finished. Total run time was %s:%s minutes.",
                command.getCommandName(), minutesPart, secondsPart));
    }
}
