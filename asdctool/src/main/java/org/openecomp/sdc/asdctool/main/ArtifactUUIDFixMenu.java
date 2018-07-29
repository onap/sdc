package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.configuration.ArtifactUUIDFixConfiguration;
import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ArtifactUUIDFixMenu {

    private static Logger log = Logger.getLogger(ArtifactUUIDFixMenu.class.getName());

    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.out.println("Usage: <configuration dir> <all/distributed_only> <services/service_vf/fix/fix_only_services>");
            System.exit(1);
        }
        String fixServices = args[1];
        String runMode = args[2];
       // String fixTosca = args[3];
        log.info("Start fixing artifact UUID after 1707 migration with arguments run with configuration [{}] , for [{}] services", runMode, fixServices);
        String appConfigDir = args[0];
        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ArtifactUUIDFixConfiguration.class);
        ArtifactUuidFix artifactUuidFix = context.getBean(ArtifactUuidFix.class);
        boolean isSuccessful = artifactUuidFix.doFix(fixServices, runMode);
        if (isSuccessful) {
            log.info("Fixing artifacts UUID for 1707  was finished successfully");
    
        } else{
            log.info("Fixing artifacts UUID for 1707  has failed");
            System.exit(2);
        }
        System.exit(0);
    }

}
