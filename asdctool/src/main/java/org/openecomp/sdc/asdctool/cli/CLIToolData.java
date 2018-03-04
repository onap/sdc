package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.CommandLine;
import org.springframework.context.support.AbstractApplicationContext;

public class CLIToolData {

    private CommandLine commandLine;
    private AbstractApplicationContext springApplicationContext;

    public CLIToolData(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public CLIToolData(CommandLine commandLine, AbstractApplicationContext springApplicationContext) {
        this.commandLine = commandLine;
        this.springApplicationContext = springApplicationContext;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public AbstractApplicationContext getSpringApplicationContext() {
        return springApplicationContext;
    }

    public void setSpringApplicationContext(AbstractApplicationContext springApplicationContext) {
        this.springApplicationContext = springApplicationContext;
    }
}
