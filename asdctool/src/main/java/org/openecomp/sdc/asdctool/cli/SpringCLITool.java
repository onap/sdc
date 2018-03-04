package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.Options;
import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * abstract class to extend when implementing a spring and sdc configuration based command line tool
 */
public abstract class SpringCLITool extends CLITool {

    @Override
    public CLIToolData init(String[] args) {
        CLIToolData cliToolData = super.init(args);
        String appConfigDir = cliToolData.getCommandLine().getOptionValue(CLIUtils.CONFIG_PATH_SHORT_OPT);
        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(getSpringConfigurationClass());
        cliToolData.setSpringApplicationContext(context);
        return cliToolData;
    }

    @Override
    protected Options buildCmdLineOptions() {
        return new Options().addOption(CLIUtils.getConfigurationPathOption());
    }

    /**
     *
     * @return the {@code Class} which holds all the spring bean declaration needed by this cli tool
     */
    protected abstract Class<?> getSpringConfigurationClass();
}
