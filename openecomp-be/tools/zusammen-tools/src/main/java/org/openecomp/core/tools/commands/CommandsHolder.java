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
package org.openecomp.core.tools.commands;

import static org.openecomp.core.tools.commands.Command.COMMAND_OPTION;
import static org.openecomp.core.tools.util.Utils.printMessage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.importinfo.ImportDataCommand;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class CommandsHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsHolder.class);
    private static final Options OPTIONS = new Options();
    private static final Map<CommandName, Command> COMMANDS = new EnumMap<>(CommandName.class);

    static {
        OPTIONS.addOption(Option.builder(COMMAND_OPTION).hasArg().argName("command").desc("command name, mandatory").build());
        registerCommands();
    }

    private CommandsHolder() {
    }

    private static void registerCommands() {
        new SetHealingFlag().register();
        new ExportDataCommand().register();
        new ImportDataCommand().register();
        new HealAll().register();
        new PopulateUserPermissions().register();
        new AddContributorCommand().register();
        new CleanUserDataCommand().register();
        new DeletePublicVersionCommand().register();
        new SetHealingFlagByItemVersionCommand().register();
    }

    public static Optional<Command> getCommand(String[] args) {
        CommandLine cmd = parseArgs(args);
        return cmd == null || !cmd.hasOption(COMMAND_OPTION) || cmd.getOptionValue(COMMAND_OPTION) == null ? Optional.empty()
            : getCommandName(cmd.getOptionValue(COMMAND_OPTION)).map(COMMANDS::get);
    }

    public static void printUsages() {
        COMMANDS.values().forEach(Command::printUsage);
    }

    private static Optional<CommandName> getCommandName(String commandName) {
        try {
            return Optional.of(CommandName.valueOf(commandName));
        } catch (IllegalArgumentException iae) {
            printMessage(LOGGER, String.format("message: %s is illegal command.", commandName));
            return Optional.empty();
        }
    }

    private static CommandLine parseArgs(String[] args) {
        try {
            return new DefaultParser().parse(OPTIONS, args, true);
        } catch (ParseException e) {
            LOGGER.error("Error parsing arguments", e);
            return null;
        }
    }

    static void addCommand(Command command) {
        CommandName commandName = command.getCommandName();
        if (COMMANDS.containsKey(commandName)) {
            throw new IllegalArgumentException(String.format("Command with the name %s was already registered", commandName));
        }
        COMMANDS.put(commandName, command);
    }
}
