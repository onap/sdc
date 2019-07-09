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

import org.openecomp.sdc.asdctool.impl.validator.ValidationToolBL;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationToolConfiguration;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by chaya on 7/3/2017.
 */
public class ValidationTool {

    private static Logger log = Logger.getLogger(ValidationTool.class.getName());

    public static void main(String[] args) {

        String outputPath = args[0];
        ValidationConfigManager.setOutputFullFilePath(outputPath);
        ValidationConfigManager.setCsvReportFilePath(outputPath);

        String appConfigDir = args[1];
        AnnotationConfigApplicationContext context = initContext(appConfigDir);
        ValidationToolBL validationToolBL = context.getBean(ValidationToolBL.class);

        log.info("Start Validation Tool");
        Boolean result = validationToolBL.validateAll();
        ReportManager.reportEndOfToolRun();
        if (result) {
            log.info("Validation finished successfully");
            System.exit(0);
        } else {
            log.info("Validation finished with warnings");
            System.exit(2);
        }
    }

    private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ValidationToolConfiguration.class);
        return context;
    }
}
