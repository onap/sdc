package org.openecomp.core.tools.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class Command {

    static final String COMMAND_OPTION = "c";
    protected final Options options = new Options();

    protected Command() {
        options.addOption(
                Option.builder(COMMAND_OPTION).hasArg().argName("command").desc(getCommandName().name()).build());
    }

    protected CommandLine parseArgs(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return cmd;
    }

    public void printUsage() {
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("zusammenMainTool", options);
    }

    public void register(){
        CommandsHolder.addCommand(this);
    }

    public abstract boolean execute(String[] args);

    public abstract CommandName getCommandName();
}
