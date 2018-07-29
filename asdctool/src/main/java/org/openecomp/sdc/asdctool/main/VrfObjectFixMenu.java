package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.configuration.VrfObjectFixConfiguration;
import org.openecomp.sdc.asdctool.impl.VrfObjectFixHandler;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

public class VrfObjectFixMenu {

    private static final Logger log = Logger.getLogger(VrfObjectFixMenu.class);

    private VrfObjectFixMenu(){}

    public static void main(String[] args) {
        if (isNotValidArguments(args)) {
            log.debug("#main - The invalid array of the arguments have been received: {}", Arrays.toString(args));
            log.debug("#main - Usage: <configuration dir> <'detect'/'fix'> <output folder path>");
            System.exit(1);
        }
        initConfig(args[0]);
        VrfObjectFixHandler vrfObjectFixHandler = getVrfObjectFixHandler();
        if (vrfObjectFixHandler.handle(args[1], args.length == 3 ? args[2] : null)) {
            log.info("#main - The {} operation of the corrupted VRFObject Node Types has been finished successfully", args[1]);
        } else{
            log.info("#main - The {} operation of the corrupted VRFObject Node Types has been failed", args[1]);
            System.exit(2);
        }
        System.exit(0);
    }

    private static VrfObjectFixHandler getVrfObjectFixHandler() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(VrfObjectFixConfiguration.class);
        return context.getBean(VrfObjectFixHandler.class);
    }

    private static boolean isNotValidArguments(String[] args) {
        return args == null || args.length < 2;
    }


    private static void initConfig(String configDir) {
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), configDir);
        new ConfigurationManager(configurationSource);
    }

}
