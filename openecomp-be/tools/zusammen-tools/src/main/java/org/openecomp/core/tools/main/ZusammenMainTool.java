/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
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
        printMessage(LOGGER, String
            .format("Zusammen tools command %s finished. Total run time was %s:%s minutes.", command.getCommandName(), minutesPart, secondsPart));
    }
}
