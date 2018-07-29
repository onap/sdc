package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * abstract base class to extend when implementing a cli tool
 */
public abstract class CLITool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLITool.class);

    public CLIToolData init(String[] args) {
        CommandLine commandLine = initCmdLineOptions(args);
        return new CLIToolData(commandLine);
    }

    private CommandLine initCmdLineOptions(String[] args) {
        Options options = buildCmdLineOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse( options, args );
        }
        catch( ParseException exp ) {
            LOGGER.error("Parsing failed.  Reason: " + exp.getMessage() );
            usageAndExit(options);
            return null;
        }
    }

    private void usageAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(commandName(), options );
        System.exit(1);
    }

    /**
     *
     * @return all command line options required by this command line tool
     */
    protected abstract Options buildCmdLineOptions();

    /**
     *
     * @return the command name
     */
    protected abstract String commandName();


}
