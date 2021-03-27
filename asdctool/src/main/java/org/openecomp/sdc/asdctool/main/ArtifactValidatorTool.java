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

import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationToolConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ArtifactValidatorTool {

    public static void main(String[] args) {
        String outputPath = args[0];
        String txtReportFilePath = ValidationConfigManager.txtReportFilePath(outputPath);
        String appConfigDir = args[1];
        AnnotationConfigApplicationContext context = initContext(appConfigDir);
        ArtifactToolBL validationToolBL = context.getBean(ArtifactToolBL.class);
        System.out.println("Start ArtifactValidation Tool");
        Boolean result = validationToolBL.validateAll(txtReportFilePath);
        if (result) {
            System.out.println("ArtifactValidation finished successfully");
            System.exit(0);
        } else {
            System.out.println("ArtifactValidation finished with warnings");
            System.exit(2);
        }
    }

    private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ValidationToolConfiguration.class);
        return context;
    }
}
