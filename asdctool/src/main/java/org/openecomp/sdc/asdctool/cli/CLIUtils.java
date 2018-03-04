package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.Option;

public class CLIUtils {

    static final String CONFIG_PATH_SHORT_OPT = "c";
    private static final String CONFIG_PATH_LONG_OPT = "configFolderPath";

    private CLIUtils(){}

    public static Option getConfigurationPathOption() {
        return Option.builder(CONFIG_PATH_SHORT_OPT)
                .longOpt(CONFIG_PATH_LONG_OPT)
                .required()
                .hasArg()
                .desc("path to sdc configuration folder - required")
                .build();
    }

}
