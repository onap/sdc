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
        log.info("Start fixing artifact UUID after 1707 migration with arguments run with configuration [{}] , for [{}] services", runMode,
            fixServices);
        String appConfigDir = args[0];
        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ArtifactUUIDFixConfiguration.class);
        ArtifactUuidFix artifactUuidFix = context.getBean(ArtifactUuidFix.class);
        boolean isSuccessful = artifactUuidFix.doFix(fixServices, runMode);
        if (isSuccessful) {
            log.info("Fixing artifacts UUID for 1707  was finished successfully");
        } else {
            log.warn("Fixing artifacts UUID for 1707  has failed");
            System.exit(2);
        }
        System.exit(0);
    }
}
