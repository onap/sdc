/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
